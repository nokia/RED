/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import org.rf.ide.core.execution.RobotDefaultAgentEventListener;

/**
 * Objects of this class can be used to keep server alive: this can be
 * the only listener which may return true when isHandlingEvents() is called,
 * so it may be used as switch to stop processing agent events.
 * 
 * @author anglart
 */
public class AgentServerKeepAlive extends RobotDefaultAgentEventListener {

    private boolean isHandlingEvents = true;

    @Override
    public boolean isHandlingEvents() {
        return isHandlingEvents;
    }

    public void stopHandlingEvents() {
        isHandlingEvents = false;
    }

    @Override
    public void handleClosed() {
        isHandlingEvents = false;
    }
}
