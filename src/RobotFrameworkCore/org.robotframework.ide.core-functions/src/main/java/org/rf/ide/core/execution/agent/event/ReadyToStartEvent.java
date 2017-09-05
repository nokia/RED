/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;
import org.rf.ide.core.execution.server.response.StartExecution;

public final class ReadyToStartEvent {

    public static ReadyToStartEvent from(final AgentClient client) {
        return new ReadyToStartEvent(new ReadyToStartEventResponder(client));
    }


    private final ReadyToStartEventResponder responder;

    public ReadyToStartEvent(final ReadyToStartEventResponder responder) {
        this.responder = responder;
    }

    public ReadyToStartEventResponder responder() {
        return responder;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj.getClass() == ReadyToStartEvent.class;
    }

    @Override
    public int hashCode() {
        // because those object are in fact constant
        return ReadyToStartEvent.class.hashCode();
    }

    public static class ReadyToStartEventResponder {

        private final AgentClient client;

        private ReadyToStartEventResponder(final AgentClient client) {
            this.client = client;
        }

        public void startExecution() throws ResponseException {
            client.send(new StartExecution());
        }
    }

}
