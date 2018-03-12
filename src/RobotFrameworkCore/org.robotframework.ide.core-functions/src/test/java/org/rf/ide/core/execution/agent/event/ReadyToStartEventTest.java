/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.ReadyToStartEvent.ReadyToStartEventResponder;
import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.StartExecution;

public class ReadyToStartEventTest {

    @Test
    public void equalsTests() {
        assertThat(new ReadyToStartEvent(mock(ReadyToStartEventResponder.class)))
                .isEqualTo(new ReadyToStartEvent(mock(ReadyToStartEventResponder.class)));
        assertThat(new ReadyToStartEvent(mock(ReadyToStartEventResponder.class))).isNotEqualTo(new Object());
        assertThat(new ReadyToStartEvent(mock(ReadyToStartEventResponder.class))).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new ReadyToStartEvent(mock(ReadyToStartEventResponder.class)).hashCode())
                .isEqualTo(new ReadyToStartEvent(mock(ReadyToStartEventResponder.class)).hashCode());
        assertThat(new ReadyToStartEvent(mock(ReadyToStartEventResponder.class)).hashCode())
                .isEqualTo(new ReadyToStartEvent(null).hashCode());
    }

    @Test
    public void readyToStartResponsesTests() {
        final AgentClient client = mock(AgentClient.class);
        final ReadyToStartEvent event = ReadyToStartEvent.from(client);

        event.responder().startExecution();

        verify(client).send(any(StartExecution.class));
        verifyNoMoreInteractions(client);
    }

}
