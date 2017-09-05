/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.InitializeAgent;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

public final class AgentInitializingEvent {

    public static AgentInitializingEvent from(final AgentClient client) {
        return new AgentInitializingEvent(new AgentInitializingEventResponder(client));
    }


    private final AgentInitializingEventResponder responder;

    public AgentInitializingEvent(final AgentInitializingEventResponder responder) {
        this.responder = responder;
    }

    public AgentInitializingEventResponder responder() {
        return responder;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj.getClass() == AgentInitializingEvent.class;
    }

    @Override
    public int hashCode() {
        // because those object are in fact constant
        return AgentInitializingEvent.class.hashCode();
    }

    public static class AgentInitializingEventResponder {

        private final AgentClient client;

        private AgentInitializingEventResponder(final AgentClient client) {
            this.client = client;
        }

        public void initialize(final TestsMode mode, final boolean agentShouldWaitForSignal) throws ResponseException {
            client.send(new InitializeAgent(mode, agentShouldWaitForSignal));
        }
    }
}
