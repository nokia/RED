/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;

/**
 * @author Michal Anglart
 */
public class RunCommandLineCallBuilder {

    public static interface IRunCommandLineBuilder {

        IRunCommandLineBuilder withExecutableFile(final String executableFilePath);

        IRunCommandLineBuilder addUserArgumentsForExecutableFile(final String arguments);

        IRunCommandLineBuilder useSingleRobotCommandLineArg(boolean shouldUseSingleRobotCommandLineArg);

        IRunCommandLineBuilder addUserArgumentsForInterpreter(final String arguments);

        IRunCommandLineBuilder enableDryRun();

        IRunCommandLineBuilder useArgumentFile(boolean shouldUseArgumentFile);

        IRunCommandLineBuilder addLocationsToPythonPath(final Collection<String> pythonPathLocations);

        IRunCommandLineBuilder addLocationsToClassPath(final Collection<String> classPathLocations);

        IRunCommandLineBuilder addVariableFiles(final Collection<String> varFiles);

        IRunCommandLineBuilder suitesToRun(final Collection<String> suites);

        IRunCommandLineBuilder testsToRun(final Collection<String> tests);

        IRunCommandLineBuilder includeTags(final Collection<String> tags);

        IRunCommandLineBuilder excludeTags(final Collection<String> tags);

        IRunCommandLineBuilder addUserArgumentsForRobot(final String arguments);

        IRunCommandLineBuilder withProject(final File project);

        IRunCommandLineBuilder withAdditionalProjectsLocations(final Collection<File> additionalProjectsLocations);

        RunCommandLine build() throws IOException;
    }

    private static class Builder implements IRunCommandLineBuilder {

        private final SuiteExecutor executor;

        private final String executorPath;

        private final int listenerPort;

        private boolean enableDryRun = false;

        private boolean useArgumentFile = false;

        private final List<String> pythonPath = new ArrayList<>();

        private final List<String> classPath = new ArrayList<>();

        private final List<String> variableFiles = new ArrayList<>();

        private final List<String> suitesToRun = new ArrayList<>();

        private final List<String> testsToRun = new ArrayList<>();

        private final List<String> tagsToInclude = new ArrayList<>();

        private final List<String> tagsToExclude = new ArrayList<>();

        private File project = null;

        private final List<File> additionalProjectsLocations = new ArrayList<>();

        private String robotUserArgs = "";

        private String interpreterUserArgs = "";

        private String executableFilePath = "";

        private String executableFileArgs = "";

        private boolean useSingleRobotCommandLineArg = false;

        private Builder(final SuiteExecutor executor, final String executorPath, final int listenerPort) {
            this.executor = executor;
            this.executorPath = executorPath;
            this.listenerPort = listenerPort;
        }

        @Override
        public IRunCommandLineBuilder withExecutableFile(final String executableFilePath) {
            this.executableFilePath = executableFilePath;
            return this;
        }

        @Override
        public IRunCommandLineBuilder addUserArgumentsForExecutableFile(final String arguments) {
            this.executableFileArgs = arguments.trim();
            return this;
        }

        @Override
        public IRunCommandLineBuilder useSingleRobotCommandLineArg(final boolean shouldUseSingleRobotCommandLineArg) {
            this.useSingleRobotCommandLineArg = shouldUseSingleRobotCommandLineArg;
            return this;
        }

        @Override
        public IRunCommandLineBuilder addUserArgumentsForInterpreter(final String arguments) {
            this.interpreterUserArgs = arguments.trim();
            return this;
        }

        @Override
        public IRunCommandLineBuilder enableDryRun() {
            this.enableDryRun = true;
            return this;
        }

        @Override
        public IRunCommandLineBuilder useArgumentFile(final boolean shouldUseArgumentFile) {
            this.useArgumentFile = shouldUseArgumentFile;
            return this;
        }

        @Override
        public IRunCommandLineBuilder addLocationsToPythonPath(final Collection<String> pythonPathLocations) {
            pythonPath.addAll(pythonPathLocations);
            return this;
        }

        @Override
        public IRunCommandLineBuilder addLocationsToClassPath(final Collection<String> classPathLocations) {
            classPath.addAll(classPathLocations);
            return this;
        }

        @Override
        public IRunCommandLineBuilder addVariableFiles(final Collection<String> varFiles) {
            variableFiles.addAll(varFiles);
            return this;
        }

        @Override
        public IRunCommandLineBuilder suitesToRun(final Collection<String> suites) {
            suitesToRun.addAll(suites);
            return this;
        }

        @Override
        public IRunCommandLineBuilder testsToRun(final Collection<String> tests) {
            testsToRun.addAll(tests);
            return this;
        }

        @Override
        public IRunCommandLineBuilder includeTags(final Collection<String> tags) {
            tagsToInclude.addAll(tags);
            return this;
        }

        @Override
        public IRunCommandLineBuilder excludeTags(final Collection<String> tags) {
            tagsToExclude.addAll(tags);
            return this;
        }

