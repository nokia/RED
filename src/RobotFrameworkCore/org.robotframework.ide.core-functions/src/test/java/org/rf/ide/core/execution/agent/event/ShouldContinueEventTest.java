/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.EnumSet;
import java.util.Map;

import org.junit.Test;
import org.rf.ide.core.execution.agent.PausingPoint;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent.ShouldContinueEventResponder;
import org.rf.ide.core.execution.server.AgentClient;
import org.rf.ide.core.execution.server.response.ServerResponse;

import com.google.common.collect.ImmutableMap;

public class ShouldContinueEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        ShouldContinueEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("should_continue", "foo");
        ShouldContinueEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("should_continue", newArrayList());
        ShouldContinueEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("should_continue", newArrayList("foo"));
        ShouldContinueEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("should_continue",
                newArrayList(ImmutableMap.of()));
        ShouldContinueEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("should_continue",
                newArrayList(ImmutableMap.of("pausing_point", "foo")));
        ShouldContinueEvent.from(mock(AgentClient.class), eventMap);
    }

    @Test
    public void eventIsProperlyConstructed() {
        for (final PausingPoint pausingPoint : EnumSet.allOf(PausingPoint.class)) {
            final Map<String, Object> eventMap = ImmutableMap.of("should_continue",
                    newArrayList(ImmutableMap.of("pausing_point", pausingPoint.name())));
            final ShouldContinueEvent event = ShouldContinueEvent.from(mock(AgentClient.class), eventMap);

            assertThat(event.getPausingPoint()).isEqualTo(pausingPoint);
        }
    }

    @Test
    public void equalsTests() {
        assertThat(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.PRE_START_KEYWORD))
                .isEqualTo(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class),
                        PausingPoint.PRE_START_KEYWORD));
        assertThat(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.START_KEYWORD))
                .isEqualTo(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class),
                        PausingPoint.START_KEYWORD));
        assertThat(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.PRE_END_KEYWORD))
                .isEqualTo(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class),
                        PausingPoint.PRE_END_KEYWORD));
        assertThat(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.END_KEYWORD))
                .isEqualTo(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class),
                        PausingPoint.END_KEYWORD));

        assertThat(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.START_KEYWORD))
                .isNotEqualTo(
                        new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.END_KEYWORD));
        assertThat(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.END_KEYWORD))
                .isNotEqualTo(
                        new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.START_KEYWORD));
        assertThat(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.START_KEYWORD))
                .isNotEqualTo(new Object());
        assertThat(new ShouldContinueEvent(mock(ShouldContinueEventResponder.class), PausingPoint.START_KEYWORD))
                .isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        final ShouldContinueEventResponder responder = mock(ShouldContinueEventResponder.class);
        assertThat(new ShouldContinueEvent(responder, PausingPoint.START_KEYWORD).hashCode())
                .isEqualTo(new ShouldContinueEvent(responder, PausingPoint.START_KEYWORD).hashCode());
        assertThat(new ShouldContinueEvent(responder, PausingPoint.START_KEYWORD).hashCode())
                .isEqualTo(new ShouldContinueEvent(null, PausingPoint.START_KEYWORD).hashCode());
    }

    @Test
    public void shouldContinueResponsesTests() {
        final Map<String, Object> eventMap = ImmutableMap.of("should_continue",
                newArrayList(ImmutableMap.of("pausing_point", PausingPoint.START_KEYWORD.name())));

        final AgentClient client = mock(AgentClient.class);
        final ShouldContinueEvent event = ShouldContinueEvent.from(client, eventMap);

        final ServerResponse response = () -> "s";
        event.responder().respond(response);

        verify(client).send(response);
        verifyNoMoreInteractions(client);
    }
}
