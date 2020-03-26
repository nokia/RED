/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.menus.UIElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent.ExecutionMode;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.red.junit.jupiter.LaunchConfig;
import org.robotframework.red.junit.jupiter.LaunchConfigExtension;

@ExtendWith(LaunchConfigExtension.class)
public class RerunHandlerTest {

    @LaunchConfig(typeId = RobotLaunchConfiguration.TYPE_ID, name = "robot")
    ILaunchConfiguration launchConfig;

    @Test
    public void coreExceptionIsThrown_whenConfigurationIsNull() throws Exception {
        final ILaunchConfiguration configuration = null;

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunHandler.E4ShowFailedOnlyHandler.getConfig(launch))
                .withMessage("Launch configuration does not exist")
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenConfigurationDoesNotExist() throws Exception {
        final ILaunchConfiguration configuration = spy(launchConfig);
        when(configuration.exists()).thenReturn(false);

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunHandler.E4ShowFailedOnlyHandler.getConfig(launch))
                .withMessage("Launch configuration does not exist")
                .withNoCause();
    }

    @Test
    public void configurationForTestsRerunIsReturned_whenExists() throws Exception {
        final ILaunchConfiguration configuration = spy(launchConfig);
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);

        assertThat(RerunHandler.E4ShowFailedOnlyHandler.getConfig(launch)).isSameAs(configuration);
    }

    @Test
    public void rerunButtonTooltipIsInTestsMode_whenExecutionContainsTests() {
        final UIElement element = mock(UIElement.class);
        final RerunHandler handler = new RerunHandler();
        handler.setTooltip(element, ExecutionMode.TESTS);

        verify(element).setTooltip("Rerun Tests");
    }

    @Test
    public void rerunButtonTooltipIsInTasksMode_whenExecutionContainsTasks() {
        final UIElement element = mock(UIElement.class);
        final RerunHandler handler = new RerunHandler();
        handler.setTooltip(element, ExecutionMode.TASKS);

        verify(element).setTooltip("Rerun Tasks");
    }
}
