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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.rf.ide.core.SystemVariableAccessor;
import org.rf.ide.core.environment.EnvironmentSearchPaths;
import org.rf.ide.core.execution.RunCommandLineCallBuilder;
import org.rf.ide.core.execution.RunCommandLineCallBuilder.IRunCommandLineBuilder;
import org.rf.ide.core.execution.RunCommandLineCallBuilder.RunCommandLine;
import org.rf.ide.core.project.RobotProjectConfig;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.variables.RedStringVariablesManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

class LocalProcessCommandLineBuilder {

    private final LocalProcessInterpreter interpreter;

    private final RobotLaunchConfiguration robotConfig;

    private final RobotProject robotProject;

    private final SystemVariableAccessor variableAccessor;

    LocalProcessCommandLineBuilder(final LocalProcessInterpreter interpreter,
            final RobotLaunchConfiguration robotConfig, final RobotProject robotProject) {
        this(interpreter, robotConfig, robotProject, new SystemVariableAccessor());
    }

    @VisibleForTesting
    LocalProcessCommandLineBuilder(final LocalProcessInterpreter interpreter,
            final RobotLaunchConfiguration robotConfig, final RobotProject robotProject,
            final SystemVariableAccessor variableAccessor) {
        this.interpreter = interpreter;
        this.robotConfig = robotConfig;
        this.robotProject = robotProject;
        this.variableAccessor = variableAccessor;
    }

    RunCommandLine createRunCommandLine(final int port, final RedPreferences preferences)
            throws CoreException, IOException {
        final IRunCommandLineBuilder builder = RunCommandLineCallBuilder.create(interpreter.getExecutor(),
                interpreter.getPath(), port);
        addArgumentEntries(builder, preferences);
        addProjectConfigEntries(builder);
        addTags(builder);
        addDataSources(builder, preferences);
        return builder.build();
    }

    private void addArgumentEntries(final IRunCommandLineBuilder builder, final RedPreferences preferences)
            throws CoreException {
        builder.useArgumentFile(preferences.shouldLaunchUsingArgumentsFile());
        if (!robotConfig.getExecutableFilePath().isEmpty()) {
            builder.withExecutableFile(resolveExecutableFile(robotConfig.getExecutableFilePath()));
            builder.addUserArgumentsForExecutableFile(
                    RobotLaunchConfigurationHelper.parseArguments(robotConfig.getExecutableFileArguments()));
            builder.useSingleRobotCommandLineArg(preferences.shouldUseSingleCommandLineArgument());
        }
        builder.addUserArgumentsForInterpreter(
                RobotLaunchConfigurationHelper.parseArguments(robotConfig.getInterpreterArguments()));
        builder.addUserArgumentsForRobot(
                RobotLaunchConfigurationHelper.parseArguments(robotConfig.getRobotArguments()));
    }

    private void addProjectConfigEntries(final IRunCommandLineBuilder builder) {
        final IProject project = robotProject.getProject();
        final RobotProjectConfig projectConfig = robotProject.getRobotProjectConfig();
        final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(project, projectConfig, variableAccessor);
        final EnvironmentSearchPaths searchPaths = redConfig.createExecutionEnvironmentSearchPaths();
        builder.addLocationsToClassPath(searchPaths.getClassPaths());
        builder.addLocationsToPythonPath(searchPaths.getPythonPaths());
    }

    private void addTags(final IRunCommandLineBuilder builder) throws CoreException {
        if (robotConfig.isIncludeTagsEnabled()) {
            builder.includeTags(robotConfig.getIncludedTags());
        }
        if (robotConfig.isExcludeTagsEnabled()) {
            builder.excludeTags(robotConfig.getExcludedTags());
        }
    }

