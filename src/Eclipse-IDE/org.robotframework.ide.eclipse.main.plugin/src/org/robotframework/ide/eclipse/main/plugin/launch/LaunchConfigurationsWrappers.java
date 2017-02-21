/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

public class LaunchConfigurationsWrappers {

    public static IRobotLaunchConfiguration robotLaunchConfiguration(final ILaunchConfiguration config) {
        try {
            if (RemoteRobotLaunchConfiguration.TYPE_ID.equals(config.getType().getIdentifier())) {
                return new RemoteRobotLaunchConfiguration(config);
            } else if (ScriptRobotLaunchConfiguration.TYPE_ID.equals(config.getType().getIdentifier())) {
                return new ScriptRobotLaunchConfiguration(config);
            } else if (RobotLaunchConfiguration.TYPE_ID.equals(config.getType().getIdentifier())) {
                return new RobotLaunchConfiguration(config);
            }
        } catch (final CoreException e) {
            throw new IllegalArgumentException("Unrecognized configuration type", e);
        }
        throw new IllegalArgumentException("Unrecognized configuration type");
    }

    public static IRemoteRobotLaunchConfiguration remoteLaunchConfiguration(final ILaunchConfiguration config) {
        try {
            if (RemoteRobotLaunchConfiguration.TYPE_ID.equals(config.getType().getIdentifier())) {
                return new RemoteRobotLaunchConfiguration(config);
            } else if (ScriptRobotLaunchConfiguration.TYPE_ID.equals(config.getType().getIdentifier())) {
                return new ScriptRobotLaunchConfiguration(config);
            }
        } catch (final CoreException e) {
            throw new IllegalArgumentException("Unrecognized configuration type", e);
        }
        throw new IllegalArgumentException("Unrecognized configuration type");
    }
}
