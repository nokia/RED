/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.mockmodel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;

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
        when(mock.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(mock.getFile()).thenReturn(new File("some/path/to/python"));
        when(mock.getPythonExecutablePath()).thenReturn("some/path/to/python");
        return mock;
    }

    public static RobotRuntimeEnvironment createValidRobotEnvironment(final String version) {
        final RobotRuntimeEnvironment mock = mock(RobotRuntimeEnvironment.class);
        when(mock.isValidPythonInstallation()).thenReturn(true);
        when(mock.hasRobotInstalled()).thenReturn(true);
        when(mock.getVersion()).thenReturn(version);
        when(mock.getInterpreter()).thenReturn(SuiteExecutor.Python);
        when(mock.getFile()).thenReturn(new File("some/path/to/python"));
        when(mock.getPythonExecutablePath()).thenReturn("some/path/to/python");
        return mock;
    }

    public static RobotRuntimeEnvironment createValidJythonRobotEnvironment(final String version) {
        final RobotRuntimeEnvironment mock = mock(RobotRuntimeEnvironment.class);
        when(mock.isValidPythonInstallation()).thenReturn(true);
        when(mock.hasRobotInstalled()).thenReturn(true);
        when(mock.getVersion()).thenReturn(version);
        when(mock.getInterpreter()).thenReturn(SuiteExecutor.Jython);
        when(mock.getFile()).thenReturn(new File("some/path/to/jython"));
        when(mock.getPythonExecutablePath()).thenReturn("some/path/to/jython");
        return mock;
    }

}
