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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.ProjectProvider;

public class RemoteRobotLaunchConfigurationTest {

    private final static String PROJECT_NAME = RemoteRobotLaunchConfigurationTest.class.getSimpleName();

    private static final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

    private IProject project;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Before
    public void setup() throws CoreException {
        removeAllConfigurations();
        project = projectProvider.getProject();
    }

    @AfterClass
    public static void clean() throws CoreException {
        removeAllConfigurations();
    }

    private static void removeAllConfigurations() throws CoreException {
        final ILaunchConfigurationType type = manager.getLaunchConfigurationType(RobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = manager.getLaunchConfigurations(type);
        for (final ILaunchConfiguration config : launchConfigs) {
            config.delete();
        }
    }

    @Test
    public void defaultConfigurationObtained_whenDefaultConfigurationIsCreated() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        assertThat(robotConfig.getName()).isEqualTo("config_name");
        assertThat(robotConfig.getProjectName()).isEmpty();
    }

    @Test
    public void defaultConfigurationObtained_whenCustomConfigurationFilledDefaults() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugHostValue("1.2.3.4");
        robotConfig.setRemoteDebugPortValue("987");
        robotConfig.setRemoteDebugTimeoutValue("123");
        robotConfig.fillDefaults();
        assertThat(robotConfig.getProjectName()).isEqualTo("");
        assertThat(robotConfig.getRemoteDebugHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getRemoteDebugPort()).isEqualTo(12345);
        assertThat(robotConfig.getRemoteDebugTimeout()).isEqualTo(30_000);
    }

    @Test
    public void remoteProjectIsDefinedDirectly() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        assertThat(robotConfig.isDefiningProjectDirectly()).isTrue();
    }

    @Test
    public void whenServerIpIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server IP cannot be empty");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugHostValue("");
        robotConfig.getRemoteDebugHost();
    }

    @Test
    public void whenPortIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPortValue("");
        robotConfig.getRemoteDebugPort();
    }

    @Test
    public void whenPortIsNotANumber_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port 'abc' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPortValue("abc");
        robotConfig.getRemoteDebugPort();
    }

    @Test
    public void whenPortIsBelowRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '0' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPortValue("0");
        robotConfig.getRemoteDebugPort();
    }

    @Test
    public void whenPortIsAboveRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '65536' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugPortValue("65536");
        robotConfig.getRemoteDebugPort();
    }

    @Test
    public void whenTimeoutIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '' must be an Integer between 1 and 3,600,000");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugTimeoutValue("");
        robotConfig.getRemoteDebugTimeout();
    }

    @Test
    public void whenTimeoutIsNotANumber_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout 'abc' must be an Integer between 1 and 3,600,000");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugTimeoutValue("abc");
        robotConfig.getRemoteDebugTimeout();
    }

    @Test
    public void whenTimeoutIsBelowRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '0' must be an Integer between 1 and 3,600,000");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugTimeoutValue("0");
        robotConfig.getRemoteDebugTimeout();
    }

    @Test
    public void whenTimeoutIsAboveRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '3600001' must be an Integer between 1 and 3,600,000");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugTimeoutValue("3600001");
        robotConfig.getRemoteDebugTimeout();
    }

    @Test
    public void remoteSettingsAreCorrect_whenSet() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteDebugHostValue("192.168.1.21");
        robotConfig.setRemoteDebugPortValue("1234");
        robotConfig.setRemoteDebugTimeoutValue("9876");
        final String host = robotConfig.getRemoteDebugHost();
        final int port = robotConfig.getRemoteDebugPort();
        final int timeout = robotConfig.getRemoteDebugTimeout();
        assertThat(host).isEqualTo("192.168.1.21");
        assertThat(port).isEqualTo(1234);
        assertThat(timeout).isEqualTo(9876);
    }

    @Test
    public void robotProjectObtainedFromConfiguration_whenProjectInWorkspace() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setProjectName(PROJECT_NAME);
        final RobotProject projectFromConfig = robotConfig.getRobotProject();
        assertThat(projectFromConfig).isEqualTo(RedPlugin.getModelManager().getModel().createRobotProject(project));
    }

    @Test
    public void whenProjectNotInWorkspace_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Project 'not_existing' cannot be found in workspace");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setProjectName("not_existing");
        robotConfig.getRobotProject();
    }

    private RemoteRobotLaunchConfiguration createRemoteRobotLaunchConfiguration() throws CoreException {
        final ILaunchConfigurationWorkingCopy configuration = manager
                .getLaunchConfigurationType(RemoteRobotLaunchConfiguration.TYPE_ID).newInstance(null, "config_name");
        return new RemoteRobotLaunchConfiguration(configuration);
    }
}
