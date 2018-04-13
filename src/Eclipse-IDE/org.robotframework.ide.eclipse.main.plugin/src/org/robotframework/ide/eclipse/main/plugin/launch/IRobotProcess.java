/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISuspendResume;
import org.rf.ide.core.execution.debug.UserProcessController;

public interface IRobotProcess extends IProcess, IDisconnect, ISuspendResume {

    public static final String PROCESS_USER_CONTROLLER = "PROCESS_USER_CONTROLLER";

    RobotConsoleFacade provideConsoleFacade(String processLabel);

    void onTerminate(Runnable operation);

    void setPythonExecutablePath(String pythonExecutablePath);

    void setConnectedToTests(boolean isConnected);

    void setUserProcessController(UserProcessController controller);

    UserProcessController getUserProcessController();

    @Override
    void suspend();

    void suspended();

    @Override
    void resume();

    void resumed();

    @Override
    void disconnect();

    void interrupt();

    void terminated();
}
