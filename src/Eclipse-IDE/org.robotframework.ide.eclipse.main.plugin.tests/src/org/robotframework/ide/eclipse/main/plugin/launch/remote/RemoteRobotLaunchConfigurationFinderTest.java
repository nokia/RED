/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RemoteRobotLaunchConfigurationFinderTest {

    @Project
    static IProject project;

    @AfterEach
    public void afterTest() throws CoreException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType type = launchManager
                .getLaunchConfigurationType(RemoteRobotLaunchConfiguration.TYPE_ID);
        for (final ILaunchConfiguration config : launchManager.getLaunchConfigurations(type)) {
            config.delete();
        }
    }

    @Test
    public void configurationReturned_whenThereIsExactlySameConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration
                .prepareDefault(project);
        new RemoteRobotLaunchConfiguration(configuration).setAgentConnectionHostValue("fakehost");
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(configuration);

        assertThat(foundConfig).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void nullReturned_whenThereIsNoConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration
                .prepareDefault(project);
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(configuration);

        assertThat(foundConfig).isNull();
    }

    @Test
    public void nullReturned_whenThereIsDifferentConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration
                .prepareDefault(project);
        new RemoteRobotLaunchConfiguration(configuration).setAgentConnectionHostValue("fakehost");
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(RemoteRobotLaunchConfiguration.prepareDefault(project));

        assertThat(foundConfig).isNull();
    }
}
