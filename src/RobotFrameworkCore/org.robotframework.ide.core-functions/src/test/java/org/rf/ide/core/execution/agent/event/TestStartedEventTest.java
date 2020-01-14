/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

public class TestStartedEventTest {

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> TestStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("test", ImmutableMap.of("longname", "s.test", "template", "kw")));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> TestStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test", new Object());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> TestStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test", newArrayList());
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> TestStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test", newArrayList("test"));
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> TestStartedEvent.from(eventMap));
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test", newArrayList("test", ImmutableMap.of()));
        assertThatIllegalArgumentException().isThrownBy(() -> TestStartedEvent.from(eventMap));
    }

    @Test
    public void eventIsProperlyConstructed_whenThereIsATemplate() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test",
                newArrayList("test", ImmutableMap.of("longname", "s.test", "template", "kw")));
        final TestStartedEvent event = TestStartedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("test");
        assertThat(event.getResolvedName()).isEqualTo("test");
        assertThat(event.getLongName()).isEqualTo("s.test");
        assertThat(event.getTemplate()).contains("kw");
    }

    @Test
    public void eventIsProperlyConstructed_whenThereIsAnEmptyTemplate() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test",
                newArrayList("test", ImmutableMap.of("longname", "s.test", "template", "")));
        final TestStartedEvent event = TestStartedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("test");
        assertThat(event.getResolvedName()).isEqualTo("test");
        assertThat(event.getLongName()).isEqualTo("s.test");
        assertThat(event.getTemplate()).isEmpty();
    }

    @Test
    public void eventIsProperlyConstructed_whenThereIsNoTemplate() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test",
                newArrayList("test", ImmutableMap.of("longname", "s.test")));
        final TestStartedEvent event = TestStartedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("test");
        assertThat(event.getResolvedName()).isEqualTo("test");
        assertThat(event.getLongName()).isEqualTo("s.test");
        assertThat(event.getTemplate()).isEmpty();
    }

    @Test
    public void eventIsProperlyConstructed_whenThereIsNoOriginalName() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test",
                newArrayList("test", ImmutableMap.of("longname", "s.test")));
        final TestStartedEvent event = TestStartedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("test");
        assertThat(event.getResolvedName()).isEqualTo("test");
        assertThat(event.getLongName()).isEqualTo("s.test");
        assertThat(event.getTemplate()).isEmpty();
    }

    @Test
    public void eventIsProperlyConstructed_whenThereIsOriginalName_1() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test",
                newArrayList("test", ImmutableMap.of("longname", "s.test", "originalname", "test")));
        final TestStartedEvent event = TestStartedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("test");
        assertThat(event.getResolvedName()).isEqualTo("test");
        assertThat(event.getLongName()).isEqualTo("s.test");
        assertThat(event.getTemplate()).isEmpty();
    }

    @Test
    public void eventIsProperlyConstructed_whenThereIsOriginalName_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_test",
                newArrayList("test 1", ImmutableMap.of("longname", "s.test 1", "originalname", "test ${var}")));
        final TestStartedEvent event = TestStartedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("test ${var}");
        assertThat(event.getResolvedName()).isEqualTo("test 1");
        assertThat(event.getLongName()).isEqualTo("s.test 1");
        assertThat(event.getTemplate()).isEmpty();
    }

    @Test
    public void equalsTests() {
        assertThat(new TestStartedEvent("test", "test", "s.test", "template"))
                .isEqualTo(new TestStartedEvent("test", "test", "s.test", "template"));
        assertThat(new TestStartedEvent("test", "test", "s.test", ""))
                .isEqualTo(new TestStartedEvent("test", "test", "s.test", ""));
        assertThat(new TestStartedEvent("test", "test", "s.test", null))
                .isEqualTo(new TestStartedEvent("test", "test", "s.test", null));
        assertThat(new TestStartedEvent("test", null, "s.test", null))
                .isEqualTo(new TestStartedEvent("test", null, "s.test", null));

        assertThat(new TestStartedEvent("test", "test", "s.test", "template"))
                .isNotEqualTo(new TestStartedEvent("test1", "test1", "s.test", "template"));
        assertThat(new TestStartedEvent("test1", "test1", "s.test", "template"))
                .isNotEqualTo(new TestStartedEvent("test", "test", "s.test", "template"));
        assertThat(new TestStartedEvent("test", "test", "s.test", "template"))
                .isNotEqualTo(new TestStartedEvent("test", "test1", "s.test", "template"));
        assertThat(new TestStartedEvent("test", "test1", "s.test", "template"))
                .isNotEqualTo(new TestStartedEvent("test", "test", "s.test", "template"));
        assertThat(new TestStartedEvent("test", "test", "s.test", "template"))
                .isNotEqualTo(new TestStartedEvent("test", "test", "s.test1", "template"));
        assertThat(new TestStartedEvent("test", "test", "s.test1", "template"))
                .isNotEqualTo(new TestStartedEvent("test", "test", "s.test", "template"));
        assertThat(new TestStartedEvent("test", "test", "s.test", "template"))
                .isNotEqualTo(new TestStartedEvent("test", "test", "s.test", "template1"));
        assertThat(new TestStartedEvent("test", "test", "s.test", "template1"))
                .isNotEqualTo(new TestStartedEvent("test", "test", "s.test", "template"));
        assertThat(new TestStartedEvent("test", "test", "s.test", "template")).isNotEqualTo(new Object());
        assertThat(new TestStartedEvent("test", "test", "s.test", "template")).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new TestStartedEvent("test", "test", "s.test", "template").hashCode())
                .isEqualTo(new TestStartedEvent("test", "test", "s.test", "template").hashCode());
        assertThat(new TestStartedEvent("test", "test", "s.test", null).hashCode())
                .isEqualTo(new TestStartedEvent("test", "test", "s.test", null).hashCode());
        assertThat(new TestStartedEvent("test", null, "s.test", null).hashCode())
                .isEqualTo(new TestStartedEvent("test", null, "s.test", null).hashCode());
    }
}
