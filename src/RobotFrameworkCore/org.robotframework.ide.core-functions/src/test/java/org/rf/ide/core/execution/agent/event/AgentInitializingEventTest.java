/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent.AgentInitializingEventResponder;
import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.InitializeAgent;

public class AgentInitializingEventTest {

    @Test
    public void equalsTests() {
        assertThat(new AgentInitializingEvent(mock(AgentInitializingEventResponder.class)))
                .isEqualTo(new AgentInitializingEvent(mock(AgentInitializingEventResponder.class)));
        assertThat(new AgentInitializingEvent(mock(AgentInitializingEventResponder.class))).isNotEqualTo(new Object());
        assertThat(new AgentInitializingEvent(mock(AgentInitializingEventResponder.class))).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new AgentInitializingEvent(mock(AgentInitializingEventResponder.class)).hashCode())
                .isEqualTo(new AgentInitializingEvent(mock(AgentInitializingEventResponder.class)).hashCode());
        assertThat(new AgentInitializingEvent(mock(AgentInitializingEventResponder.class)).hashCode())
                .isEqualTo(new AgentInitializingEvent(null).hashCode());
    }

    @Test
    public void initializationResponsesTests() {
        final AgentClient client = mock(AgentClient.class);
        final AgentInitializingEvent event = AgentInitializingEvent.from(client);

        event.responder().initialize(TestsMode.RUN, true);
        event.responder().initialize(TestsMode.RUN, false);
        event.responder().initialize(TestsMode.DEBUG, true);
        event.responder().initialize(TestsMode.DEBUG, false);
        
        verify(client, times(4)).send(any(InitializeAgent.class));
        verifyNoMoreInteractions(client);
    }

}
