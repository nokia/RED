/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationNaming;

public class RobotLaunchConfigurationFinder {

    public static ILaunchConfigurationWorkingCopy findLaunchConfiguration(final List<IResource> resources)
            throws CoreException {

        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType launchConfigurationType = launchManager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations(launchConfigurationType);
        if (resources.size() == 1) {
            if (resources.get(0) instanceof IContainer) {
                final String resourceName = resources.get(0).getName();
                final String projectName = resources.get(0).getProject().getName();
                for (final ILaunchConfiguration configuration : launchConfigs) {
                    if (configuration.getName().startsWith(resourceName)
                            && new RobotLaunchConfiguration(configuration).getProjectName().equals(projectName)) {
                        return asWorkingCopy(configuration);
                    }
                }
            } else {
                for (final ILaunchConfiguration configuration : launchConfigs) {
                    final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
                    if (robotConfig.isSuitableForOnly(resources)) {
                        return asWorkingCopy(configuration);
                    }
                }
            }
        }
        for (final ILaunchConfiguration configuration : launchConfigs) {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            if (robotConfig.isSuitableFor(resources)) {
                return asWorkingCopy(configuration);
            }
        }
        return null;
    }

    public static ILaunchConfigurationWorkingCopy findLaunchConfigurationSelectedTestCases(
            final List<IResource> resources)
            throws CoreException {

        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType launchConfigurationType = launchManager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations(launchConfigurationType);
        final String configurationName = RobotLaunchConfigurationNaming.getNamePrefix(resources,
                RobotLaunchConfigurationNaming.SELECTED_TEST_CASES_SUFFIX);
        final String projectName = resources.get(0).getProject().getName();
        for (final ILaunchConfiguration configuration : launchConfigs) {
            if (configuration.getName().equals(configurationName)
                    && new RobotLaunchConfiguration(configuration).getProjectName().equals(projectName)) {
                return asWorkingCopy(configuration);
            }
        }
        return null;
    }

    public static ILaunchConfigurationWorkingCopy findLaunchConfigurationExceptSelectedTestCases(
            final List<IResource> resources)
            throws CoreException {

        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType launchConfigurationType = launchManager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations(launchConfigurationType);
        if (resources.size() == 1 && (resources.get(0) instanceof IContainer)) {
            final String resourceName = resources.get(0).getName();
            final String projectName = resources.get(0).getProject().getName();
            for (final ILaunchConfiguration configuration : launchConfigs) {
                if (configuration.getName().equals(resourceName)
                        && new RobotLaunchConfiguration(configuration).getProjectName().equals(projectName)) {
                    return asWorkingCopy(configuration);
                }
            }
        }
        for (final ILaunchConfiguration configuration : launchConfigs) {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            if (robotConfig.isGeneralPurposeConfiguration() && robotConfig.isSuitableFor(resources)) {
                return asWorkingCopy(configuration);
            }
        }
        return null;
    }

    public static ILaunchConfigurationWorkingCopy getLaunchConfiguration(final List<IResource> resources)
            throws CoreException {
        ILaunchConfigurationWorkingCopy configuration = findLaunchConfiguration(resources);
        if (configuration == null) {
            configuration = RobotLaunchConfiguration.prepareDefault(resources);
        }
        return configuration;
    }

    public static ILaunchConfigurationWorkingCopy getLaunchConfigurationExceptSelectedTestCases(
            final List<IResource> resources)
            throws CoreException {
        ILaunchConfigurationWorkingCopy configuration = findLaunchConfigurationExceptSelectedTestCases(resources);
        if (configuration == null) {
            configuration = RobotLaunchConfiguration.prepareDefault(resources);
        }
        return configuration;
    }

    public static ILaunchConfigurationWorkingCopy getLaunchConfigurationForSelectedTestCases(
            final Map<IResource, List<String>> resourcesToTests) throws CoreException {
        ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfigurationFinder
                .findLaunchConfigurationSelectedTestCases(newArrayList(resourcesToTests.keySet()));
        if (configuration == null) {
            configuration = RobotLaunchConfiguration.prepareForSelectedTestCases(resourcesToTests);
        }
        return configuration;
    }

    public static ILaunchConfigurationWorkingCopy findSameAs(final ILaunchConfiguration configuration)
            throws CoreException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType launchConfigurationType = launchManager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations(launchConfigurationType);
        for (final ILaunchConfiguration config : launchConfigs) {
            if (contentEquals(configuration, config)) {
                return asWorkingCopy(config);
            }
        }
        return null;
    }

    private static boolean contentEquals(final ILaunchConfiguration config1, final ILaunchConfiguration config2)
            throws CoreException {
        final RobotLaunchConfiguration rConfig1 = new RobotLaunchConfiguration(config1);
        final RobotLaunchConfiguration rConfig2 = new RobotLaunchConfiguration(config2);
        return rConfig1.getExecutor().equals(rConfig2.getExecutor())
                && rConfig1.getExecutorArguments().equals(rConfig2.getExecutorArguments())
                && rConfig1.getProjectName().equals(rConfig2.getProjectName())
                && rConfig1.isUsingInterpreterFromProject() == rConfig2.isUsingInterpreterFromProject()
                && rConfig1.getInterpreterArguments().equals(rConfig2.getInterpreterArguments())
                && rConfig1.isExcludeTagsEnabled() == rConfig2.isExcludeTagsEnabled()
                && rConfig1.isIncludeTagsEnabled() == rConfig2.isIncludeTagsEnabled()
                && rConfig1.isGeneralPurposeConfiguration() == rConfig2.isGeneralPurposeConfiguration()
                && rConfig1.getExcludedTags().equals(rConfig2.getExcludedTags())
                && rConfig1.getIncludedTags().equals(rConfig2.getIncludedTags())
                && rConfig1.getSuitePaths().equals(rConfig2.getSuitePaths());
    }

    private static ILaunchConfigurationWorkingCopy asWorkingCopy(final ILaunchConfiguration config) {
        if (config instanceof ILaunchConfigurationWorkingCopy) {
            return (ILaunchConfigurationWorkingCopy) config;
        } else {
            try {
                return config.getWorkingCopy();
            } catch (final CoreException e) {
                return null;
            }
        }
    }
}
