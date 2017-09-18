/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import java.util.List;
import java.util.Map;

import org.rf.ide.core.execution.agent.PausingPoint;
import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ServerResponse;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

public final class ShouldContinueEvent {

    public static ShouldContinueEvent from(final AgentClient client, final Map<String, Object> eventMap) {
        final List<?> arguments = (List<?>) eventMap.get("should_continue");
        final Map<String, String> attributes = Events.ensureOrderedMapOfStringsToStrings((Map<?, ?>) arguments.get(0));
        final PausingPoint pausingPoint = PausingPoint.valueOf(attributes.get("pausing_point").toUpperCase());

        return new ShouldContinueEvent(new ShouldContinueEventResponder(client), pausingPoint);
    }


    private final ShouldContinueEventResponder responder;

    private final PausingPoint pausingPoint;

    public ShouldContinueEvent(final ShouldContinueEventResponder responder,
            final PausingPoint pausingPoint) {
        this.responder = responder;
        this.pausingPoint = pausingPoint;
    }

    public PausingPoint getPausingPoint() {
        return pausingPoint;
    }

    public ShouldContinueEventResponder responder() {
        return responder;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj != null && obj.getClass() == ShouldContinueEvent.class) {
            final ShouldContinueEvent that = (ShouldContinueEvent) obj;
            return this.pausingPoint == that.pausingPoint;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return pausingPoint.hashCode();
    }

    public static class ShouldContinueEventResponder {

        private final AgentClient client;

        public ShouldContinueEventResponder(final AgentClient client) {
            this.client = client;
        }

        public void respond(final ServerResponse response) throws ResponseException {
            client.send(response);
        }
    }
}
