package org.robotframework.ide.eclipse.main.plugin.project.build;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.build.FatalProblemsReporter.ReportingInterruptedException;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ConfigFileProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.RuntimeEnvironmentProblem;

public class RobotProjectBuilder extends IncrementalProjectBuilder {

    @Override
    protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
        try {
            final IProject project = getProject();

            final LibspecsFolder libspecsFolder = LibspecsFolder.createIfNeeded(getProject());
            final boolean rebuildNeeded = libspecsFolder.shouldRegenerateLibspecs(getDelta(project), kind);

            final Job buildJob = createBuildJob(rebuildNeeded);
            final Job validationJob = new RobotProjectValidator().createValidationJob(project);
            final IProgressMonitor progressMonitor = Job.getJobManager().createProgressGroup();
            try {
                final String projectPath = project.getFullPath().toString();

                progressMonitor.beginTask("Building and validating " + projectPath + " project", 200);

                buildJob.setProgressGroup(progressMonitor, 100);
                buildJob.schedule();

                validationJob.setProgressGroup(progressMonitor, 100);
                validationJob.schedule();

                monitor.subTask("waiting for project " + projectPath + " build end");
                buildJob.join();

                if (buildJob.getResult().getSeverity() == IStatus.CANCEL) {
                    RobotFramework.getModelManager().getModel().createRobotProject(project).clear();
                    if (libspecsFolder.exists()) {
                        libspecsFolder.remove();
                        return new IProject[0];
                    }
                }
                RobotFramework.getModelManager().getModel().createRobotProject(project).clear();
                project.refreshLocal(IResource.DEPTH_INFINITE, monitor);

                if (!monitor.isCanceled()) {
                    monitor.subTask("waiting for project validation end");
                    validationJob.join();
                }
            } catch (final InterruptedException e) {
                throw new CoreException(Status.CANCEL_STATUS);
            } finally {
                progressMonitor.done();
            }
            return new IProject[0];
        } finally {
            monitor.worked(1);
        }
    }

    private Job createBuildJob(final boolean buildingIsNeeded) {
        if (buildingIsNeeded) {
            return new Job("Building") {
                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    try {
                        try {
                            getProject().getFile(".project").deleteMarkers(IMarker.PROBLEM, true,
                                    IResource.DEPTH_INFINITE);
                            getProject().getFile(RobotProjectConfig.FILENAME).deleteMarkers(IMarker.PROBLEM, true,
                                    IResource.DEPTH_INFINITE);
                        } catch (final CoreException e) {
                            // that's fine, lets try to build project
                        }
                        buildLibrariesSpecs(getProject(), monitor, new FatalProblemsReporter());
                        monitor.done();
                        return Status.OK_STATUS;
                    } catch (final ReportingInterruptedException e) {
                        return new Status(IStatus.CANCEL, RobotFramework.PLUGIN_ID, "Unable to build libraries", e);
                    }
                }
            };
        } else {
            return new Job("Skipping build") {
                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };
        }
    }

    private void buildLibrariesSpecs(final IProject project, final IProgressMonitor monitor,
            final IProblemsReporter reporter) {
        if (monitor.isCanceled()) {
            return;
        }
        final SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        subMonitor.beginTask("Building", 100);
        subMonitor.subTask("checking Robot execution environment");

        final RobotProject robotProject = RobotFramework.getModelManager().getModel().createRobotProject(project);
        final RobotProjectConfig configuration = provideConfiguration(subMonitor.newChild(10), robotProject, reporter);
        if (subMonitor.isCanceled()) {
            return;
        }

        final RobotRuntimeEnvironment runtimeEnvironment = provideRuntimeEnvironment(subMonitor.newChild(10),
                robotProject, configuration, reporter);
        if (subMonitor.isCanceled()) {
            return;
        }

        final List<String> stdLibs = runtimeEnvironment.getStandardLibrariesNames();
        final LibspecsFolder libspecsFolder = LibspecsFolder.get(project);
        final List<IFile> specsToRecreate = newArrayList();
        try {
            specsToRecreate.addAll(libspecsFolder.collectSpecsWithDifferentVersion(stdLibs,
                    runtimeEnvironment.getVersion()));
        } catch (final CoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        subMonitor.setWorkRemaining(specsToRecreate.size());

        for (final IFile spec : specsToRecreate) {
            if (subMonitor.isCanceled()) {
                return;
            }
            final String libName = spec.getLocation().removeFileExtension().lastSegment();
            subMonitor.subTask("generating libdoc for " + libName + " library");
            runtimeEnvironment.createLibdocForStdLibrary(libName, spec.getLocation().toFile());
            subMonitor.worked(1);
        }
    }

    private RobotProjectConfig provideConfiguration(final IProgressMonitor monitor,
            final RobotProject robotProject, final IProblemsReporter reporter) {
        try {
            if (!robotProject.getConfigurationFile().exists()) {
                final RobotProblem problem = RobotProblem.causedBy(ConfigFileProblem.DOES_NOT_EXIST);
                reporter.handleProblem(problem, robotProject.getFile(".project"), 1);
            }
            return new RobotProjectConfigReader().readConfiguration(robotProject);
        } catch (final CannotReadProjectConfigurationException e) {
            final RobotProblem problem = RobotProblem.causedBy(ConfigFileProblem.OTHER_PROBLEM);
            problem.fillFormattedMessageWith(e.getMessage());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), e.getLineNumber());
            return null;
        } finally {
            monitor.done();
        }
    }

    private RobotRuntimeEnvironment provideRuntimeEnvironment(final IProgressMonitor monitor,
            final RobotProject robotProject, final RobotProjectConfig configuration,
            final IProblemsReporter reporter) {
        try {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            if (runtimeEnvironment == null) {
                final RobotProblem problem = RobotProblem.causedBy(RuntimeEnvironmentProblem.MISSING_ENVIRONMENT);
                problem.fillFormattedMessageWith(configuration.getPythonLocation());
                reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
            } else if (!runtimeEnvironment.isValidPythonInstallation()) {
                final RobotProblem problem = RobotProblem.causedBy(RuntimeEnvironmentProblem.NON_PYTHON_INSTALLATION);
                problem.fillFormattedMessageWith(runtimeEnvironment.getFile());
                reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
            } else if (!runtimeEnvironment.hasRobotInstalled()) {
                final RobotProblem problem = RobotProblem.causedBy(RuntimeEnvironmentProblem.MISSING_ROBOT);
                problem.fillFormattedMessageWith(runtimeEnvironment.getFile());
                reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
            }
            return runtimeEnvironment;
        } finally {
            monitor.done();
        }
    }

    @Override
    protected void clean(final IProgressMonitor monitor) throws CoreException {
        getProject().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        RobotFramework.getModelManager().getModel().createRobotProject(getProject()).clear();

        LibspecsFolder.get(getProject()).removeNonSpecResources();
    }
}
