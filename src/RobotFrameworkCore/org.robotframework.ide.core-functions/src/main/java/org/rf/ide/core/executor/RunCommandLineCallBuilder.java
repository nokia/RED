/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import static com.google.common.base.Predicates.not;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;

/**
 * @author Michal Anglart
 */
public class RunCommandLineCallBuilder {

    public static interface IRunCommandLineBuilder {

        IRunCommandLineBuilder withExecutableFile(final File executableFile);

        IRunCommandLineBuilder addUserArgumentsForExecutableFile(final Collection<String> arguments);

        IRunCommandLineBuilder useSingleRobotCommandLineArg(boolean shouldUseSingleRobotCommandLineArg);

        IRunCommandLineBuilder addUserArgumentsForInterpreter(final Collection<String> arguments);

        IRunCommandLineBuilder useArgumentFile(boolean shouldUseArgumentFile);

        IRunCommandLineBuilder addLocationsToPythonPath(final Collection<String> pythonPathLocations);

        IRunCommandLineBuilder addLocationsToClassPath(final Collection<String> classPathLocations);

        IRunCommandLineBuilder addVariableFiles(final Collection<String> varFiles);

        IRunCommandLineBuilder suitesToRun(final Collection<String> suites);

        IRunCommandLineBuilder testsToRun(final Collection<String> tests);

        IRunCommandLineBuilder includeTags(final Collection<String> tags);

        IRunCommandLineBuilder excludeTags(final Collection<String> tags);

        IRunCommandLineBuilder addUserArgumentsForRobot(final Collection<String> arguments);

        IRunCommandLineBuilder withProject(final File project);

        IRunCommandLineBuilder withAdditionalProjectsLocations(final Collection<File> additionalProjectsLocations);

        RunCommandLine build() throws IOException;
    }

    private static class Builder implements IRunCommandLineBuilder {

        private final SuiteExecutor executor;

        private final String executorPath;

        private final int listenerPort;

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

        private final List<String> robotUserArgs = new ArrayList<>();

        private final List<String> interpreterUserArgs = new ArrayList<>();

        private File executableFile = null;

        private final List<String> executableFileArgs = new ArrayList<>();

        private boolean useSingleRobotCommandLineArg = false;

        private Builder(final SuiteExecutor executor, final String executorPath, final int listenerPort) {
            this.executor = executor;
            this.executorPath = executorPath;
            this.listenerPort = listenerPort;
        }

        @Override
        public IRunCommandLineBuilder withExecutableFile(final File executableFile) {
            this.executableFile = executableFile;
            return this;
        }

        @Override
        public IRunCommandLineBuilder addUserArgumentsForExecutableFile(final Collection<String> arguments) {
            this.executableFileArgs.addAll(arguments);
            return this;
        }

        @Override
        public IRunCommandLineBuilder useSingleRobotCommandLineArg(final boolean shouldUseSingleRobotCommandLineArg) {
            this.useSingleRobotCommandLineArg = shouldUseSingleRobotCommandLineArg;
            return this;
        }

        @Override
        public IRunCommandLineBuilder addUserArgumentsForInterpreter(final Collection<String> arguments) {
            this.interpreterUserArgs.addAll(arguments);
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
        public IRunCommandLineBuilder addUserArgumentsForRobot(final Collection<String> arguments) {
            this.robotUserArgs.addAll(arguments);
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

            if (executableFile != null) {
                return buildRunCommandLineWrappedWithExecutable(robotRunCommandLine);
            }

            return robotRunCommandLine;
        }

        private RunCommandLine buildRunCommandLineWrappedWithExecutable(final RunCommandLine robotRunCommandLine) {
            final List<String> cmdLine = new ArrayList<>();
            cmdLine.add(executableFile.getAbsolutePath());
            cmdLine.addAll(executableFileArgs);

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
            cmdLine.addAll(interpreterUserArgs);
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
            for (final String suite : suitesToRun) {
                robotArgs.add("-s");
                robotArgs.add(suite);
            }
            for (final String test : testsToRun) {
                robotArgs.add("-t");
                robotArgs.add(test);
            }
            robotArgs.addAll(robotUserArgs);
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

            int i = 0;
            while (i < robotUserArgs.size()) {
                if (robotUserArgs.get(i).startsWith("-") && i < robotUserArgs.size() - 1
                        && !robotUserArgs.get(i + 1).startsWith("-")) {
                    argumentsFile.addLine(robotUserArgs.get(i), robotUserArgs.get(i + 1));
                    i++;
                } else {
                    argumentsFile.addLine(robotUserArgs.get(i));
                }
                i++;
            }
        }

        private String classPath() {
            return Streams.concat(new SystemVariableAccessor().getPaths("CLASSPATH").stream(), classPath.stream())
                    .filter(not(Strings::isNullOrEmpty))
                    .collect(joining(File.pathSeparator));
        }

        private String pythonPath() {
            return pythonPath.stream().filter(not(Strings::isNullOrEmpty)).collect(joining(":"));
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

    public static IRunCommandLineBuilder forDefault(final int listenerPort) {
        return forExecutor(SuiteExecutor.Python, listenerPort);
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
    }
}
