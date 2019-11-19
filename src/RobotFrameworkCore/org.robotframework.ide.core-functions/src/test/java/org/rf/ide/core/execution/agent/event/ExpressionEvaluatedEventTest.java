/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;

import com.google.common.collect.ImmutableMap;

public class ExpressionEvaluatedEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        ExpressionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList(ImmutableMap.of("result", Boolean.TRUE)));
        ExpressionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("expression_result", newArrayList());
        ExpressionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("expression_result", newArrayList(new Object()));
        ExpressionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("expression_result", newArrayList(ImmutableMap.of()));
        ExpressionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("expression_result",
                newArrayList(ImmutableMap.of("arg", "foo")));
        ExpressionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_7() {
        final Map<String, Object> eventMap = ImmutableMap.of("expression_result",
                newArrayList(ImmutableMap.of("type", "ROBOT")));
        ExpressionEvaluatedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_8() {
        final Map<String, Object> eventMap = ImmutableMap.of("expression_result",
                newArrayList(ImmutableMap.of("type", "ROBOT", "id", 123)));
        ExpressionEvaluatedEvent.from(eventMap);
    }

    @Test
    public void eventWithResultIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("expression_result",
                newArrayList(ImmutableMap.of("type", "ROBOT", "id", 123, "result", "x")));
        final ExpressionEvaluatedEvent event = ExpressionEvaluatedEvent.from(eventMap);

        assertThat(event.getId()).isEqualTo(123);
        assertThat(event.getType()).isEqualTo(ExpressionType.ROBOT);
        assertThat(event.getResult()).contains("x");
        assertThat(event.getError()).isEmpty();
    }

    @Test
    public void eventWithErrorIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("expression_result",
                newArrayList(ImmutableMap.of("type", "ROBOT", "id", 123, "error", "exception")));
        final ExpressionEvaluatedEvent event = ExpressionEvaluatedEvent.from(eventMap);

        assertThat(event.getId()).isEqualTo(123);
        assertThat(event.getType()).isEqualTo(ExpressionType.ROBOT);
        assertThat(event.getResult()).isEmpty();
        assertThat(event.getError()).contains("exception");
    }

    @Test
    public void equalsTests() {
        assertThat(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error"))
                .isEqualTo(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error"));
        assertThat(new ExpressionEvaluatedEvent(2, ExpressionType.PYTHON, "result", null))
                .isEqualTo(new ExpressionEvaluatedEvent(2, ExpressionType.PYTHON, "result", null));
        assertThat(new ExpressionEvaluatedEvent(3, ExpressionType.VARIABLE, null, "error"))
                .isEqualTo(new ExpressionEvaluatedEvent(3, ExpressionType.VARIABLE, null, "error"));

        assertThat(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error"))
                .isNotEqualTo(new ExpressionEvaluatedEvent(2, ExpressionType.ROBOT, "result", "error"));
        assertThat(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error"))
                .isNotEqualTo(new ExpressionEvaluatedEvent(1, ExpressionType.PYTHON, "result", "error"));
        assertThat(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error"))
                .isNotEqualTo(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result1", "error"));
        assertThat(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error"))
                .isNotEqualTo(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error1"));
        assertThat(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error")).isNotEqualTo(new Object());
        assertThat(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error")).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error").hashCode())
                .isEqualTo(new ExpressionEvaluatedEvent(1, ExpressionType.ROBOT, "result", "error").hashCode());
        assertThat(new ExpressionEvaluatedEvent(2, ExpressionType.PYTHON, "result", null).hashCode())
                .isEqualTo(new ExpressionEvaluatedEvent(2, ExpressionType.PYTHON, "result", null).hashCode());
        assertThat(new ExpressionEvaluatedEvent(3, ExpressionType.VARIABLE, null, "error").hashCode())
                .isEqualTo(new ExpressionEvaluatedEvent(3, ExpressionType.VARIABLE, null, "error").hashCode());
    }
}
