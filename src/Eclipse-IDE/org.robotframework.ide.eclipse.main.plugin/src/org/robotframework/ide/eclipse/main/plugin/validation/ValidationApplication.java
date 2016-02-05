/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.validation;

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.robotframework.ide.eclipse.main.plugin.model.LibspecsFolder;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.ProblemPosition;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProjectBuilder;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.io.Files;


public class ValidationApplication implements IApplication {

    private final Logger logger = new Logger();

    @Override
    public Object start(final IApplicationContext context) throws Exception {
        try {
            System.out.println("# RED projects validator\n");

            final List<String> args = newArrayList((String[]) context.getArguments().get("application.args"));
            final ProvidedArguments arguments = parseArguments(args);
            importNeededProjects(arguments.projectsToImport);
            runValidation(arguments);

            return IApplication.EXIT_OK;
        } catch (final InvalidArgumentsProvidedException e) {
            e.explainUsage();
            return -100;
        } catch (final Exception e) {
            e.printStackTrace();
            return -200;
        }
    }

    private ProvidedArguments parseArguments(final List<String> passedArgs) {
        if (passedArgs.isEmpty()) {
            throw new InvalidArgumentsProvidedException("There were no arguments provided");
        }
        final ProvidedArguments args = new ProvidedArguments();
        while (!passedArgs.isEmpty()) {
            if (passedArgs.get(0).equals("-import")) {
                parseImportArgument(passedArgs, args);
            } else if (passedArgs.get(0).equals("-projects")) {
                parseProjectsArgument(passedArgs, args);
            } else if (passedArgs.get(0).equals("-report")) {
                parseReportArgument(passedArgs, args);
            } else {
                throw new InvalidArgumentsProvidedException("Unexpected argument " + passedArgs.get(0) + " provided");
            }
        }
        return args;
    }

    private void parseImportArgument(final List<String> passedArgs, final ProvidedArguments args) {
        passedArgs.remove(0);
        while (!passedArgs.isEmpty() && !isSwitch(passedArgs.get(0))) {
            args.projectsToImport.add(passedArgs.remove(0));
        }
    }

    private void parseProjectsArgument(final List<String> passedArgs, final ProvidedArguments args) {
        passedArgs.remove(0);
        if (passedArgs.isEmpty() || isSwitch(passedArgs.get(0))) {
            throw new InvalidArgumentsProvidedException("No projects were specified after -projects switch");
        }
        args.projectNames = Splitter.on(";").splitToList(passedArgs.remove(0));
    }

    private void parseReportArgument(final List<String> passedArgs, final ProvidedArguments args) {
        passedArgs.remove(0);
        if (passedArgs.isEmpty() || isSwitch(passedArgs.get(0))) {
            throw new InvalidArgumentsProvidedException("No projects were specified after -report switch");
        }
        args.reportFilepath = passedArgs.remove(0);
    }

    private boolean isSwitch(final String arg) {
        return "-projects".equals(arg) || "-report".equals(arg);
    }

    private void importNeededProjects(final List<String> projectsToImport)
            throws InvocationTargetException, InterruptedException, CoreException, IOException {

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IPath wsLocation = workspace.getRoot().getLocation();

        for (final String path : projectsToImport) {
            logger.log("Importing project: " + path);

            final IPath originalLocation = new Path(path);
            final IPath wsProjectLocation = wsLocation.append(originalLocation.lastSegment());

            final File from = new File(originalLocation.toFile().toURI());
            final File to = wsProjectLocation.toFile();
            if (from.equals(to)) {
                logger.log("WARNING: project " + path + " is already in the workspace");
            } else {
                copyProjectFiles(from, to);
            }

            final IProjectDescription description = workspace
                    .loadProjectDescription(wsProjectLocation.append(".project"));
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
            if (!project.exists()) {
                project.create(description, null);
            }
            project.open(null);

            logger.log("Project: " + path + " was succesfully imported");
        }
    }

    private void copyProjectFiles(final File sourceLocation, final File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            final String[] children = sourceLocation.list();
            for (int i = 0; i < children.length; i++) {
                copyProjectFiles(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
            }
        } else {
            Files.copy(sourceLocation, targetLocation);
        }
    }

    private void runValidation(final ProvidedArguments arguments) throws CoreException {

        final Table<IPath, ProblemPosition, RobotProblem> allProblems = HashBasedTable.create();
        for (final String projectName : arguments.getProjectNames()) {
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

                final ProblemsGatheringReportingStrategy reporter = ProblemsGatheringReportingStrategy.reportOnly();
                final ProblemsGatheringReportingStrategy fatalReporter = ProblemsGatheringReportingStrategy
                        .reportAndPanic();
                final RobotProjectBuilder builder = new RobotProjectBuilder(reporter, fatalReporter);

                final long start = System.currentTimeMillis();
                builder.build(IncrementalProjectBuilder.FULL_BUILD, robotProject, new NullProgressMonitor());
                final long end = System.currentTimeMillis();
                final double duration = (end - start) / 1000.0;

                logger.log(String.format("Project %s validation has FINISHED (took %.3f seconds and found %d problems)",
                        projectName, duration, reporter.getNumberOfProblems() + fatalReporter.getNumberOfProblems()));

                allProblems.putAll(fatalReporter.getProblems());
                allProblems.putAll(reporter.getProblems());
            }
        }

        final File outFile = new File(arguments.getReportFilePath());
        generateFile(outFile, allProblems);
    }

    private void generateFile(final File file, final Table<IPath, ProblemPosition, RobotProblem> problems) {
        logger.log("Generating report file '" + file.getAbsolutePath() + "'");
        try (ReportWithCheckstyleFormat checkstyleReporter = new ReportWithCheckstyleFormat(file)) {
            checkstyleReporter.writeHeader();
            checkstyleReporter.writeEntries(problems);
            checkstyleReporter.writeFooter();
            logger.log("Report file '" + file.getAbsolutePath() + "' has been generated");
        } catch (final IOException e) {
            logger.logError("Unable to create report file '" + file.getAbsolutePath() + "'. Reason: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        // nothing to do
    }

    private static class InvalidArgumentsProvidedException extends RuntimeException {

        InvalidArgumentsProvidedException(final String msg) {
            super(msg);
        }

        public void explainUsage() {
            final StringBuilder explanation = new StringBuilder();
            explanation.append("Invalid application arguments were provided: ");
            explanation.append(getMessage() + "\n");
            explanation.append("Application usage:\n");
            explanation.append(Strings.padEnd("\t-projects <projects>", 30, ' '));
            explanation.append("[REQUIRED] where <projects> are semicolon-separated names of projects to validate\n");
            explanation.append(Strings.padEnd("\t-report <file>", 30, ' '));
            explanation.append("[OPTIONAL] <file> path to the report file which should be written\n");
            explanation.append(Strings.padEnd("\t-import <path1> ... <pathn>", 30, ' '));
            explanation.append("[Optional] paths to projects which should be imported to workspace\n");
            explanation.append("\n");
            System.err.println(explanation.toString());
        }
    }

    private static class ProvidedArguments {

        private final List<String> projectsToImport = new ArrayList<>();

        private String reportFilepath;

        private List<String> projectNames;

        public String getReportFilePath() {
            return reportFilepath == null ? "report.xml" : reportFilepath;
        }

        public List<String> getProjectNames() {
            return projectNames;
        }
    }
}
