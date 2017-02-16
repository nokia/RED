/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

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

public class RobotLaunchConfigurationFinder {

    public static final String SELECTED_TESTS_CONFIG_SUFFIX = " (Selected Test Cases)";

    // public static ILaunchConfigurationWorkingCopy findLaunchConfiguration(final List<IResource>
    // resources)
    // throws CoreException {
    //
    // final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
    // final ILaunchConfigurationType launchConfigurationType = launchManager
    // .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
    // final ILaunchConfiguration[] launchConfigs =
    // launchManager.getLaunchConfigurations(launchConfigurationType);
    // if (resources.size() == 1 && (resources.get(0) instanceof IContainer)) {
    // final String resourceName = resources.get(0).getName();
    // final String projectName = resources.get(0).getProject().getName();
    // for (final ILaunchConfiguration configuration : launchConfigs) {
    // if (configuration.getName().startsWith(resourceName)
    // && new RobotLaunchConfiguration(configuration).getProjectName().equals(projectName)) {
    // return asWorkingCopy(configuration);
    // }
    // }
    // }
    // for (final ILaunchConfiguration configuration : launchConfigs) {
    // final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
    // if (robotConfig.isSuitableFor(resources)) {
    // return asWorkingCopy(configuration);
    // }
    // }
    // return null;
    // }

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
        final String configurationName = RobotLaunchConfiguration.getNameForSelectedTestCasesConfiguration(resources);
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
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType launchConfigurationType = launchManager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        ILaunchConfigurationWorkingCopy configuration = findLaunchConfiguration(resources);
        if (configuration == null) {
            configuration = RobotLaunchConfiguration.prepareDefault(launchConfigurationType, resources);
        }
        return configuration;
    }

    public static ILaunchConfigurationWorkingCopy getLaunchConfigurationExceptSelectedTestCases(
            final List<IResource> resources)
            throws CoreException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType launchConfigurationType = launchManager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        ILaunchConfigurationWorkingCopy configuration = findLaunchConfigurationExceptSelectedTestCases(resources);
        if (configuration == null) {
            configuration = RobotLaunchConfiguration.prepareDefault(launchConfigurationType, resources);
        }
        return configuration;
    }

    public static ILaunchConfigurationWorkingCopy getLaunchConfigurationForSelectedTestCases(
            final Map<IResource, List<String>> resourcesToTests) throws CoreException {
        ILaunchConfigurationWorkingCopy configuration = RobotLaunchConfigurationFinder
                .findLaunchConfigurationSelectedTestCases(newArrayList(resourcesToTests.keySet()));
        if (configuration == null) {
            configuration = RobotLaunchConfiguration.prepareLaunchConfigurationForSelectedTestCases(resourcesToTests);
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
            if (RobotLaunchConfiguration.contentEquals(configuration, config)) {
                return asWorkingCopy(config);
            }
        }
        return null;
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
