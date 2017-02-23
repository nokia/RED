/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.rf.ide.core.execution.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;
import org.rf.ide.core.execution.server.response.StartExecution;


public class AgentServerTestsStarter extends RobotDefaultAgentEventListener {

    private final Semaphore startSemaphore = new Semaphore(0);

    private AgentClient client;

    @Override
    public void setClient(final AgentClient client) {
        this.client = client;
    }

    @Override
    public void handleAgentIsReadyToStart() {
        try {
            startSemaphore.acquire();
            client.send(new StartExecution());
        } catch (ResponseException | IOException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        } catch (final InterruptedException e) {
            throw new RobotAgentEventsListenerException("Server thread interrupted when waiting for start", e);
        }
    }

    public void allowClientTestsStart() {
        startSemaphore.release();
    }
}
