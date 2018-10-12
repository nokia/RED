/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.red.junit.RunConfigurationProvider;

public class RemoteRobotLaunchConfigurationDelegateTest {

    private static final String PROJECT_NAME = RemoteRobotLaunchConfigurationDelegateTest.class.getSimpleName();

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RemoteRobotLaunchConfiguration.TYPE_ID);

    @Test
    public void whenConfigurationVersionIsInvalid_coreExceptionIsThrown() throws Exception {
        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RemoteRobotLaunchConfiguration robotConfig = new RemoteRobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(PROJECT_NAME);

        final ILaunchConfigurationWorkingCopy launchCopy = configuration.getWorkingCopy();
        launchCopy.setAttribute("Version of configuration", "invalid");

        final RemoteRobotLaunchConfigurationDelegate launchDelegate = new RemoteRobotLaunchConfigurationDelegate();

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
