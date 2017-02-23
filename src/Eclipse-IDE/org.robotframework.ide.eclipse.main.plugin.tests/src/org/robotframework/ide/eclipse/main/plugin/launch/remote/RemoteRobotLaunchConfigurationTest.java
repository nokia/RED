/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.red.junit.ProjectProvider;

public class RemoteRobotLaunchConfigurationTest {

    private static ILaunchManager manager;

    private final static String PROJECT_NAME = RemoteRobotLaunchConfigurationTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Before
    public void setup() throws CoreException {
        manager = DebugPlugin.getDefault().getLaunchManager();
        removeAllConfigurations();
    }

    @AfterClass
    public static void clean() throws CoreException {
        removeAllConfigurations();
    }

    private static void removeAllConfigurations() throws CoreException {
        final ILaunchConfigurationType launchConfigurationType = manager
                .getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(launchConfigurationType);
        for (final ILaunchConfiguration config : launchConfigs) {
            config.delete();
        }
    }

    // @Test
    // public void remoteDebugPortAndTimeoutAreCorrect_whenSet() throws CoreException {
    // final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
    // robotConfig.setRemoteDebugPort("1234");
    // robotConfig.setRemoteDebugTimeout("9876");
    // final Optional<Integer> port = robotConfig.getRemoteDebugPort();
    // final Optional<Integer> timeout = robotConfig.getRemoteDebugTimeout();
    // assertThat(port.isPresent()).isTrue();
    // assertThat(port.get()).isEqualTo(1234);
    // assertThat(timeout.isPresent()).isTrue();
    // assertThat(timeout.get()).isEqualTo(9876);
    // }
    //
    // @Test
    // public void remoteDebugPortAndTimeoutAreAbsent_whenNotSet() throws CoreException {
    // final RobotLaunchConfiguration robotConfig = getDefaultRobotLaunchConfiguration();
    // final Optional<Integer> port = robotConfig.getRemoteDebugPort();
    // final Optional<Integer> timeout = robotConfig.getRemoteDebugTimeout();
    // assertThat(port.isPresent()).isFalse();
    // assertThat(timeout.isPresent()).isFalse();
    // }
}
