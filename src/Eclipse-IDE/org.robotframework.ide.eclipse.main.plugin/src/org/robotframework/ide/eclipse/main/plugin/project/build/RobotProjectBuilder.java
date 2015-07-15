package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;

public class RobotProjectBuilder extends IncrementalProjectBuilder {

    @Override
    protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
        try {
            final IProject project = getProject();

            final LibspecsFolder libspecsFolder = LibspecsFolder.createIfNeeded(getProject());
            final boolean rebuildNeeded = libspecsFolder.shouldRegenerateLibspecs(getDelta(project), kind);

            final Job buildJob = new RobotLibrariesBuilder(getProject()).createBuildJob(rebuildNeeded);
            final Job validationJob = new RobotProjectValidator(getProject()).createValidationJob(getDelta(project),
                    kind);
            final IProgressMonitor progressMonitor = Job.getJobManager().createProgressGroup();
            try {
                final String projectPath = project.getFullPath().toString();

                progressMonitor.beginTask("Building and validating " + projectPath + " project", 200);

                buildJob.setProgressGroup(progressMonitor, 100);
                validationJob.setProgressGroup(progressMonitor, 100);

                monitor.subTask("waiting for project " + projectPath + " build end");
                buildJob.schedule();
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
                    validationJob.schedule();
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

    @Override
    protected void clean(final IProgressMonitor monitor) throws CoreException {
        getProject().deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
        RobotFramework.getModelManager().getModel().createRobotProject(getProject()).clear();

        LibspecsFolder.get(getProject()).removeNonSpecResources();
    }
}
