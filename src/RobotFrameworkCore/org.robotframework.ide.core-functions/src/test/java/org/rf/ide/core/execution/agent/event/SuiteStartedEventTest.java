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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

public class SuiteStartedEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        SuiteStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something",
                newArrayList("suite", ImmutableMap.of("status", "PASS", "elapsedtime", 100, "message", "error")));
        SuiteStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_suite", new Object());
        SuiteStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_suite", newArrayList());
        SuiteStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_suite", newArrayList("suite"));
        SuiteStartedEvent.from(eventMap);
    }

    @Test
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final ImmutableMap<String, ? extends Object> template = ImmutableMap.of("source", "/suite", "is_dir", true,
                "totaltests", 42, "suites", newArrayList("s1", "s2"), "tests", newArrayList("t1", "t2"));

        // from all combinations remove this which consist of all keys
        final Set<Set<String>> allKeysCombinations = newHashSet(
                Sets.powerSet(newHashSet("source", "is_dir", "totaltests", "suites", "tests")));
        allKeysCombinations.remove(newHashSet("source", "is_dir", "totaltests", "suites", "tests"));
        allKeysCombinations.removeIf(s -> !s.contains("source"));

        for (final Set<String> combination : allKeysCombinations) {
            final Map<String, Object> attributes = new HashMap<>();
            for (final String key : combination) {
                attributes.put(key, template.get(key));
            }
            final Map<String, Object> eventMap = ImmutableMap.of("start_suite", newArrayList("suite", attributes));

            assertThatIllegalArgumentException().isThrownBy(() -> SuiteStartedEvent.from(eventMap))
                    .withMessage(
                            "Suite started event should have directory/file flag, children suites and tests as well as number of total tests")
                    .withNoCause();
        }
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_7() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_suite",
                newArrayList("suite", ImmutableMap.of("source", "/suite", "is_dir", true, "totaltests", "100", "suites",
                        newArrayList("s1", "s2"), "tests", newArrayList("t1", "t2"))));
        SuiteStartedEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_8() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_suite",
                newArrayList("suite", ImmutableMap.of("source", "/suite", "is_dir", "true", "totaltests", 42, "suites",
                        newArrayList("s1", "s2"), "tests", newArrayList("t1", "t2"))));
        SuiteStartedEvent.from(eventMap);
    }

    @Test
    public void eventIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("start_suite",
                newArrayList("suite", ImmutableMap.of("source", "/suite", "is_dir", true, "totaltests", 42, "suites",
                        newArrayList("s1", "s2"), "tests", newArrayList("t1", "t2"))));
        final SuiteStartedEvent event = SuiteStartedEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("suite");
        assertThat(event.isDirectory()).isTrue();
        assertThat(event.getPath()).isEqualTo(URI.create("file:///suite"));
        assertThat(event.getNumberOfTests()).isEqualTo(42);
        assertThat(event.getChildrenSuites()).containsExactly("s1", "s2");
        assertThat(event.getChildrenTests()).containsExactly("t1", "t2");
    }

    @Test
    public void equalsTests() {
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));

        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite1", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite1", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite1"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite1"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), false, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), false, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 20,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 20, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1")))
                        .isNotEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")));
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2"))).isNotEqualTo(new Object());
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2"))).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10, newArrayList("s1", "s2"),
                newArrayList("t1", "t2")).hashCode())
                        .isEqualTo(new SuiteStartedEvent("suite", URI.create("file:///suite"), true, 10,
                                newArrayList("s1", "s2"), newArrayList("t1", "t2")).hashCode());
    }
}
