/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.build;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy.ReportingInterruptedException;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProjectConfigurationProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.libs.LibrariesBuilder;
import org.robotframework.ide.eclipse.main.plugin.project.build.variables.VariablesBuilder;

public class RobotArtifactsBuilder {

    private final IProject project;

    public RobotArtifactsBuilder(final IProject project) {
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
                        buildArtifacts(project, monitor, new FatalProblemsReportingStrategy());

                        return Status.OK_STATUS;
                    } catch (final ReportingInterruptedException e) {
                        return new Status(IStatus.CANCEL, RedPlugin.PLUGIN_ID, "Unable to build libraries", e);
                    } finally {
                        monitor.done();
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

    private void buildArtifacts(final IProject project, final IProgressMonitor monitor,
            final ProblemsReportingStrategy reporter) {
        if (monitor.isCanceled()) {
            return;
        }
        final SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        subMonitor.beginTask("Building", 100);
        subMonitor.subTask("checking Robot execution environment");

        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        final SubMonitor configCreationMonitor = subMonitor.newChild(10);
        final RobotProjectConfig configuration = provideConfiguration(robotProject, reporter);
        configCreationMonitor.done();
        if (subMonitor.isCanceled()) {
            return;
        }

        final SubMonitor runtimeEnvCreationMonitor = subMonitor.newChild(10);
        final RobotRuntimeEnvironment runtimeEnvironment = provideRuntimeEnvironment(robotProject, configuration,
                reporter);
        runtimeEnvCreationMonitor.done();
        if (subMonitor.isCanceled()) {
            return;
        }

        new LibrariesBuilder().buildLibraries(robotProject, runtimeEnvironment, configuration, subMonitor.newChild(40));
        new VariablesBuilder().buildVariables(runtimeEnvironment, configuration, subMonitor.newChild(40));
    }

    private RobotProjectConfig provideConfiguration(final RobotProject robotProject,
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
        }
    }

    private RobotRuntimeEnvironment provideRuntimeEnvironment(final RobotProject robotProject,
            final RobotProjectConfig configuration, final ProblemsReportingStrategy reporter) {

        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        if (runtimeEnvironment == null) {
            final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.ENVIRONMENT_MISSING)
                    .formatMessageWith(configuration.providePythonLocation());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
        } else if (!runtimeEnvironment.isValidPythonInstallation()) {
            final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.ENVIRONMENT_NOT_A_PYTHON)
                    .formatMessageWith(runtimeEnvironment.getFile());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
        } else if (!runtimeEnvironment.hasRobotInstalled()) {
            final RobotProblem problem = RobotProblem.causedBy(ProjectConfigurationProblem.ENVIRONMENT_HAS_NO_ROBOT)
                    .formatMessageWith(runtimeEnvironment.getFile());
            reporter.handleProblem(problem, robotProject.getConfigurationFile(), 1);
        }
        return runtimeEnvironment;
    }
}
