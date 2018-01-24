/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfig;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotArtifactsValidator.ModelUnitValidatorConfigFactory;

public class RobotProjectBuilder extends IncrementalProjectBuilder {

    private final BuildLogger logger;
    private final ProblemsReportingStrategy reporter;
    private final ProblemsReportingStrategy fatalReporter;

    public RobotProjectBuilder() {
        this(ProblemsReportingStrategy.reportOnly(), ProblemsReportingStrategy.reportAndPanic(), new BuildLogger());
    }

    public RobotProjectBuilder(final ProblemsReportingStrategy reporter, final ProblemsReportingStrategy fatalReporter,
            final BuildLogger logger) {
        this.reporter = reporter;
        this.fatalReporter = fatalReporter;
        this.logger = logger;
    }

    @Override
    protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor)
            throws CoreException {
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(getProject());
        build(kind, robotProject, monitor);
        return null;
    }

    public void build(final int kind, final RobotProject robotProject, final IProgressMonitor monitor)
            throws CoreException {
        final boolean isValidationTurnedOff = RedPlugin.getDefault().getPreferences().isValidationTurnedOff();
        try {
            final IProject project = robotProject.getProject();
            final LibspecsFolder libspecsFolder = LibspecsFolder.createIfNeeded(project);
            final IResourceDelta delta = getDelta(project);
            final boolean rebuildNeeded = libspecsFolder.shouldRegenerateLibspecs(delta, kind);

            final Job buildJob = new RobotArtifactsBuilder(project, logger).createBuildJob(rebuildNeeded,
                    fatalReporter);
            Job validationJob = null;
            if (!isValidationTurnedOff) {
                final ModelUnitValidatorConfig validatorConfig = ModelUnitValidatorConfigFactory.create(project, delta,
                        kind, reporter);
                validationJob = new RobotArtifactsValidator(project, logger).createValidationJob(buildJob,
                        validatorConfig);
            }
            try {
                final String projectPath = project.getFullPath().toString();

                monitor.subTask("waiting for project " + projectPath + " build end");
                buildJob.schedule();
                if (!isValidationTurnedOff) {
                    validationJob.schedule();
                }
                buildJob.join();

                if (buildJob.getResult().getSeverity() == IStatus.CANCEL
                        || buildJob.getResult().getSeverity() == IStatus.ERROR) {
                    robotProject.clearConfiguration();
                    if (libspecsFolder.exists()) {
                        libspecsFolder.remove();
                        if (!isValidationTurnedOff) {
                            validationJob.cancel();
                        }
                        return;
                    }
                }
                robotProject.clearConfiguration();
                project.refreshLocal(IResource.DEPTH_INFINITE, null);

                if (!monitor.isCanceled()) {
                    monitor.subTask("waiting for project " + projectPath + " validation end");
                    if (!isValidationTurnedOff) {
                        validationJob.join();
                    }
                }
            } catch (final InterruptedException e) {
                throw new CoreException(Status.CANCEL_STATUS);
            }
        } finally {
            monitor.worked(1);
        }
    }

    @Override
    protected void clean(final IProgressMonitor monitor) throws CoreException {
        clean(RedPlugin.getModelManager().createProject(getProject()));
    }

    public static void clean(final RobotProject project) throws CoreException {
        project.getProject().deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
        project.clearConfiguration();
        project.clearKwSources();

        LibspecsFolder.get(project.getProject()).removeNonSpecResources();
    }
}
