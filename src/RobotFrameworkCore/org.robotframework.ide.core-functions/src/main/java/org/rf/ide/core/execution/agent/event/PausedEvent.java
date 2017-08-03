/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.concurrent.FutureTask;

import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ServerResponse;

public final class PausedEvent {

    private final PausedEventResponder responder;

    public static PausedEvent from(final AgentClient client) {
        return new PausedEvent(new PausedEventResponder(client));
    }

    public PausedEvent(final PausedEventResponder responder) {
        this.responder = responder;
    }

    public PausedEventResponder responder() {
        return responder;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj != null && obj.getClass() == PausedEvent.class;
    }

    @Override
    public int hashCode() {
        // because those object are in fact constant
        return 31;
    }

    public static class PausedEventResponder {

        private final AgentClient client;

        public PausedEventResponder(final AgentClient client) {
            this.client = client;
        }

        public void respondAsynchronously(final FutureTask<ServerResponse> futureResponse,
                final ServerResponse responseOnError) {
            client.sendAsync(futureResponse, responseOnError);
        }
    }
}
