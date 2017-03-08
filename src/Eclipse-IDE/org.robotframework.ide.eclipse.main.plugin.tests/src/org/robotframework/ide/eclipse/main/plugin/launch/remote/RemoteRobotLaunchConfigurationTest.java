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
    public void defaultConfigurationObtained_whenCustomConfigurationIsFilledWithDefaults() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteHostValue("1.2.3.4");
        robotConfig.setRemotePortValue("987");
        robotConfig.setRemoteTimeoutValue("123");
        robotConfig.fillDefaults();
        assertThat(robotConfig.getProjectName()).isEqualTo("");
        assertThat(robotConfig.isRemoteAgent()).isFalse();
        assertThat(robotConfig.getRemoteHost()).isEqualTo("127.0.0.1");
        assertThat(robotConfig.getRemotePort()).isBetween(1, 65_535);
        assertThat(robotConfig.getRemoteTimeout()).isEqualTo(30);
    }

    @Test
    public void remoteProjectIsDefinedDirectly() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        assertThat(robotConfig.isDefiningProjectDirectly()).isTrue();
    }

    @Test
    public void projectIsReturned_whenAskedForResourcesUnderDebug() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setProjectName(PROJECT_NAME);
        assertThat(robotConfig.getResourcesUnderDebug()).containsExactly(project);
    }

    @Test
    public void whenServerIpIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server IP cannot be empty");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteHostValue("");
        robotConfig.getRemoteHost();
    }

    @Test
    public void whenPortIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemotePortValue("");
        robotConfig.getRemotePort();
    }

    @Test
    public void whenPortIsNotANumber_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port 'abc' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemotePortValue("abc");
        robotConfig.getRemotePort();
    }

    @Test
    public void whenPortIsBelowRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '0' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemotePortValue("0");
        robotConfig.getRemotePort();
    }

    @Test
    public void whenPortIsAboveRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Server port '65536' must be an Integer between 1 and 65,535");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemotePortValue("65536");
        robotConfig.getRemotePort();
    }

    @Test
    public void whenTimeoutIsEmpty_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '' must be an Integer between 1 and 3,600");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteTimeoutValue("");
        robotConfig.getRemoteTimeout();
    }

    @Test
    public void whenTimeoutIsNotANumber_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout 'abc' must be an Integer between 1 and 3,600");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteTimeoutValue("abc");
        robotConfig.getRemoteTimeout();
    }

    @Test
    public void whenTimeoutIsBelowRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '0' must be an Integer between 1 and 3,600");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteTimeoutValue("0");
        robotConfig.getRemoteTimeout();
    }

    @Test
    public void whenTimeoutIsAboveRange_coreExceptionIsThrown() throws CoreException {
        thrown.expect(CoreException.class);
        thrown.expectMessage("Connection timeout '3601' must be an Integer between 1 and 3,600");

        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteTimeoutValue("3601");
        robotConfig.getRemoteTimeout();
    }

    @Test
    public void remoteSettingsAreCorrect_whenSet() throws CoreException {
        final RemoteRobotLaunchConfiguration robotConfig = createRemoteRobotLaunchConfiguration();
        robotConfig.setRemoteHostValue("192.168.1.21");
        robotConfig.setRemotePortValue("1234");
        robotConfig.setRemoteTimeoutValue("567");
        assertThat(robotConfig.getRemoteHost()).isEqualTo("192.168.1.21");
        assertThat(robotConfig.getRemotePort()).isEqualTo(1234);
        assertThat(robotConfig.getRemoteTimeout()).isEqualTo(567);
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
