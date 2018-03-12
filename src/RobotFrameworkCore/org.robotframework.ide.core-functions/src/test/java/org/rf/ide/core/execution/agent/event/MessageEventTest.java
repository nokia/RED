/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.rf.ide.core.execution.agent.LogLevel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public class MessageEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_message1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        MessageEvent.fromMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_message2() {
        final Map<String, Object> eventMap = ImmutableMap.of("message", "m");
        MessageEvent.fromMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_message3() {
        final Map<String, Object> eventMap = ImmutableMap.of("message", newArrayList());
        MessageEvent.fromMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_message4() {
        final Map<String, Object> eventMap = ImmutableMap.of("message", newArrayList("m"));
        MessageEvent.fromMessage(eventMap);
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_message5() {
        final ImmutableMap<String, ? extends Object> template = ImmutableMap.of("message", "msg", "level", "error",
                "timestamp", "ts");

        // from all combinations remove this which consist of all keys
        final Set<Set<String>> allKeysCombinations = newHashSet(
                Sets.powerSet(newHashSet("message", "level", "timestamp")));
        allKeysCombinations.remove(newHashSet("message", "level", "timestamp"));

        for (final Set<String> combination : allKeysCombinations) {
            final Map<String, Object> attributes = new HashMap<>();
            for (final String key : combination) {
                attributes.put(key, template.get(key));
            }
            final Map<String, Object> eventMap = ImmutableMap.of("message", newArrayList(attributes));

            assertThatIllegalArgumentException().isThrownBy(() -> MessageEvent.fromMessage(eventMap))
                    .withMessage("Message event has to have the content, timestamp and level")
                    .withNoCause();
        }
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_message6() {
        final Map<String, Object> eventMap = ImmutableMap.of("message",
                newArrayList(ImmutableMap.of("message", "msg", "timestamp", "ts", "level", "unknown")));
        MessageEvent.fromMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_message7() {
        final Map<String, Object> eventMap = ImmutableMap.of("log_message",
                newArrayList(ImmutableMap.of("message", "msg", "timestamp", "ts", "level", "error")));
        MessageEvent.fromMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_logMessage1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        MessageEvent.fromLogMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_logMessage2() {
        final Map<String, Object> eventMap = ImmutableMap.of("log_message", "m");
        MessageEvent.fromLogMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_logMessage3() {
        final Map<String, Object> eventMap = ImmutableMap.of("log_message", newArrayList());
        MessageEvent.fromLogMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_logMessage4() {
        final Map<String, Object> eventMap = ImmutableMap.of("log_message", newArrayList("m"));
        MessageEvent.fromLogMessage(eventMap);
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_logMessage5() {
        final ImmutableMap<String, ? extends Object> template = ImmutableMap.of("message", "msg", "level", "error",
                "timestamp", "ts");

        // from all combinations remove this which consist of all keys
        final Set<Set<String>> allKeysCombinations = newHashSet(
                Sets.powerSet(newHashSet("message", "level", "timestamp")));
        allKeysCombinations.remove(newHashSet("message", "level", "timestamp"));

        for (final Set<String> combination : allKeysCombinations) {
            try {
                final Map<String, Object> attributes = new HashMap<>();
                for (final String key : combination) {
                    attributes.put(key, template.get(key));
                }
                final Map<String, Object> eventMap = ImmutableMap.of("log_message", newArrayList(attributes));
                MessageEvent.fromLogMessage(eventMap);

                fail();
            } catch (final IllegalArgumentException e) {
                // that's what we expect to have
            }
        }
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_logMessage6() {
        final Map<String, Object> eventMap = ImmutableMap.of("log_message",
                newArrayList(ImmutableMap.of("message", "msg", "timestamp", "ts", "level", "unknown")));
        MessageEvent.fromLogMessage(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_logMessage7() {
        final Map<String, Object> eventMap = ImmutableMap.of("message",
                newArrayList(ImmutableMap.of("message", "msg", "timestamp", "ts", "level", "error")));
        MessageEvent.fromLogMessage(eventMap);
    }

    @Test
    public void eventOfMessageIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("message",
                newArrayList(ImmutableMap.of("message", "msg", "timestamp", "ts", "level", "error")));
        final MessageEvent event = MessageEvent.fromMessage(eventMap);

        assertThat(event.getMessage()).isEqualTo("msg");
        assertThat(event.getTimestamp()).isEqualTo("ts");
        assertThat(event.getLevel()).isEqualTo(LogLevel.ERROR);
    }

    @Test
    public void eventOfLogMessageIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("log_message",
                newArrayList(ImmutableMap.of("message", "msg", "timestamp", "ts", "level", "debug")));
        final MessageEvent event = MessageEvent.fromLogMessage(eventMap);

        assertThat(event.getMessage()).isEqualTo("msg");
        assertThat(event.getTimestamp()).isEqualTo("ts");
        assertThat(event.getLevel()).isEqualTo(LogLevel.DEBUG);
    }

    @Test
    public void equalsTests() {
        assertThat(new MessageEvent("msg", LogLevel.FAIL, "ts"))
                .isEqualTo(new MessageEvent("msg", LogLevel.FAIL, "ts"));
        assertThat(new MessageEvent("msg1", LogLevel.NONE, "ts1"))
                .isEqualTo(new MessageEvent("msg1", LogLevel.NONE, "ts1"));

        assertThat(new MessageEvent("msg", LogLevel.FAIL, "ts"))
                .isNotEqualTo(new MessageEvent("msg1", LogLevel.FAIL, "ts"));
        assertThat(new MessageEvent("msg1", LogLevel.FAIL, "ts"))
                .isNotEqualTo(new MessageEvent("msg", LogLevel.FAIL, "ts"));

        assertThat(new MessageEvent("msg", LogLevel.FAIL, "ts"))
                .isNotEqualTo(new MessageEvent("msg", LogLevel.INFO, "ts"));
        assertThat(new MessageEvent("msg", LogLevel.INFO, "ts"))
                .isNotEqualTo(new MessageEvent("msg", LogLevel.FAIL, "ts"));

        assertThat(new MessageEvent("msg", LogLevel.FAIL, "ts"))
                .isNotEqualTo(new MessageEvent("msg", LogLevel.FAIL, "ts1"));
        assertThat(new MessageEvent("msg", LogLevel.FAIL, "ts1"))
                .isNotEqualTo(new MessageEvent("msg", LogLevel.FAIL, "ts"));

        assertThat(new MessageEvent("msg", LogLevel.FAIL, "ts")).isNotEqualTo(new Object());
        assertThat(new MessageEvent("msg", LogLevel.FAIL, "ts")).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new MessageEvent("msg", LogLevel.DEBUG, "ts").hashCode())
                .isEqualTo(new MessageEvent("msg", LogLevel.DEBUG, "ts").hashCode());
    }
}
