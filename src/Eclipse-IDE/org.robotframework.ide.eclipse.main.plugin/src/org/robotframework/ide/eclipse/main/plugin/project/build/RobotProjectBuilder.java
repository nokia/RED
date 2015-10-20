/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
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
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;

public class RobotProjectBuilder extends IncrementalProjectBuilder {

    @Override
    protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
        try {
            final IProject project = getProject();

            final LibspecsFolder libspecsFolder = LibspecsFolder.createIfNeeded(getProject());
            final boolean rebuildNeeded = libspecsFolder.shouldRegenerateLibspecs(getDelta(project), kind);

            final Job buildJob = new RobotArtifactsBuilder(getProject()).createBuildJob(rebuildNeeded);
            final Job validationJob = new RobotArtifactsValidator(getProject()).createValidationJob(getDelta(project),
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

                if (buildJob.getResult().getSeverity() == IStatus.CANCEL
                        || buildJob.getResult().getSeverity() == IStatus.ERROR) {
                    RedPlugin.getModelManager().getModel().createRobotProject(project).clearConfiguration();
                    if (libspecsFolder.exists()) {
                        libspecsFolder.remove();
                        return new IProject[0];
                    }
                }
                RedPlugin.getModelManager().getModel().createRobotProject(project).clearConfiguration();
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
        RedPlugin.getModelManager().getModel().createRobotProject(getProject()).clearConfiguration();

        LibspecsFolder.get(getProject()).removeNonSpecResources();
    }
}
