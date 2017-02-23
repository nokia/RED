/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IProcessFactory;
import org.robotframework.ide.eclipse.main.plugin.launch.local.LocalProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.remote.RemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.script.ScriptRobotLaunchConfiguration;

public class LaunchConfigurationsWrappers implements IProcessFactory {

    public static final String FACTORY_ID = "org.robotframework.ide.launch.process.factory";

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

    @Override
    public IRobotProcess newProcess(final ILaunch launch, final Process process, final String name,
            final Map<String, String> attributes) {
        try {
            final String id = launch.getLaunchConfiguration().getType().getIdentifier();
            if (RobotLaunchConfiguration.TYPE_ID.equals(id)) {
                return new LocalProcess(launch, process, name, attributes);
            } else if (RemoteRobotLaunchConfiguration.TYPE_ID.equals(id)) {
                return new RemoteProcess(launch, () -> {}, name);
            } else if (ScriptRobotLaunchConfiguration.TYPE_ID.equals(id)) {
                // FIXME : return local process
                return null;
            }
        } catch (final CoreException e) {
            throw new IllegalStateException("Unable to create process representation", e);
        }
        throw new IllegalStateException("Unable to create process representation");
    }
}
