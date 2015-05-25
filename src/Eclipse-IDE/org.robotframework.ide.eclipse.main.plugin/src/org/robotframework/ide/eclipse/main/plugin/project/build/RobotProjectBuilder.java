package org.robotframework.ide.eclipse.main.plugin.project.build;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.project.BuildpathFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectMetadata;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem.Cause;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem.Severity;

public class RobotProjectBuilder extends IncrementalProjectBuilder {

    private static final String LIBSPECS_FOLDER_NAME = "libspecs";

    @Override
    protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
        try {
            final IFolder libspecsFolder = getProject().getFolder(LIBSPECS_FOLDER_NAME);
            if (!libspecsFolder.exists()) {
                libspecsFolder.create(IResource.FORCE | IResource.DERIVED, true, null);
            }
            final boolean rebuildNeeded = shouldRebuildLibraries(kind);
            if (rebuildNeeded) {
                clearLinksToLibSources();
            }
            
            final IProgressMonitor progressMonitor = Job.getJobManager().createProgressGroup();

            final Job buildJob = createBuildJob(rebuildNeeded);
            final Job validationJob = new RobotProjectValidator().createValidationJob(getProject());
            try {
                final String projectPath = getProject().getFullPath().toString();

                progressMonitor.beginTask("Building and validating " + projectPath + " project", 200);
                buildJob.setProgressGroup(progressMonitor, 100);
                buildJob.schedule();

                validationJob.setProgressGroup(progressMonitor, 100);
                validationJob.schedule();

                monitor.subTask("waiting for project " + projectPath + " build end");
                buildJob.join();

                final QualifiedName key = new QualifiedName(RobotFramework.PLUGIN_ID, "buildResult");
                final RobotProjectMetadata metadata = (RobotProjectMetadata) buildJob.getProperty(key);
                if (metadata != null) {
                    new BuildpathFile(getProject()).write(metadata);
                    buildJob.setProperty(key, null);
                } else if (buildJob.getResult().getSeverity() == IStatus.ERROR) {
                    new BuildpathFile(getProject()).write(RobotProjectMetadata.create(null, null,
                            new ArrayList<String>()));
                    RobotFramework.getModelManager().getModel().createRobotProject(getProject()).clearMetadata();
                    if (libspecsFolder.exists()) {
                        libspecsFolder.delete(true, null);
                        return new IProject[0];
                    }
                }
                getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
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

    private boolean shouldRebuildLibraries(final int kind) {
        return kind == IncrementalProjectBuilder.FULL_BUILD
                || getDelta(getProject()).findMember(
                        getProject().getFile(RobotProjectNature.BUILDPATH_FILE).getProjectRelativePath()) != null;
    }

    private Job createBuildJob(final boolean buildingIsNeeded) {
        if (!buildingIsNeeded) {
            return new Job("Skipping build") {
                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    monitor.done();
                    return Status.OK_STATUS;
                }
            };
        } else {
            return new Job("Building") {
                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    try {
                        final RobotProjectMetadata metadata = buildLibrariesSpecs(getProject(), monitor);
                        setProperty(new QualifiedName(RobotFramework.PLUGIN_ID, "buildResult"), metadata);
                        return Status.OK_STATUS;
                    } catch (final UnableToBuildLibrariesException e) {
                        return new Status(IStatus.ERROR, RobotFramework.PLUGIN_ID, "Unable to build libraries", e);
                    }
                }
            };
        }
    }

    private RobotProjectMetadata buildLibrariesSpecs(final IProject project, final IProgressMonitor monitor) {
        if (monitor.isCanceled()) {
            return null;
        }
        final SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        subMonitor.beginTask("Building", 100);
        subMonitor.subTask("checking Robot execution environment");

        final RobotProjectMetadata projectMetadata = provideBuildpathsFile(subMonitor.newChild(10), project);
        if (subMonitor.isCanceled()) {
            return null;
        }

        final File savedLocation = providePythonInstallationDirectory(subMonitor.newChild(10), project, projectMetadata);
        if (subMonitor.isCanceled()) {
            return null;
        }

        final RobotRuntimeEnvironment runtimeEnvironment = provideRuntimeEnvironment(subMonitor.newChild(10), project,
                savedLocation);
        if (subMonitor.isCanceled()) {
            return null;
        }

        final List<String> librariesToRegenerate = provideLibrariesToRegenerate(subMonitor.newChild(20),
                projectMetadata, runtimeEnvironment);
        if (subMonitor.isCanceled()) {
            return null;
        }

        subMonitor.setWorkRemaining(librariesToRegenerate.size());
        final IFolder libspecsFolder = getProject().getFolder(LIBSPECS_FOLDER_NAME);
        for (final String libName : librariesToRegenerate) {
            if (subMonitor.isCanceled()) {
                return null;
            }
            subMonitor.subTask("generating libdoc for " + libName + " library");
            runtimeEnvironment.createLibdocForStdLibrary(libName,
                    libspecsFolder.getLocation().append(libName.toLowerCase() + ".libspec").toFile());
            subMonitor.worked(1);
        }

        RobotFramework.getModelManager().getModel().createRobotProject(project).clearMetadata();

        subMonitor.done();
        final RobotProjectMetadata result = RobotProjectMetadata.create(projectMetadata.getPythonLocation(),
                runtimeEnvironment.getVersion(), runtimeEnvironment.getStandardLibrariesNames());
        return result.equals(projectMetadata) ? null : result;
    }

