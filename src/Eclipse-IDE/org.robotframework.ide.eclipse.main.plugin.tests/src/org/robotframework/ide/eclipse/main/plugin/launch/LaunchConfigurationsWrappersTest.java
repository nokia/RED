/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.local.LocalProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteRobotLaunchConfiguration;

public class LaunchConfigurationsWrappersTest {

    @Test
    public void properWrapperIsReturnedForLocalTestsLaunchConfiguration() throws Exception {
        final ILaunchConfiguration config = createConfigurationMock(RobotLaunchConfiguration.TYPE_ID);
        final IRobotLaunchConfiguration wrapper = LaunchConfigurationsWrappers.robotLaunchConfiguration(config);

        assertThat(wrapper).isExactlyInstanceOf(RobotLaunchConfiguration.class);
    }

    @Test
    public void properWrapperIsReturnedForRemoteTestsLaunchConfiguration() throws Exception {
        final ILaunchConfiguration config = createConfigurationMock(RemoteRobotLaunchConfiguration.TYPE_ID);
        final IRobotLaunchConfiguration wrapper = LaunchConfigurationsWrappers.robotLaunchConfiguration(config);

        assertThat(wrapper).isExactlyInstanceOf(RemoteRobotLaunchConfiguration.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingWrapperForUnknownConfigurationType() throws Exception {
        final ILaunchConfiguration config = createConfigurationMock("unknown");
        LaunchConfigurationsWrappers.robotLaunchConfiguration(config);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrown_whenCreatingWrapperForProblematicConfiguration() throws Exception {
        final ILaunchConfiguration config = createExceptionsThrowingConfigurationMock();
        LaunchConfigurationsWrappers.robotLaunchConfiguration(config);
    }

    @Test
    public void properProcessIsReturnedForLocalTestsLaunchConfiguration() throws Exception {
        final ILaunchConfiguration config = createConfigurationMock(RobotLaunchConfiguration.TYPE_ID);
        final ILaunch launch = mock(ILaunch.class);
        when(launch.getLaunchConfiguration()).thenReturn(config);
        when(launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT)).thenReturn("false");

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        final LaunchConfigurationsWrappers processFactory = new LaunchConfigurationsWrappers();
        final IRobotProcess robotProcess = processFactory.newProcess(launch, process, "name", new HashMap<>());

        assertThat(robotProcess).isExactlyInstanceOf(LocalProcess.class);
        assertThat(robotProcess.getLabel()).isEqualTo("name");
        assertThat(robotProcess.getLaunch()).isSameAs(launch);
    }

    @Test
    public void properProcessIsReturnedForRemoteTestsLaunchConfiguration() throws Exception {
        final ILaunchConfiguration config = createConfigurationMock(RemoteRobotLaunchConfiguration.TYPE_ID);
        final ILaunch launch = mock(ILaunch.class);
        when(launch.getLaunchConfiguration()).thenReturn(config);
        when(launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT)).thenReturn("false");

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        final LaunchConfigurationsWrappers processFactory = new LaunchConfigurationsWrappers();
        final IRobotProcess robotProcess = processFactory.newProcess(launch, process, "name", new HashMap<>());

        assertThat(robotProcess).isExactlyInstanceOf(RemoteProcess.class);
        assertThat(robotProcess.getLabel()).isEqualTo("name");
        assertThat(robotProcess.getLaunch()).isSameAs(launch);
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenCreatingProcessForUnknownConfigurationType() throws Exception {
        final ILaunchConfiguration config = createConfigurationMock("unknown");
        final ILaunch launch = mock(ILaunch.class);
        when(launch.getLaunchConfiguration()).thenReturn(config);
        when(launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT)).thenReturn("false");

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        final LaunchConfigurationsWrappers processFactory = new LaunchConfigurationsWrappers();
        processFactory.newProcess(launch, process, "name", new HashMap<>());
    }

    @Test(expected = IllegalStateException.class)
    public void exceptionIsThrown_whenCreatingProcessForProblematicConfiguration() throws Exception {
        final ILaunchConfiguration config = createExceptionsThrowingConfigurationMock();
        final ILaunch launch = mock(ILaunch.class);
        when(launch.getLaunchConfiguration()).thenReturn(config);
        when(launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT)).thenReturn("false");

        final Process process = mock(Process.class);
        when(process.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        when(process.getErrorStream()).thenReturn(new ByteArrayInputStream("".getBytes()));

        final LaunchConfigurationsWrappers processFactory = new LaunchConfigurationsWrappers();
        processFactory.newProcess(launch, process, "name", new HashMap<>());
    }

    private ILaunchConfiguration createConfigurationMock(final String id) throws CoreException {
        final ILaunchConfigurationType configType = mock(ILaunchConfigurationType.class);
        when(configType.getIdentifier()).thenReturn(id);
        final ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
        when(configuration.getType()).thenReturn(configType);

        return configuration;
    }

    private ILaunchConfiguration createExceptionsThrowingConfigurationMock() throws CoreException {
        final ILaunchConfiguration configuration = mock(ILaunchConfiguration.class);
        when(configuration.getType()).thenThrow(CoreException.class);

        return configuration;
    }

}