    private void addDataSources(final IRunCommandLineBuilder builder, final RedPreferences preferences)
            throws CoreException {
        final IProject project = robotProject.getProject();
        final Map<String, List<String>> suitePaths = robotConfig.getSelectedSuitePaths();
        final Map<IResource, List<String>> selectedResources = RobotLaunchConfigurationHelper.findResources(project,
                suitePaths);
        final Map<IResource, List<String>> linkedResources = RobotLaunchConfigurationHelper
                .findLinkedResources(selectedResources);
        final Map<IResource, List<String>> notLinkedResources = Maps.filterKeys(selectedResources,
                resource -> !resource.isVirtual() && !resource.isLinked(IResource.CHECK_ANCESTORS));
        final Map<IResource, List<String>> allResources = new LinkedHashMap<>(notLinkedResources);
        allResources.putAll(linkedResources);

        if (preferences.shouldUseSingleFileDataSource() && allResources.size() == 1) {
            builder.withDataSources(newArrayList(getOnlyElement(allResources.keySet()).getLocation().toFile()));
            if (!suitePaths.isEmpty()) {
                final Function<IResource, List<String>> mapper = r -> newArrayList(
                        removeFileExtensionIfNeeded(r, r.getLocation()).lastSegment());
                final String topLevelSuiteName = RobotLaunchConfigurationHelper.createTopLevelSuiteName(
                        new ArrayList<>(),
                        RobotLaunchConfigurationHelper.parseArguments(robotConfig.getRobotArguments()));
                final int pathSegmentsToSkip = !topLevelSuiteName.isEmpty() ? 1 : 0;
                builder.testsToRun(RobotPathsNaming.createTestNames(allResources, topLevelSuiteName, mapper,
                        pathSegmentsToSkip));
            }
        } else {
            final List<IResource> dataSources = new ArrayList<>();
            dataSources.add(project);
            dataSources.addAll(linkedResources.keySet());
            builder.withDataSources(
                    dataSources.stream().map(IResource::getLocation).map(IPath::toFile).collect(toList()));
            if (!suitePaths.isEmpty()) {
                final String topLevelSuiteName = RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources,
                        RobotLaunchConfigurationHelper.parseArguments(robotConfig.getRobotArguments()));
                final Function<IResource, List<String>> mapper = r -> {
                    if (r.isLinked(IResource.CHECK_ANCESTORS)) {
                        return newArrayList(removeFileExtensionIfNeeded(r, r.getLocation()).lastSegment());
                    } else {
                        final List<String> segments = newArrayList(project.getLocation().lastSegment());
                        segments.addAll(
                                newArrayList(removeFileExtensionIfNeeded(r, r.getProjectRelativePath()).segments()));
                        return segments;
                    }
                };
                final int pathSegmentsToSkip = (linkedResources.isEmpty() && !topLevelSuiteName.isEmpty()) ? 1 : 0;
                builder.suitesToRun(RobotPathsNaming.createSuiteNames(notLinkedResources, topLevelSuiteName, mapper,
                        pathSegmentsToSkip));
                builder.suitesToRun(RobotPathsNaming.createSuiteNames(linkedResources, topLevelSuiteName, mapper,
                        pathSegmentsToSkip));
                builder.testsToRun(RobotPathsNaming.createTestNames(notLinkedResources, topLevelSuiteName, mapper,
                        pathSegmentsToSkip));
                builder.testsToRun(RobotPathsNaming.createTestNames(linkedResources, topLevelSuiteName, mapper,
                        pathSegmentsToSkip));
            }
            robotConfig.setLinkedResourcesPaths(linkedResources);
        }
    }

    private static IPath removeFileExtensionIfNeeded(final IResource resource, final IPath path) {
        return resource.getType() == IResource.FILE ? path.removeFileExtension() : path;
    }

    private static File resolveExecutableFile(final String path) throws CoreException {
        final RedStringVariablesManager variableManager = new RedStringVariablesManager();
        final File executableFile = new File(variableManager.substituteUsingQuickValuesSet(path));
        if (!executableFile.exists()) {
            throw newCoreException("Executable file '" + executableFile.getAbsolutePath() + "' does not exist");
        }
        return executableFile;
    }
}
