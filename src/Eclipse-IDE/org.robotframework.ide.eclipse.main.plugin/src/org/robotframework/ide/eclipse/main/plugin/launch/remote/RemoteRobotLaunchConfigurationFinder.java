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
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;

public class RemoteRobotLaunchConfigurationFinder {

    public static ILaunchConfigurationWorkingCopy findSameAs(final ILaunchConfiguration configuration)
            throws CoreException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final ILaunchConfigurationType launchConfigurationType = launchManager
                .getLaunchConfigurationType(RemoteRobotLaunchConfiguration.TYPE_ID);
        final ILaunchConfiguration[] launchConfigs = launchManager.getLaunchConfigurations(launchConfigurationType);
        for (final ILaunchConfiguration config : launchConfigs) {
            if (contentEquals(configuration, config)) {
                return asWorkingCopy(config);
            }
        }
        return null;
    }

    private static ILaunchConfigurationWorkingCopy asWorkingCopy(final ILaunchConfiguration config) {
        if (config.isWorkingCopy()) {
            return (ILaunchConfigurationWorkingCopy) config;
        } else {
            try {
                return config.getWorkingCopy();
            } catch (final CoreException e) {
                return null;
            }
        }
    }

    private static boolean contentEquals(final ILaunchConfiguration config1, final ILaunchConfiguration config2)
            throws CoreException {
        final RemoteRobotLaunchConfiguration rConfig1 = new RemoteRobotLaunchConfiguration(config1);
        final RemoteRobotLaunchConfiguration rConfig2 = new RemoteRobotLaunchConfiguration(config2);
        return rConfig1.getProjectName().equals(rConfig2.getProjectName())
                && rConfig1.getAgentConnectionHost().equals(rConfig2.getAgentConnectionHost())
                && rConfig1.getAgentConnectionPort() == rConfig2.getAgentConnectionPort()
                && rConfig1.getAgentConnectionTimeout() == rConfig2.getAgentConnectionTimeout();
    }
}
