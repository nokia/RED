/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfigurationFinderTest;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

public class RemoteRobotLaunchConfigurationFinderTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotLaunchConfigurationFinderTest.class);

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RemoteRobotLaunchConfiguration.TYPE_ID);

    @Test
    public void configurationReturned_whenThereIsExactlySameConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration
                .prepareDefault(projectProvider.getProject());
        new RemoteRobotLaunchConfiguration(configuration).setAgentConnectionHostValue("fakehost");
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(configuration);

        assertThat(foundConfig).isEqualToIgnoringGivenFields(configuration, "fOriginal");
    }

    @Test
    public void nullReturned_whenThereIsNoConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration
                .prepareDefault(projectProvider.getProject());
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(configuration);

        assertThat(foundConfig).isNull();
    }

    @Test
    public void nullReturned_whenThereIsDifferentConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = RemoteRobotLaunchConfiguration
                .prepareDefault(projectProvider.getProject());
        new RemoteRobotLaunchConfiguration(configuration).setAgentConnectionHostValue("fakehost");
        configuration.doSave();
        final ILaunchConfigurationWorkingCopy foundConfig = RemoteRobotLaunchConfigurationFinder
                .findSameAs(RemoteRobotLaunchConfiguration.prepareDefault(projectProvider.getProject()));

        assertThat(foundConfig).isNull();
    }
}
