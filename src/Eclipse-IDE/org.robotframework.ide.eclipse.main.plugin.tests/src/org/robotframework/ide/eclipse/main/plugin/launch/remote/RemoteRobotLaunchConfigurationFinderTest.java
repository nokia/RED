/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfigurationFinderTest;
import org.robotframework.red.junit.ProjectProvider;

public class RemoteRobotLaunchConfigurationFinderTest {

    private static IProject project;

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotLaunchConfigurationFinderTest.class);

    @BeforeClass
    public static void createNeededResources() throws CoreException, IOException, ClassNotFoundException {
        project = projectProvider.getProject();
    }

    @Before
    public void removeAllConfigurations() throws CoreException {
        final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType type = manager
                .getLaunchConfigurationType(RemoteRobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(type);
        for (final ILaunchConfiguration config : launchConfigs) {
            config.delete();
        }
    }

    @Test
    public void configurationReturned_whenThereIsExactlySameConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration.prepareDefault(project);
        new RemoteRobotLaunchConfiguration(configuration).setAgentConnectionHostValue("fakehost");
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(configuration);

        assertThat(foundConfig).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void nullReturned_whenThereIsNoConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration.prepareDefault(project);
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(configuration);

        assertThat(foundConfig).isNull();
    }

    @Test
    public void nullReturned_whenThereIsDifferentConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration.prepareDefault(project);
        new RemoteRobotLaunchConfiguration(configuration).setAgentConnectionHostValue("fakehost");
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(RemoteRobotLaunchConfiguration.prepareDefault(project));

        assertThat(foundConfig).isNull();
    }
}
