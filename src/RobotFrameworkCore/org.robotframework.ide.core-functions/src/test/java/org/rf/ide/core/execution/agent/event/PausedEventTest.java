/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.FutureTask;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.PausedEvent.PausedEventResponder;
import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ServerResponse;

public class PausedEventTest {

    @Test
    public void equalsTests() {
        assertThat(new PausedEvent(mock(PausedEventResponder.class)))
                .isEqualTo(new PausedEvent(mock(PausedEventResponder.class)));
        assertThat(new PausedEvent(mock(PausedEventResponder.class))).isNotEqualTo(new Object());
        assertThat(new PausedEvent(mock(PausedEventResponder.class))).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new PausedEvent(mock(PausedEventResponder.class)).hashCode())
                .isEqualTo(new PausedEvent(mock(PausedEventResponder.class)).hashCode());
        assertThat(new PausedEvent(mock(PausedEventResponder.class)).hashCode())
                .isEqualTo(new PausedEvent(null).hashCode());
    }

    @Test
    public void pausedResponsesTests() {
        final AgentClient client = mock(AgentClient.class);
        final PausedEvent event = PausedEvent.from(client);

        final FutureTask<ServerResponse> task = new FutureTask<>(() -> null);
        final ServerResponse response = () -> "s";
        event.responder().respondAsynchronously(task, response);

        verify(client).sendAsync(task, response);
        verifyNoMoreInteractions(client);
    }
}
