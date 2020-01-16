/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.environment.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfigurationDelegate.ConsoleData;
import org.robotframework.red.junit.jupiter.LaunchConfig;
import org.robotframework.red.junit.jupiter.LaunchConfigExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, LaunchConfigExtension.class })
public class RobotLaunchConfigurationDelegateTest {

    @Project
    static IProject project;

    @LaunchConfig(typeId = RobotLaunchConfiguration.TYPE_ID, name = "robot")
    ILaunchConfiguration launchCfg;

    @Test
    public void pathToExecutableAndUnknownRobotVersionAreUsed_whenPathToExecutableIsSet() throws Exception {
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());
        robotConfig.setExecutableFilePath("some/path/to/script");

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig,
                new LocalProcessInterpreter(SuiteExecutor.Python, "some/path/to/python", "RF 1.2.3"));

        assertThat(consoleData.getProcessLabel()).isEqualTo("some/path/to/script");
        assertThat(consoleData.getSuiteExecutorVersion()).isEqualTo("<unknown>");
    }

    @Test
    public void pathToPythonAndKnownRobotVersionAreUsed_whenPathToExecutableIsNotSet() throws Exception {
        final RobotLaunchConfiguration robotConfig = createRobotLaunchConfiguration(project.getName());
        robotConfig.setExecutableFilePath("");

        final ConsoleData consoleData = RobotLaunchConfigurationDelegate.ConsoleData.create(robotConfig,
                new LocalProcessInterpreter(SuiteExecutor.Python, "some/path/to/python", "RF 1.2.3"));

        assertThat(consoleData.getProcessLabel()).isEqualTo("some/path/to/python");
        assertThat(consoleData.getSuiteExecutorVersion()).isEqualTo("RF 1.2.3");
    }

    private RobotLaunchConfiguration createRobotLaunchConfiguration(final String projectName) throws CoreException {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchCfg);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(projectName);
        return robotConfig;
    }

    @Test
    public void whenConfigurationVersionIsInvalid_coreExceptionIsThrown() throws Exception {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchCfg);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(project.getName());

        final ILaunchConfigurationWorkingCopy launchCopy = launchCfg.getWorkingCopy();
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
