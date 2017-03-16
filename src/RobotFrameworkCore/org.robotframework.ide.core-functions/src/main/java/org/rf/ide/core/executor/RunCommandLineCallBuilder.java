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
import java.util.Collection;
import java.util.List;

import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;

/**
 * @author Michal Anglart
 */
public class RunCommandLineCallBuilder {

    public static interface IRunCommandLineBuilder {

        IRunCommandLineBuilder addLocationsToPythonPath(final Collection<String> pythonPathLocations);

        IRunCommandLineBuilder addLocationsToClassPath(final Collection<String> classPathLocations);

        IRunCommandLineBuilder addVariableFiles(final Collection<String> varFiles);

        IRunCommandLineBuilder suitesToRun(final Collection<String> suites);

        IRunCommandLineBuilder testsToRun(final Collection<String> tests);

        IRunCommandLineBuilder includeTags(final Collection<String> tags);

        IRunCommandLineBuilder excludeTags(final Collection<String> tags);

        IRunCommandLineBuilder addUserArgumentsForInterpreter(final String arguments);

        IRunCommandLineBuilder addUserArgumentsForRobot(final String arguments);

        IRunCommandLineBuilder enableDryRun();

        IRunCommandLineBuilder withProject(final File project);

        IRunCommandLineBuilder withAdditionalProjectsLocations(final Collection<String> additionalProjectsLocations);

        IRunCommandLineBuilder withExecutableFile(final String executableFilePath);

        IRunCommandLineBuilder addUserArgumentsForExecutableFile(final String arguments);

        RunCommandLine build() throws IOException;
    }

    private static class Builder implements IRunCommandLineBuilder {

        private final SuiteExecutor executor;

        private final String executorPath;

        private final int listenerPort;

        private final List<String> pythonPath = new ArrayList<>();

        private final List<String> classPath = new ArrayList<>();

        private final List<String> variableFiles = new ArrayList<>();

        private final List<String> suitesToRun = new ArrayList<>();

        private final List<String> testsToRun = new ArrayList<>();

        private final List<String> tagsToInclude = new ArrayList<>();

        private final List<String> tagsToExclude = new ArrayList<>();

        private final List<String> additionalProjectsLocations = new ArrayList<>();

        private File project = null;

        private boolean enableDryRun = false;

        private String robotUserArgs = "";

        private String interpreterUserArgs = "";

        private String executableFilePath = "";

        private String executableFileArgs = "";

        private Builder(final SuiteExecutor executor, final String executorPath, final int listenerPort) {
            this.executor = executor;
            this.executorPath = executorPath;
            this.listenerPort = listenerPort;
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
        public IRunCommandLineBuilder addUserArgumentsForInterpreter(final String arguments) {
            this.interpreterUserArgs = arguments.trim();
            return this;
        }

        @Override
        public IRunCommandLineBuilder addUserArgumentsForRobot(final String arguments) {
            this.robotUserArgs = arguments.trim();
            return this;
        }

        @Override
        public IRunCommandLineBuilder enableDryRun() {
            this.enableDryRun = true;
            return this;
        }

        @Override
        public IRunCommandLineBuilder withProject(final File project) {
            this.project = project;
            return this;
        }

        @Override
        public IRunCommandLineBuilder withAdditionalProjectsLocations(
                final Collection<String> additionalProjectsLocations) {
            this.additionalProjectsLocations.addAll(additionalProjectsLocations);
            return this;
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
        public RunCommandLine build() throws IOException {
            final List<String> cmdLine = new ArrayList<>();

            if (!executableFilePath.isEmpty()) {
                cmdLine.add(executableFilePath);
                if (!executableFileArgs.isEmpty()) {
                    cmdLine.addAll(convertArguments(executableFileArgs));
                }
            }
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
                cmdLine.addAll(convertArguments(interpreterUserArgs));
            }
            cmdLine.add("-m");
            cmdLine.add("robot.run");

            cmdLine.add("--listener");
            cmdLine.add(RobotRuntimeEnvironment.copyScriptFile("TestRunnerAgent.py").toPath() + ":" + listenerPort);

            final ArgumentsFile argumentsFile = createArgumentsFile();
            cmdLine.add("--argumentfile");
            cmdLine.add(argumentsFile.writeToTemporaryOrUseAlreadyExisting().toPath().toString());

            if (project != null) {
                cmdLine.add(project.getAbsolutePath());
            }
            for (final String projectLocation : additionalProjectsLocations) {
                cmdLine.add(projectLocation);
            }
            return new RunCommandLine(cmdLine, argumentsFile);
        }

        private List<String> convertArguments(final String args) {
            return ArgumentsConverter.joinMultipleArgValues(ArgumentsConverter.parseArguments(args));
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

            final List<String> args = ArgumentsConverter.parseArguments(robotUserArgs);
            final List<String> joinedArgs = ArgumentsConverter.joinMultipleArgValues(args);
            
            int i = 0;
            while (i < joinedArgs.size()) {
                if (ArgumentsConverter.isSwitchArgument(joinedArgs.get(i))) {
                    if (i < joinedArgs.size() - 1 && ArgumentsConverter.isSwitchArgument(joinedArgs.get(i + 1))) {
                        argumentsFile.addLine(joinedArgs.get(i));
                    } else {
                        argumentsFile.addLine(joinedArgs.get(i), joinedArgs.get(i + 1));
                        i++;
                    }
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

        private final ArgumentsFile argFile;

        RunCommandLine(final List<String> commandLine, final ArgumentsFile argumentsFile) {
            this.commandLine = new ArrayList<>(commandLine);
            this.argFile = argumentsFile;
        }

        public ArgumentsFile getArgumentFile() {
            return argFile;
        }

        public String[] getCommandLine() {
            return commandLine.toArray(new String[0]);
        }

        public String show() {
            return String.join(" ", commandLine);
        }
    }
}
