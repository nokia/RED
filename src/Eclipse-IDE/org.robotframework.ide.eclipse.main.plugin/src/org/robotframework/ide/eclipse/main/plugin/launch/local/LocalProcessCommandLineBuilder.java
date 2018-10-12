/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Iterables.getOnlyElement;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugPlugin;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.IRunCommandLineBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotPathsNaming;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;

class LocalProcessCommandLineBuilder {

    private final RobotLaunchConfiguration robotConfig;

    private final RobotProject robotProject;

    LocalProcessCommandLineBuilder(final RobotLaunchConfiguration robotConfig, final RobotProject robotProject) {
        this.robotConfig = robotConfig;
        this.robotProject = robotProject;
    }

    RunCommandLine createRunCommandLine(final int port, final RedPreferences preferences)
            throws CoreException, IOException {
        final IRunCommandLineBuilder builder = createBuilder(port);

        builder.useArgumentFile(preferences.shouldLaunchUsingArgumentsFile());
        if (!robotConfig.getExecutableFilePath().isEmpty()) {
            builder.withExecutableFile(resolveExecutableFile(robotConfig.getExecutableFilePath()));
            builder.addUserArgumentsForExecutableFile(parseArguments(robotConfig.getExecutableFileArguments()));
            builder.useSingleRobotCommandLineArg(preferences.shouldUseSingleCommandLineArgument());
        }
        builder.addUserArgumentsForInterpreter(parseArguments(robotConfig.getInterpreterArguments()));
        builder.addUserArgumentsForRobot(parseArguments(robotConfig.getRobotArguments()));

        final RobotProjectConfig projectConfig = robotProject.getRobotProjectConfig();
        if (projectConfig != null) {
            final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(robotProject.getProject(),
                    projectConfig);
            final EnvironmentSearchPaths searchPaths = redConfig.createExecutionEnvironmentSearchPaths();
            builder.addLocationsToClassPath(searchPaths.getClassPaths());
            builder.addLocationsToPythonPath(searchPaths.getPythonPaths());
            builder.addVariableFiles(redConfig.getVariableFilePaths());
        }

        final Map<String, List<String>> suitePaths = robotConfig.getSuitePaths();
        final List<IResource> resources = findSuiteResources(suitePaths, robotProject.getProject());
        if (shouldUseSingleTestPathInCommandLine(resources, preferences)) {
            builder.withDataSources(newArrayList(getOnlyElement(resources).getLocation().toFile()));
            builder.testsToRun(getOnlyElement(suitePaths.values()));
        } else {
            builder.withDataSources(newArrayList(robotProject.getProject().getLocation().toFile()));
            builder.suitesToRun(createSuitesToRun(resources));
            builder.testsToRun(createTestsToRun(suitePaths, robotProject.getProject()));
        }

        if (robotConfig.isIncludeTagsEnabled()) {
            builder.includeTags(robotConfig.getIncludedTags());
        }
        if (robotConfig.isExcludeTagsEnabled()) {
            builder.excludeTags(robotConfig.getExcludedTags());
        }
        return builder.build();
    }

    private IRunCommandLineBuilder createBuilder(final int port) throws CoreException {
        if (robotConfig.isUsingInterpreterFromProject()) {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            if (runtimeEnvironment != null) {
                return RunCommandLineCallBuilder.forEnvironment(runtimeEnvironment, port);
            } else {
                return RunCommandLineCallBuilder.forExecutor(SuiteExecutor.Python, port);
            }
        } else {
            return RunCommandLineCallBuilder.forExecutor(robotConfig.getInterpreter(), port);
        }
    }

    private boolean shouldUseSingleTestPathInCommandLine(final List<IResource> resources,
            final RedPreferences preferences) throws CoreException {
        // FIXME temporary fix for https://github.com/robotframework/robotframework/issues/2564
        return preferences.shouldUseSingleFileDataSource() && resources.size() == 1
                && getOnlyElement(resources) instanceof IFile;
    }

    private File resolveExecutableFile(final String path) throws CoreException {
        final IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
        final File executableFile = new File(variableManager.performStringSubstitution(path));
        if (!executableFile.exists()) {
            throw newCoreException("Executable file '" + executableFile.getAbsolutePath() + "' does not exist");
        }
        return executableFile;
    }

    private List<String> parseArguments(final String arguments) {
        final IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
        return Stream.of(DebugPlugin.parseArguments(arguments)).map(argument -> {
            try {
                return variableManager.performStringSubstitution(argument);
            } catch (final CoreException e) {
                return argument;
            }
        }).collect(toList());
    }

    private List<IResource> findSuiteResources(final Map<String, List<String>> suitePaths, final IProject project)
            throws CoreException {
        final List<IResource> resources = new ArrayList<>();
        final Set<String> problems = new HashSet<>();
        for (final String suitePath : suitePaths.keySet()) {
            final IResource resource = project.findMember(Path.fromPortableString(suitePath));
            if (resource != null) {
                resources.add(resource);
            } else {
                problems.add("Suite '" + suitePath + "' does not exist in project '" + project.getName() + "'");
            }
        }
        if (!problems.isEmpty()) {
            throw newCoreException(String.join("\n", problems));
        }
        return resources;
    }

    private List<String> createSuitesToRun(final List<IResource> resources) {
        return resources.stream().map(RobotPathsNaming::createSuiteName).collect(toList());
    }

    private List<String> createTestsToRun(final Map<String, List<String>> suitePaths, final IProject project) {
        return suitePaths.entrySet().stream().flatMap(e -> {
            final IPath path = Path.fromPortableString(e.getKey());
            return e.getValue().stream().map(testName -> RobotPathsNaming.createTestName(project, path, testName));
        }).collect(toList());
    }

}
