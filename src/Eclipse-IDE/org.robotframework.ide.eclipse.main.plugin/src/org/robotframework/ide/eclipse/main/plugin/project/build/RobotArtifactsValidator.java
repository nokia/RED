package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.project.RobotSuiteFileDescriber;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotInitFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotProjectConfigFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotResourceFileValidator;
import org.robotframework.ide.eclipse.main.plugin.project.build.validation.RobotSuiteFileValidator;

public class RobotArtifactsValidator {

    private final IProject project;

    public RobotArtifactsValidator(final IProject project) {
        this.project = project;
    }

    public Job createValidationJob(final IResourceDelta delta, final int kind) {
        return new Job("Validating") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                try {
                    if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
                        validateWholeProject(monitor);
                    } else if (delta != null) {
                        validateChangedFiles(delta, monitor);
                    }
                    return Status.OK_STATUS;
                } catch (final CoreException e) {
                    RedPlugin.logError("Project validation was corrupted", e);
                    return Status.CANCEL_STATUS;
                } finally {
                    monitor.done();
                }
            }
        };
    }

    private void validateWholeProject(final IProgressMonitor monitor) throws CoreException {
        project.deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);

        project.accept(new IResourceVisitor() {
            @Override
            public boolean visit(final IResource resource) throws CoreException {
                validateResource(resource, monitor, false);
                return true;
            }
        });
    }

    private void validateChangedFiles(final IResourceDelta delta, final IProgressMonitor monitor) throws CoreException {
        delta.accept(new IResourceDeltaVisitor() {

            @Override
            public boolean visit(final IResourceDelta delta) throws CoreException {
                if (delta.getKind() != IResourceDelta.REMOVED && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                    validateResource(delta.getResource(), monitor, true);
                }
                return true;
            }
        });
    }

    private void validateResource(final IResource resource, final IProgressMonitor monitor, final boolean removeMarkers)
            throws CoreException {
        if (resource.getType() != IResource.FILE) {
            return;
        }

        final IFile file = (IFile) resource;
        RobotFileValidator validator = null;
        if (RobotSuiteFileDescriber.isSuiteFile(file)) {
            validator = new RobotSuiteFileValidator(file);
        } else if (RobotSuiteFileDescriber.isResourceFile(file)) {
            validator = new RobotResourceFileValidator(file);
        } else if (RobotSuiteFileDescriber.isInitializationFile(file)) {
            validator = new RobotInitFileValidator(file);
        } else if (file.getName().equals("red.xml") && file.getParent() == file.getProject()) {
            validator = new RobotProjectConfigFileValidator(file);
        }

        if (validator != null) {
            if (removeMarkers) {
                file.deleteMarkers(RobotProblem.TYPE_ID, true, 1);
            }
            validator.validate(monitor);
        }
    }

    public interface RobotFileValidator {

        void validate(IProgressMonitor monitor) throws CoreException;
    }
}
