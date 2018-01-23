/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProjectBuilder;
import org.robotframework.ide.eclipse.main.plugin.validation.ArgumentsParser.InvalidArgumentsProvidedException;
import org.robotframework.ide.eclipse.main.plugin.validation.ArgumentsParser.ProvidedArguments;
import org.robotframework.ide.eclipse.main.plugin.validation.ProblemsReportingStrategyFactory.HeadlessValidationReportingStrategy;

public class ValidationApplication implements IApplication {

    private final Logger logger = new Logger();

    @Override
    public Object start(final IApplicationContext context) throws Exception {
        // this is necessary to load preferences before start or sometimes error stack
        // would appear due to using prefs in another thread before fully loaded
        RedPlugin.getDefault().getPreferences();
        
        IWorkspaceDescription description = ResourcesPlugin.getWorkspace().getDescription();
        final boolean isAutobuildingEnabled = description.isAutoBuilding();
        description.setAutoBuilding(false);
        ResourcesPlugin.getWorkspace().setDescription(description);
        try {
            System.out.println("# RED projects validator\n");

            final List<String> args = newArrayList((String[]) context.getArguments().get("application.args"));
            final ProvidedArguments arguments = new ArgumentsParser().parseArguments(args);

            description.setAutoBuilding(false);
            ResourcesPlugin.getWorkspace().setDescription(description);

            new ProjectsImporter(logger).importNeededProjects(arguments.getProjectPathsToImport());

            final String reportFilepath = arguments.getReportFilePath();
            final HeadlessValidationReportingStrategy reporter = ProblemsReportingStrategyFactory
                    .checkstyleReporter(reportFilepath, logger);
            final HeadlessValidationReportingStrategy fatalReporter = ProblemsReportingStrategyFactory
                    .checkstylePanicReporter(reportFilepath, logger);

            runValidation(arguments, reporter, fatalReporter);

            fatalReporter.finishReporting();
            reporter.finishReporting();

            return IApplication.EXIT_OK;
        } catch (final InvalidArgumentsProvidedException e) {
            e.explainUsage();
            return -100;
        } catch (final Exception e) {
            e.printStackTrace();
            return -200;
        } finally {
            description = ResourcesPlugin.getWorkspace().getDescription();
            description.setAutoBuilding(isAutobuildingEnabled);
            ResourcesPlugin.getWorkspace().setDescription(description);
        }
    }

    private void runValidation(final ProvidedArguments arguments, final HeadlessValidationReportingStrategy reporter,
            final HeadlessValidationReportingStrategy fatalReporter) throws CoreException {

        for (final String projectName : arguments.getProjectNamesToValidate()) {
            logger.log("Project '" + projectName + "' validation started");

            final IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
            final IProject project = wsRoot.getProject(projectName);

            if (!project.exists()) {
                logger.log("Project '" + projectName + "' validation was SKIPPED (does not exist in workspace)");
            } else if (!project.isOpen()) {
                logger.log("Project '" + projectName + "' validation was SKIPPED (is closed)");
            } else {
                final RobotProject robotProject = new RobotModel().createRobotProject(project);
                robotProject.clearConfiguration();
                LibspecsFolder.get(project.getProject()).removeNonSpecResources();

                final RobotProjectBuilder builder = new RobotProjectBuilder(reporter, fatalReporter, logger);

                fatalReporter.projectValidationStarted(projectName);
                reporter.projectValidationStarted(projectName);

                builder.build(IncrementalProjectBuilder.FULL_BUILD, robotProject, new NullProgressMonitor());

                fatalReporter.projectValidationFinished(projectName);
                reporter.projectValidationFinished(projectName);
            }
        }
    }

    @Override
    public void stop() {
        // nothing to do
    }
}
