/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockmodel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;

/**
 * @author Michal Anglart
 *
 */
public class RuntimeEnvironmentsMocks {

    public static RobotRuntimeEnvironment createInvalidPythonEnvironment() {
        final RobotRuntimeEnvironment mock = mock(RobotRuntimeEnvironment.class);
        when(mock.isValidPythonInstallation()).thenReturn(false);
        when(mock.hasRobotInstalled()).thenReturn(false);
        when(mock.getVersion()).thenReturn(null);
        return mock;
    }

    public static RobotRuntimeEnvironment createInvalidRobotEnvironment() {
        final RobotRuntimeEnvironment mock = mock(RobotRuntimeEnvironment.class);
        when(mock.isValidPythonInstallation()).thenReturn(true);
        when(mock.hasRobotInstalled()).thenReturn(false);
        when(mock.getVersion()).thenReturn(null);
        return mock;
    }

    public static RobotRuntimeEnvironment createValidRobotEnvironment(final String version) {
        final RobotRuntimeEnvironment mock = mock(RobotRuntimeEnvironment.class);
        when(mock.isValidPythonInstallation()).thenReturn(true);
        when(mock.hasRobotInstalled()).thenReturn(true);
        when(mock.getVersion()).thenReturn(version);
        return mock;
    }

}
