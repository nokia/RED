/*
 * Copyright 2020 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static java.util.stream.Collectors.toList;
import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.variables.RedStringVariablesManager;
import org.robotframework.ide.eclipse.main.plugin.project.ASuiteFileDescriber;

public class RobotLaunchConfigurationHelper {

    public static List<String> parseArguments(final String arguments) {
        final RedStringVariablesManager variableManager = new RedStringVariablesManager();
        return Stream.of(DebugPlugin.parseArguments(arguments)).map(argument -> {
            try {
                return variableManager.substituteUsingQuickValuesSet(argument);
            } catch (final CoreException e) {
                return argument;
            }
        }).collect(toList());
    }

    public static Map<IResource, List<String>> findResources(final IProject project,
            final Map<String, List<String>> suitePaths) throws CoreException {
        final Map<IResource, List<String>> result = new LinkedHashMap<>();
        if (suitePaths.isEmpty()) {
            result.put(project, new ArrayList<>());
            return result;
        }

        final Set<String> problems = new HashSet<>();

        final Map<String, List<String>> sortedSuitePaths = new TreeMap<>(suitePaths);
        for (final Entry<String, List<String>> entry : sortedSuitePaths.entrySet()) {
            final IResource resource = project.findMember(Path.fromPortableString(entry.getKey()));
            if (resource != null) {
                if (resource.isVirtual() || isNotIncluded(resource, result.keySet())) {
                    result.put(resource, entry.getValue());
                }
            } else {
                problems.add("Suite '" + entry.getKey() + "' does not exist in project '" + project.getName() + "'");
            }
        }

        if (!problems.isEmpty()) {
            throw newCoreException(String.join("\n", problems));
        }

        return result;
    }

    public static Map<IResource, List<String>> findLinkedResources(final Map<IResource, List<String>> resources)
            throws CoreException {
        final Map<IResource, List<String>> result = new LinkedHashMap<>();

        for (final Entry<IResource, List<String>> entry : resources.entrySet()) {
            entry.getKey().accept(r -> {
                if (r.isLinked(IResource.CHECK_ANCESTORS) && isNotIncluded(r, result.keySet()) && isDataSource(r)) {
                    result.put(r, entry.getValue());
                }
                return true;
            });
        }

        return result;
    }

    private static boolean isDataSource(final IResource resource) {
        return resource.getType() != IResource.FILE || ASuiteFileDescriber.isSuiteFile((IFile) resource)
                || ASuiteFileDescriber.isRpaSuiteFile((IFile) resource);
    }

    private static boolean isNotIncluded(final IResource resource, final Set<IResource> resources) {
        return !resource.isVirtual() && resources.stream()
                .noneMatch(r -> !r.isVirtual() && r.getLocation().isPrefixOf(resource.getLocation()));
    }

    public static String createTopLevelSuiteName(final List<IResource> dataSources,
            final List<String> customRobotArguments) {
        for (int i = customRobotArguments.size() - 1; i >= 0; i--) {
            final String arg = customRobotArguments.get(i);
            if (arg.equals("--name") || arg.equals("-N")) {
                if (i < customRobotArguments.size() - 1) {
                    final String nameValue = customRobotArguments.get(i + 1);
                    if (!nameValue.isEmpty() && !nameValue.startsWith("-")) {
                        return nameValue;
                    }
                }
            }
        }
        return RobotPathsNaming.createTopLevelSuiteName(dataSources);
    }

    public static String collectRobotArguments(final RobotLaunchConfiguration robotConfig,
            final List<String> testPaths) throws CoreException {
        final List<String> args = new ArrayList<>();
        args.addAll(getRobotArgumentsWithoutOldTestsOrTasks(robotConfig));
        args.addAll(getNewTestOrTaskArguments(testPaths));
        return args.stream().map(arg -> arg.contains(" ") ? "\"" + arg + "\"" : arg).collect(Collectors.joining(" "));
    }

    private static List<String> getRobotArgumentsWithoutOldTestsOrTasks(final RobotLaunchConfiguration robotConfig)
            throws CoreException {
        final List<String> robotArguments = new ArrayList<>();
        final String customRobotArguments = robotConfig.getRobotArguments();
        if (!customRobotArguments.isEmpty()) {
            final List<String> args = parseArguments(customRobotArguments);
            for (int i = 0; i < args.size() - 1; i++) {
                final String arg = args.get(i);
                if (!arg.equals("--test") && !arg.equals("--task") && !arg.equals("-t")
                        && arg.startsWith("-")) {
                    final String argValue = args.get(i + 1);
                    if (!argValue.isEmpty() && !argValue.startsWith("-")) {
                        robotArguments.add(arg);
                        robotArguments.add(argValue);
                    }
                }
            }
        }
        return robotArguments;
    }

    private static List<String> getNewTestOrTaskArguments(final List<String> testOrTaskPaths) {
        final List<String> robotArguments = new ArrayList<>();
        for (final String path : testOrTaskPaths) {
            robotArguments.add("-t");
            robotArguments.add(path);
        }
        return robotArguments;

    }

}