        @Override
        public IRunCommandLineBuilder addUserArgumentsForRobot(final String arguments) {
            this.robotUserArgs = arguments.trim();
            return this;
        }

        @Override
        public IRunCommandLineBuilder withProject(final File project) {
            this.project = project;
            return this;
        }

        @Override
        public IRunCommandLineBuilder withAdditionalProjectsLocations(
                final Collection<File> additionalProjectsLocations) {
            this.additionalProjectsLocations.addAll(additionalProjectsLocations);
            return this;
        }

        @Override
        public RunCommandLine build() throws IOException {
            final RunCommandLine robotRunCommandLine = buildRobotRunCommandLine();

            if (!executableFilePath.isEmpty()) {
                return buildRunCommandLineWrappedWithExecutable(robotRunCommandLine);
            }

            return robotRunCommandLine;
        }

        private RunCommandLine buildRunCommandLineWrappedWithExecutable(final RunCommandLine robotRunCommandLine) {
            final List<String> cmdLine = new ArrayList<>();
            cmdLine.add(executableFilePath);
            if (!executableFileArgs.isEmpty()) {
                cmdLine.addAll(ArgumentsConverter.parseArguments(executableFileArgs));
            }

            if (useSingleRobotCommandLineArg) {
                cmdLine.add(String.join(" ", robotRunCommandLine.getCommandLine()));
            } else {
                cmdLine.addAll(Arrays.asList(robotRunCommandLine.getCommandLine()));
            }

            if (robotRunCommandLine.getArgumentFile().isPresent()) {
                return new RunCommandLine(cmdLine, robotRunCommandLine.getArgumentFile().get());
            }
            return new RunCommandLine(cmdLine, null);
        }

        private RunCommandLine buildRobotRunCommandLine() throws IOException {
            final List<String> cmdLine = new ArrayList<>();
            cmdLine.add(executorPath);
            if (executor == SuiteExecutor.Jython) {
                final String additionalPythonPathLocationForJython = extractAdditionalPythonPathLocationForJython();
                if (!additionalPythonPathLocationForJython.isEmpty()) {
                    cmdLine.add(additionalPythonPathLocationForJython);
                }
                cmdLine.add("-J-cp");
                cmdLine.add(classPath());
            }
            if (!interpreterUserArgs.isEmpty()) {
                cmdLine.addAll(ArgumentsConverter.parseArguments(interpreterUserArgs));
            }
            cmdLine.add("-m");
            cmdLine.add("robot.run");

            cmdLine.add("--listener");
            cmdLine.add(RobotRuntimeEnvironment.copyScriptFile("TestRunnerAgent.py").toPath() + ":" + listenerPort);

            ArgumentsFile argumentsFile = null;
            if (useArgumentFile) {
                argumentsFile = createArgumentsFile();
                cmdLine.add("--argumentfile");
                cmdLine.add(argumentsFile.writeToTemporaryOrUseAlreadyExisting().toPath().toString());
            } else {
                cmdLine.addAll(createInlinedArguments());
            }

            if (project != null) {
                cmdLine.add(project.getAbsolutePath());
            }
            for (final File projectLocation : additionalProjectsLocations) {
                cmdLine.add(projectLocation.getAbsolutePath());
            }
            return new RunCommandLine(cmdLine, argumentsFile);
        }

        private List<String> createInlinedArguments() throws IOException {
            final List<String> robotArgs = new ArrayList<>();
            if (!pythonPath.isEmpty()) {
                robotArgs.add("-P");
                robotArgs.add(pythonPath());
            }
            for (final String varFile : variableFiles) {
                robotArgs.add("-V");
                robotArgs.add(varFile);
            }
            for (final String tagToInclude : tagsToInclude) {
                robotArgs.add("-i");
                robotArgs.add(tagToInclude);
            }
            for (final String tagToExclude : tagsToExclude) {
                robotArgs.add("-e");
                robotArgs.add(tagToExclude);
            }
            if (enableDryRun) {
                robotArgs.add("--prerunmodifier");
                robotArgs.add(RobotRuntimeEnvironment.copyScriptFile("SuiteVisitorImportProxy.py").toPath().toString());
                robotArgs.add("--runemptysuite");
                robotArgs.add("--dryrun");
                robotArgs.add("--output");
                robotArgs.add("NONE");
                robotArgs.add("--report");
                robotArgs.add("NONE");
                robotArgs.add("--log");
                robotArgs.add("NONE");
            }
            for (final String suite : suitesToRun) {
                robotArgs.add("-s");
                robotArgs.add(suite);
            }
            for (final String test : testsToRun) {
                robotArgs.add("-t");
                robotArgs.add(test);
            }
            if (!robotUserArgs.isEmpty()) {
                robotArgs.addAll(ArgumentsConverter.parseArguments(robotUserArgs));
            }
            return robotArgs;
        }

