/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfigurationDelegate.ConsoleData;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.RunConfigurationProvider;

public class RobotLaunchConfigurationDelegateTest {

    private static final String PROJECT_NAME = RobotLaunchConfigurationDelegateTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RobotLaunchConfiguration.TYPE_ID);

    @Test()
    public void pathToExecutableAndUnknownRobotVersionAreUsed_whenPathToExecutableIsSet() throws Exception {
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setExecutableFilePath("some/path/to/script");

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig,
                new LocalProcessInterpreter(SuiteExecutor.Python, "some/path/to/python", "RF 1.2.3"));

        assertThat(consoleData.getProcessLabel()).isEqualTo("some/path/to/script");
        assertThat(consoleData.getSuiteExecutorVersion()).isEqualTo("<unknown>");
    }

    @Test()
    public void pathToPythonAndKnownRobotVersionAreUsed_whenPathToExecutableIsNotSet()
            throws Exception {
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(PROJECT_NAME);
        robotConfig.setExecutableFilePath("");

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig,
                new LocalProcessInterpreter(SuiteExecutor.Python, "some/path/to/python", "RF 1.2.3"));

        assertThat(consoleData.getProcessLabel()).isEqualTo("some/path/to/python");
        assertThat(consoleData.getSuiteExecutorVersion()).isEqualTo("RF 1.2.3");
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(projectName);
        return robotConfig;
    }

    @Test
    public void whenConfigurationVersionIsInvalid_coreExceptionIsThrown() throws Exception {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(PROJECT_NAME);

        final ILaunchConfigurationWorkingCopy launchCopy = configuration.getWorkingCopy();
        launchCopy.setAttribute("Version of configuration", "invalid");

        final RobotLaunchConfigurationDelegate launchDelegate = new RobotLaunchConfigurationDelegate();

        assertThatExceptionOfType(CoreException.class)
                .isThrownBy(() -> launchDelegate.launch(launchCopy, "run", null, null))
                .withMessage(
                        "This configuration is incompatible with RED version you are currently using.%n"
                                + "Expected: %s, but was: %s"
                                + "%n%nResolution: Delete old configurations manually and create the new ones.",
                        RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION, "invalid")
                .withNoCause();
    }
}
