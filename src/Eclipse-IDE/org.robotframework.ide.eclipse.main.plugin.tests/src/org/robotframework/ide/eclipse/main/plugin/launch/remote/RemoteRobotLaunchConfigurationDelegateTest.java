/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.red.junit.RunConfigurationProvider;

public class RemoteRobotLaunchConfigurationDelegateTest {

    private static final String PROJECT_NAME = RemoteRobotLaunchConfigurationDelegateTest.class.getSimpleName();

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Rule
    public RunConfigurationProvider runConfigurationProvider = new RunConfigurationProvider(
            RemoteRobotLaunchConfiguration.TYPE_ID);

    @Test
    public void whenConfigurationVersionIsInvalid_coreExceptionIsThrown() throws Exception {
        thrown.expect(CoreException.class);
        thrown.expectMessage("This configuration is incompatible with RED version you are currently using."
                + "\nExpected: " + RobotLaunchConfiguration.CURRENT_CONFIGURATION_VERSION + ", but was: invalid"
                + "\n\nResolution: Delete old configurations manually and create the new ones.");

        final ILaunchConfiguration configuration = runConfigurationProvider.create("robot");
        final RemoteRobotLaunchConfiguration robotConfig = new RemoteRobotLaunchConfiguration(configuration);
        robotConfig.fillDefaults();
        robotConfig.setProjectName(PROJECT_NAME);

        final ILaunchConfigurationWorkingCopy launchCopy = configuration.getWorkingCopy();
        launchCopy.setAttribute("Version of configuration", "invalid");

        final RemoteRobotLaunchConfigurationDelegate launchDelegate = new RemoteRobotLaunchConfigurationDelegate();
        launchDelegate.launch(launchCopy, "run", null, null);
    }

}
