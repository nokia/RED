package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.io.IOException;

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

class RobotProjectValidator {

    private final IProject project;

    public RobotProjectValidator(final IProject project) {
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
        if (resource.getType() == IResource.FILE && resource.getFileExtension().equals("robot")) {
            try {
                if (removeMarkers) {
                    resource.deleteMarkers(RobotProblem.TYPE_ID, true, 1);
                }
                new RobotSuiteFileValidator((IFile) resource).validate(monitor);
            } catch (final IOException e) {
                throw new CoreException(Status.CANCEL_STATUS);
            }
        } else if (resource.getType() == IResource.FILE && resource.getName().equals("red.xml")
                && resource.getParent() == resource.getProject()) {
            if (removeMarkers) {
                resource.deleteMarkers(RobotProblem.TYPE_ID, true, 1);
            }
            new RobotProjectConfigFileValidator((IFile) resource).validate(monitor);
        }
    }
}
