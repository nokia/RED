/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.util.concurrent.Semaphore;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.agent.event.ReadyToStartEvent;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;


public class AgentServerTestsStarter extends RobotDefaultAgentEventListener {

    private final Semaphore startSemaphore = new Semaphore(0);

    private final TestsMode mode;

    public AgentServerTestsStarter(final TestsMode mode) {
        this.mode = mode;
    }

    @Override
    public void handleAgentInitializing(final AgentInitializingEvent event) {
        try {
            event.responder().initialize(mode, true);
        } catch (final ResponseException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
    }

    @Override
    public void handleAgentIsReadyToStart(final ReadyToStartEvent event) {
        try {
            startSemaphore.acquire();
            event.responder().startExecution();
        } catch (final ResponseException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        } catch (final InterruptedException e) {
            throw new RobotAgentEventsListenerException("Server thread interrupted when waiting for start", e);
        }
    }

    public void allowClientTestsStart() {
        startSemaphore.release();
    }
}
