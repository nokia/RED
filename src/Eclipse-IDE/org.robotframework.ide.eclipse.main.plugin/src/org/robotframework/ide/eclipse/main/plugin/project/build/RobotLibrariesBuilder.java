package org.robotframework.ide.eclipse.main.plugin.project.build;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.LibraryType;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.RemoteLocation;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy.ReportingInterruptedException;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProjectConfigurationProblem;

public class RobotLibrariesBuilder {

    private final IProject project;

    RobotLibrariesBuilder(final IProject project) {
        this.project = project;
    }

    public Job createBuildJob(final boolean rebuildNeeded) {
        if (rebuildNeeded) {
            try {
                final LibspecsFolder libspecsFolder = LibspecsFolder.get(project);
                for (final IResource resource : libspecsFolder.members()) {
                    if (resource.getType() == IResource.FILE && resource.getName().startsWith("Remote_")) {
                        resource.delete(true, null);
                    }
                }
            } catch (final CoreException e) {
                // that's fine
            }

            return new Job("Building") {
                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    try {
                        try {
                            project.getFile(".project").deleteMarkers(RobotProblem.TYPE_ID, true,
                                    IResource.DEPTH_INFINITE);
                            project.getFile(RobotProjectConfig.FILENAME).deleteMarkers(RobotProblem.TYPE_ID, true,
                                    IResource.DEPTH_INFINITE);
                        } catch (final CoreException e) {
                            // that's fine, lets try to build project
                        }
                        buildLibrariesSpecs(project, monitor, new FatalProblemsReportingStrategy());
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
            final ProblemsReportingStrategy reporter) {
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

        final List<ILibdocGenerator> libdocGenerators = newArrayList();

        final LibspecsFolder libspecsFolder = LibspecsFolder.get(project);
        libdocGenerators.addAll(getStandardLibrariesToRecreate(runtimeEnvironment, libspecsFolder));
        libdocGenerators.addAll(getReferencedPythonLibrariesToRecreate(configuration, libspecsFolder));
        libdocGenerators.addAll(getReferencedJavaLibrariesToRecreate(configuration, libspecsFolder));
        libdocGenerators.addAll(getRemoteLibrariesToRecreate(configuration, libspecsFolder));

        subMonitor.setWorkRemaining(libdocGenerators.size());
        for (final ILibdocGenerator generator : libdocGenerators) {
            if (subMonitor.isCanceled()) {
                return;
            }
            subMonitor.subTask(generator.getMessage());
            generator.generateLibdoc(runtimeEnvironment);
            subMonitor.worked(1);
        }
    }

    private RobotProjectConfig provideConfiguration(final IProgressMonitor monitor, final RobotProject robotProject,
            final ProblemsReportingStrategy reporter) {
        try {
            if (!robotProject.getConfigurationFile().exists()) {
                final RobotProblem problem = RobotProblem
                        .causedBy(ProjectConfigurationProblem.CONFIG_FILE_MISSING);
                reporter.handleProblem(problem, robotProject.getFile(".project"), 1);
            }
            return new RobotProjectConfigReader().readConfiguration(robotProject);
        } catch (final CannotReadProjectConfigurationException e) {
            final RobotProblem problem = RobotProblem.causedBy(
                    ProjectConfigurationProblem.CONFIG_FILE_READING_PROBLEM)
                    .formatMessageWith(e.getMessage());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), e.getLineNumber());
            return null;
        } finally {
            monitor.done();
        }
    }

    private RobotRuntimeEnvironment provideRuntimeEnvironment(final IProgressMonitor monitor,
            final RobotProject robotProject, final RobotProjectConfig configuration,
            final ProblemsReportingStrategy reporter) {
        try {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            if (runtimeEnvironment == null) {
                final RobotProblem problem = RobotProblem.causedBy(
                        ProjectConfigurationProblem.ENVIRONMENT_MISSING).formatMessageWith(
                        configuration.providePythonLocation());
                reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
            } else if (!runtimeEnvironment.isValidPythonInstallation()) {
                final RobotProblem problem = RobotProblem.causedBy(
                        ProjectConfigurationProblem.ENVIRONMENT_NOT_A_PYTHON).formatMessageWith(
                        runtimeEnvironment.getFile());
                reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
            } else if (!runtimeEnvironment.hasRobotInstalled()) {
                final RobotProblem problem = RobotProblem.causedBy(
                        ProjectConfigurationProblem.ENVIRONMENT_HAS_NO_ROBOT)
                        .formatMessageWith(runtimeEnvironment.getFile());
                reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
            }
            return runtimeEnvironment;
        } finally {
            monitor.done();
        }
    }

    private List<ILibdocGenerator> getStandardLibrariesToRecreate(final RobotRuntimeEnvironment runtimeEnvironment,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = newArrayList();
        final List<String> stdLibs = runtimeEnvironment.getStandardLibrariesNames();
        try {
            final List<IFile> toRecr = libspecsFolder.collectSpecsWithDifferentVersion(stdLibs,
                    runtimeEnvironment.getVersion());
            for (final IFile specToRecreate : toRecr) {
                generators.add(new StandardLibraryLibdocGenerator(specToRecreate));
            }
        } catch (final CoreException e) {
            // FIXME : handle this
            e.printStackTrace();
        }
        return generators;
    }
    
    private List<ILibdocGenerator> getReferencedPythonLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = newArrayList();

        for (final ReferencedLibrary lib : configuration.getLibraries()) {
            if (lib.provideType() == LibraryType.PYTHON) {
                final String libName = lib.getName();
                final IFile specFile = libspecsFolder.getSpecFile(libName);
                if (!specFile.exists()) {
                    final String libPath = lib.getPath();
                    generators.add(new PythonLibraryLibdocGenerator(libName, libPath, specFile));
                }
            }
        }
        return generators;
    }

    private List<ILibdocGenerator> getReferencedJavaLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = newArrayList();

        for (final ReferencedLibrary lib : configuration.getLibraries()) {
            if (lib.provideType() == LibraryType.JAVA) {
                final String libName = lib.getName();
                final IFile specFile = libspecsFolder.getSpecFile(libName);
                if (!specFile.exists()) {
                    final String jarPath = lib.getPath();
                    generators.add(new JavaLibraryLibdocGenerator(libName, jarPath, specFile));
                }
            }
        }
        return generators;
    }

    private Collection<? extends ILibdocGenerator> getRemoteLibrariesToRecreate(final RobotProjectConfig configuration,
            final LibspecsFolder libspecsFolder) {
        final List<ILibdocGenerator> generators = newArrayList();

        for (final RemoteLocation location : configuration.getRemoteLocations()) {
            final IFile specFile = libspecsFolder.getSpecFile(location.createLibspecFileName());

            if (!specFile.exists()) {
                generators.add(new RemoteLibraryLibdocGenerator(location.getUriAddress(), specFile));
            }
        }
        return generators;
    }
}
