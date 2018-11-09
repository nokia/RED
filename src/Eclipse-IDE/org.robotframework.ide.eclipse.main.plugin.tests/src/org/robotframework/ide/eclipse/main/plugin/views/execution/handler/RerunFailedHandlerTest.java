/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rf.ide.core.execution.agent.event.OutputFileEvent;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusTracker;
import org.robotframework.red.junit.RunConfigurationProvider;

import com.google.common.collect.ImmutableMap;

public class RerunFailedHandlerTest {

    @Rule
    public RunConfigurationProvider robotRunConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void coreExceptionIsThrown_whenConfigurationIsNull() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = null;

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunFailedHandler.E4ShowFailedOnlyHandler.getConfig(launch))
                .withMessage("Launch configuration does not exist")
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenConfigurationDoesNotExist() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(false);

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunFailedHandler.E4ShowFailedOnlyHandler.getConfig(launch))
                .withMessage("Launch configuration does not exist")
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenConfigurationExistsButOutputFileIsNotSet() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunFailedHandler.E4ShowFailedOnlyHandler.getConfig(launch))
                .withMessage("Output file does not exist")
                .withNoCause();
    }

    @Test
    public void coreExceptionIsThrown_whenConfigurationExistsButOutputFileDoesNotExist() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);
        setOutputFilePath(launch, new URI("file:///not_existing_output.xml"));

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> RerunFailedHandler.E4ShowFailedOnlyHandler.getConfig(launch))
                .withMessage("Output file does not exist")
                .withNoCause();
    }

    @Test
    public void configurationForFailedTestsRerunIsReturned_whenOutputFileExists() throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);

        final File output = tempFolder.newFile("existing_output.xml");
        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);
        setOutputFilePath(launch, output.toURI());

        assertThat(RerunFailedHandler.E4ShowFailedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Robot arguments", "-R " + output.getAbsolutePath())
                .containsEntry("Test suites", new HashMap<>());
    }

    @Test
    public void configurationForFailedTestRerunIsReturned_whenOutputFileExistsAndCustomRobotArgumentsAreSet()
            throws Exception {
        final ILaunchConfigurationWorkingCopy configuration = createConfigurationSpy();
        when(configuration.exists()).thenReturn(true);
        configuration.setAttribute("Robot arguments", "-a -b -c");
        configuration.setAttribute("Test suites", ImmutableMap.of("Suite", "test_a::test_b::test_c"));

        final File output = tempFolder.newFile("existing_output.xml");
        final RobotTestsLaunch launch = new RobotTestsLaunch(configuration);
        setOutputFilePath(launch, output.toURI());

        assertThat(RerunFailedHandler.E4ShowFailedOnlyHandler.getConfig(launch).getAttributes())
                .containsEntry("Robot arguments", "-a -b -c -R " + output.getAbsolutePath())
                .containsEntry("Test suites", new HashMap<>());
    }

    private ILaunchConfigurationWorkingCopy createConfigurationSpy() throws CoreException {
        return spy(robotRunConfigurationProvider.create("robot").getWorkingCopy());
    }

    private void setOutputFilePath(final RobotTestsLaunch launch, final URI path) {
        final ExecutionStatusTracker tracker = new ExecutionStatusTracker(launch);
        tracker.eventsProcessingAboutToStart();
        tracker.handleOutputFile(new OutputFileEvent(path));
    }

}
