/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.rf.ide.core.execution.RobotAgentEventListener.RobotAgentEventsListenerException;

public class DefaultAgentServerStatusListener implements AgentServerStatusListener {

    @Override
    public void serverEstablished(final String host, final int port) {
        // implement in subclasses
    }

    @Override
    public void clientConnected(final int clientId) {
        // implement in subclasses
    }

    @Override
    public void clientConnectionClosed(final int clientId) {
        // implement in subclasses
    }

    @Override
    public void clientConnectionTimedOut(final SocketTimeoutException e) {
        // implement in subclasses
    }

    @Override
    public void clientConnectionError(final IOException e) {
        // implement in subclasses
    }

    @Override
    public void clientEventHandlingError(final RobotAgentEventsListenerException e) {
        // implement in subclasses
    }
}
