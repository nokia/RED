/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.rf.ide.core.execution.RobotAgentEventListener.RobotAgentEventsListenerException;

public interface AgentServerStatusListener {

    void serverEstablished(String host, int port);

    void clientConnected(int clientId);

    void clientConnectionClosed(int clientId);

    void clientConnectionTimedOut(SocketTimeoutException e);

    void clientConnectionError(IOException e);

    void clientEventHandlingError(RobotAgentEventsListenerException e);

}