        private ArgumentsFile createArgumentsFile() throws IOException {
            final ArgumentsFile argumentsFile = new ArgumentsFile();
            argumentsFile.addCommentLine("arguments automatically generated");
            if (!pythonPath.isEmpty()) {
                argumentsFile.addLine("--pythonpath", pythonPath());
            }
            for (final String varFile : variableFiles) {
                argumentsFile.addLine("--variablefile", varFile);
            }
            for (final String tagToInclude : tagsToInclude) {
                argumentsFile.addLine("--include", tagToInclude);
            }
            for (final String tagToExclude : tagsToExclude) {
                argumentsFile.addLine("--exclude", tagToExclude);
            }
            if (enableDryRun) {
                argumentsFile.addLine("--prerunmodifier",
                        RobotRuntimeEnvironment.copyScriptFile("SuiteVisitorImportProxy.py").toPath().toString());
                argumentsFile.addLine("--runemptysuite");
                argumentsFile.addLine("--dryrun");
                argumentsFile.addLine("--output", "NONE");
                argumentsFile.addLine("--report", "NONE");
                argumentsFile.addLine("--log", "NONE");
            }
            for (final String suiteToRun : suitesToRun) {
                argumentsFile.addLine("--suite", suiteToRun);
            }
            for (final String testToRun : testsToRun) {
                argumentsFile.addLine("--test", testToRun);
            }
            if (!robotUserArgs.isEmpty()) {
                addUserArguments(argumentsFile);
            }
            return argumentsFile;
        }

        private void addUserArguments(final ArgumentsFile argumentsFile) {
            argumentsFile.addCommentLine("arguments specified manually by user");

            final List<String> parsedArgs = ArgumentsConverter.parseArguments(robotUserArgs);

            int i = 0;
            while (i < parsedArgs.size()) {
                if (ArgumentsConverter.isSwitchArgument(parsedArgs.get(i)) && i < parsedArgs.size() - 1
                        && !ArgumentsConverter.isSwitchArgument(parsedArgs.get(i + 1))) {
                    argumentsFile.addLine(parsedArgs.get(i), parsedArgs.get(i + 1));
                    i++;
                } else {
                    argumentsFile.addLine(parsedArgs.get(i));
                }
                i++;
            }
        }

        private String classPath() {
            final String sysPath = System.getenv("CLASSPATH");
            final List<String> wholeClasspath = new ArrayList<>();
            if (sysPath != null && !sysPath.isEmpty()) {
                wholeClasspath.add(sysPath);
            }
            wholeClasspath.addAll(classPath);

            return String.join(RedSystemProperties.getPathsSeparator(), wholeClasspath);
        }

        private String pythonPath() {
            return String.join(":", pythonPath);
        }

        private String extractAdditionalPythonPathLocationForJython() {
            String additionalPythonPathLocation = "";
            final Path jythonPath = Paths.get(executorPath);
            Path jythonParentPath = jythonPath.getParent();
            if (jythonParentPath == null) {
                final List<PythonInstallationDirectory> pythonInterpreters = RobotRuntimeEnvironment
                        .whereArePythonInterpreters();
                for (final PythonInstallationDirectory pythonInstallationDirectory : pythonInterpreters) {
                    if (pythonInstallationDirectory.getInterpreter() == SuiteExecutor.Jython) {
                        jythonParentPath = pythonInstallationDirectory.toPath();
                        break;
                    }
                }
            }
            if (jythonParentPath != null && jythonParentPath.getFileName() != null
                    && jythonParentPath.getFileName().toString().equalsIgnoreCase("bin")) {
                final Path mainDir = jythonParentPath.getParent();
                final Path sitePackagesDir = Paths.get(mainDir.toString(), "Lib", "site-packages");
                // in case of 'robot' folder existing in project
                additionalPythonPathLocation = "-J-Dpython.path=" + sitePackagesDir.toString();
            }
            return additionalPythonPathLocation;
        }
    }

    public static IRunCommandLineBuilder forEnvironment(final RobotRuntimeEnvironment env, final int listenerPort) {
        return new Builder(env.getInterpreter(), env.getPythonExecutablePath(), listenerPort);
    }

    public static IRunCommandLineBuilder forExecutor(final SuiteExecutor executor, final int listenerPort) {
        return new Builder(executor, executor.executableName(), listenerPort);
    }

    public static class RunCommandLine {

        private final List<String> commandLine;

        private final Optional<ArgumentsFile> argFile;

        RunCommandLine(final List<String> commandLine, final ArgumentsFile argumentsFile) {
            this.commandLine = new ArrayList<>(commandLine);
            this.argFile = Optional.ofNullable(argumentsFile);
        }

        public Optional<ArgumentsFile> getArgumentFile() {
            return argFile;
        }

        public String[] getCommandLine() {
            return commandLine.toArray(new String[0]);
        }

        public String[] getCommandLineWithWrappedArguments() {
            return commandLine.stream().map(RobotRuntimeEnvironment::wrapArgumentIfNeeded).toArray(String[]::new);
        }
    }
}
