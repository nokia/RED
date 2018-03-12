/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ConditionEvaluatedEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        ConditionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList(ImmutableMap.of("result", Boolean.TRUE)));
        ConditionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result", newArrayList());
        ConditionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result",
                newArrayList(new Object()));
        ConditionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result", newArrayList(ImmutableMap.of()));
        ConditionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result",
                newArrayList(ImmutableMap.of("arg", "foo")));
        ConditionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_7() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result",
                newArrayList(ImmutableMap.of("result", "foo")));
        ConditionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_8() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result",
                newArrayList(ImmutableMap.of("error", 1)));
        ConditionEvaluatedEvent.from(eventMap);
    }

    @Test
    public void eventWithTrueResultIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result",
                newArrayList(ImmutableMap.of("result", Boolean.TRUE)));
        final ConditionEvaluatedEvent event = ConditionEvaluatedEvent.from(eventMap);

        assertThat(event.getResult()).contains(Boolean.TRUE);
        assertThat(event.getError()).isEmpty();
    }

    @Test
    public void eventWithFalseResultIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result",
                newArrayList(ImmutableMap.of("result", Boolean.FALSE)));
        final ConditionEvaluatedEvent event = ConditionEvaluatedEvent.from(eventMap);

        assertThat(event.getResult()).contains(Boolean.FALSE);
        assertThat(event.getError()).isEmpty();
    }

    @Test
    public void eventWithErrorIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("condition_result",
                newArrayList(ImmutableMap.of("error", "message")));
        final ConditionEvaluatedEvent event = ConditionEvaluatedEvent.from(eventMap);

        assertThat(event.getResult()).isEmpty();
        assertThat(event.getError()).contains("message");
    }

    @Test
    public void equalsTests() {
        assertThat(new ConditionEvaluatedEvent(Boolean.TRUE)).isEqualTo(new ConditionEvaluatedEvent(Boolean.TRUE));
        assertThat(new ConditionEvaluatedEvent(Boolean.FALSE)).isEqualTo(new ConditionEvaluatedEvent(Boolean.FALSE));
        assertThat(new ConditionEvaluatedEvent("error")).isEqualTo(new ConditionEvaluatedEvent("error"));

        assertThat(new ConditionEvaluatedEvent(Boolean.TRUE)).isNotEqualTo(new ConditionEvaluatedEvent(Boolean.FALSE));
        assertThat(new ConditionEvaluatedEvent(Boolean.FALSE)).isNotEqualTo(new ConditionEvaluatedEvent(Boolean.TRUE));
        assertThat(new ConditionEvaluatedEvent("error")).isNotEqualTo(new ConditionEvaluatedEvent(Boolean.TRUE));
        assertThat(new ConditionEvaluatedEvent(Boolean.TRUE)).isNotEqualTo(new ConditionEvaluatedEvent("error"));
        assertThat(new ConditionEvaluatedEvent("error1")).isNotEqualTo(new ConditionEvaluatedEvent("error2"));
        assertThat(new ConditionEvaluatedEvent("error")).isNotEqualTo(new Object());
        assertThat(new ConditionEvaluatedEvent("error")).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new ConditionEvaluatedEvent(Boolean.TRUE).hashCode())
                .isEqualTo(new ConditionEvaluatedEvent(Boolean.TRUE).hashCode());
        assertThat(new ConditionEvaluatedEvent(Boolean.FALSE).hashCode())
                .isEqualTo(new ConditionEvaluatedEvent(Boolean.FALSE).hashCode());
        assertThat(new ConditionEvaluatedEvent("error").hashCode())
                .isEqualTo(new ConditionEvaluatedEvent("error").hashCode());
    }
}
