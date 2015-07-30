package org.robotframework.ide.eclipse.main.plugin.project.build;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfig.ReferencedVariableFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigReader.CannotReadProjectConfigurationException;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemsReportingStrategy.ReportingInterruptedException;
import org.robotframework.ide.eclipse.main.plugin.project.build.causes.ProjectConfigurationProblem;

/**
 * @author mmarzec
 *
 */
public class RobotVariablesBuilder {

    private final IProject project;

    RobotVariablesBuilder(final IProject project) {
        this.project = project;
    }

    public Job createBuildJob(boolean rebuildNeeded) {
        if (rebuildNeeded) {

            return new Job("Building") {

                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    try {
                        buildVariables(project, monitor, new FatalProblemsReportingStrategy());
                        monitor.done();
                        return Status.OK_STATUS;
                    } catch (final ReportingInterruptedException e) {
                        return new Status(IStatus.CANCEL, RedPlugin.PLUGIN_ID, "Unable to build variables", e);
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

    private void buildVariables(final IProject project, final IProgressMonitor monitor,
            final ProblemsReportingStrategy reporter) {
        if (monitor.isCanceled()) {
            return;
        }
        final SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        subMonitor.beginTask("Building", 100);
        subMonitor.subTask("checking Robot execution environment");

        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        final RobotProjectConfig configuration = provideConfiguration(subMonitor.newChild(10), robotProject, reporter);
        if (subMonitor.isCanceled()) {
            return;
        }

        final RobotRuntimeEnvironment runtimeEnvironment = provideRuntimeEnvironment(subMonitor.newChild(10),
                robotProject, configuration, reporter);
        if (subMonitor.isCanceled()) {
            return;
        }

        List<ReferencedVariableFile> referencedVariableFiles = configuration.getReferencedVariableFiles();
        if (referencedVariableFiles != null) {
            subMonitor.setWorkRemaining(referencedVariableFiles.size());
            for (ReferencedVariableFile referencedVariableFile : referencedVariableFiles) {
                if (subMonitor.isCanceled()) {
                    return;
                }
                @SuppressWarnings("unchecked")
                Map<String, String> varsMap = (Map<String, String>) runtimeEnvironment.getVariablesFromFile(
                        referencedVariableFile.getPath(), referencedVariableFile.getArguments());
                if (varsMap != null && !varsMap.isEmpty()) {
                    referencedVariableFile.setVariables(varsMap);
                }
                subMonitor.worked(1);
            }
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
}