    private RobotProjectMetadata provideBuildpathsFile(final SubMonitor subMonitor, final IProject project) {
        final RobotProjectMetadata projectMetadata = new BuildpathFile(project).read();
        if (projectMetadata != null) {
            subMonitor.done();
            return projectMetadata;
        }
        return RobotProjectMetadata.createEmpty();
    }

    private File providePythonInstallationDirectory(final SubMonitor subMonitor, final IProject project,
            final RobotProjectMetadata projectMetadata) {
        final File savedLocation = projectMetadata.getPythonLocation();
        if (savedLocation != null) {
            subMonitor.done();
            return savedLocation;
        }
        final RobotRuntimeEnvironment activeRobotInstallation = RobotFramework.getDefault()
                .getActiveRobotInstallation();
        if (activeRobotInstallation != null) {
            subMonitor.done();
            return activeRobotInstallation.getFile();
        }

        final RobotProblem problem = new RobotProblem(Severity.ERROR, Cause.NO_ACTIVE_INSTALLATION,
                "FATAL: the build paths definitions file does not specify any Python location and there is"
                        + " also no active installation specified in preferences. Fix this problem to build project");
        final IFile resource = project.getFile(RobotProjectNature.BUILDPATH_FILE);
        problem.createMarker(resource, 1);
        subMonitor.done();
        throw new UnableToBuildLibrariesException();
    }

    private RobotRuntimeEnvironment provideRuntimeEnvironment(final SubMonitor subMonitor, final IProject project,
            final File savedLocation) {
        final RobotRuntimeEnvironment runtimeEnvironment = RobotRuntimeEnvironment.create(savedLocation);
        if (!runtimeEnvironment.isValidPythonInstallation()) {
            final RobotProblem problem = new RobotProblem(Severity.ERROR, Cause.INVALID_PYTHON_DIRECTORY, "FATAL: "
                    + runtimeEnvironment.getFile()
                    + " is not a Python installation directory. Fix this problem to build project");
            final IFile resource = project.getFile(RobotProjectNature.BUILDPATH_FILE);
            problem.createMarker(resource, 1);
            subMonitor.done();
            throw new UnableToBuildLibrariesException();
        } else if (!runtimeEnvironment.hasRobotInstalled()) {
            final RobotProblem problem = new RobotProblem(Severity.ERROR, Cause.MISSING_ROBOT,
                    "FATAL: Python instalation " + runtimeEnvironment.getFile() + " has no Robot installed");
            final IFile resource = project.getFile(RobotProjectNature.BUILDPATH_FILE);
            problem.createMarker(resource, 1);
            subMonitor.done();
            throw new UnableToBuildLibrariesException();
        }
        subMonitor.done();
        return runtimeEnvironment;
    }

    private List<String> provideLibrariesToRegenerate(final SubMonitor subMonitor,
            final RobotProjectMetadata projectMetadata, final RobotRuntimeEnvironment runtimeEnvironment) {
        final List<String> librariesToRegenerate = runtimeEnvironment.getStandardLibrariesNames();
        if (projectMetadata.getVersion() != null
                && projectMetadata.getVersion().equals(runtimeEnvironment.getVersion())) {
            for (final String libName : newArrayList(librariesToRegenerate)) {
                if (projectMetadata.getStdLibrariesNames().contains(libName)) {
                    librariesToRegenerate.remove(libName);
                }
            }
        }
        subMonitor.done();
        return librariesToRegenerate;
    }

    @Override
    protected void clean(final IProgressMonitor monitor) throws CoreException {
        getProject().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        RobotFramework.getModelManager().getModel().createRobotProject(getProject()).clearMetadata();

        clearLinksToLibSources();
    }

    private void clearLinksToLibSources() throws CoreException {
        for (final IResource resource : getProject().getFolder("libspecs").members(IContainer.INCLUDE_HIDDEN)) {
            if (resource.exists() && resource.isHidden()) {
                resource.delete(true, null);
            }
        }
    }

    private static class UnableToBuildLibrariesException extends RuntimeException {

    }
}
