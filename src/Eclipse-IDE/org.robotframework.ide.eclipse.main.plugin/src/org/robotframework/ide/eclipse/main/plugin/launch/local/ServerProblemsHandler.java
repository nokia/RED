/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.server.AgentServerStatusListener;


class ServerProblemsHandler implements AgentServerStatusListener {

    @Override
    public void serverEstablished(final String host, final int port) {
        // that's fine, nothing to do
    }

    @Override
    public void clientConnected(final int clientId) {
        // that's fine, nothing to do
    }

    @Override
    public void clientConnectionClosed(final int clientId) {
        // that's fine, nothing to do
    }

    @Override
    public void clientConnectionTimedOut(final SocketTimeoutException e) {
        // throw new RedServerException("Server error: timed out when waiting for agent connection",
        // e);
    }

    @Override
    public void clientConnectionError(final IOException e) {
        // throw new RedServerException("Server error: problem connecting with agent", e);
    }

    @Override
    public void clientEventHandlingError(final RobotAgentEventsListenerException e) {
        throw new RedServerException("Server error: problem handling agent event", e);
    }

    public static class RedServerException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public RedServerException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}
