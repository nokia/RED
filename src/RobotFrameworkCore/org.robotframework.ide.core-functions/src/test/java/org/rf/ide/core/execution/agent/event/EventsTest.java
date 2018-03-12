/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class EventsTest {

    @Test
    public void uriParsingTests() {
        assertThat(Events.toFileUri(null)).isNull();
        assertThat(Events.toFileUri("")).isNull();
        assertThat(Events.toFileUri("a/b/c")).isEqualTo(URI.create("file:///a/b/c"));
        assertThat(Events.toFileUri("/a/b/c")).isEqualTo(URI.create("file:///a/b/c"));
        assertThat(Events.toFileUri("a\\b\\c")).isEqualTo(URI.create("file:///a/b/c"));
        assertThat(Events.toFileUri("[abc]")).isNull();
    }

    @Test
    public void properListOfStringIsReturned_whenListOfObjectsIsProvided() {
        final List<?> list = newArrayList("a", "b", "c");
        assertThat(Events.ensureListOfStrings(list)).containsExactly("a", "b", "c");
    }

    @Test(expected = ClassCastException.class)
    public void classCastExceptionIsThrown_whenListOfObjectsContainsSomethingDifferentThanString() {
        final List<?> list = newArrayList("a", new Object(), "c");
        Events.ensureListOfStrings(list);
    }

    @Test
    public void properListOfOrderedStringToObjectMapIsReturned_whenListOfObjectsIsProvided() {
        final List<?> list = newArrayList(ImmutableMap.of("x", 1, "y", 2),
                ImmutableMap.of("1", Boolean.TRUE, "2", "abc"));
        final List<Map<String, Object>> result = Events.ensureListOfOrderedMapOfStringsToObjects(list);

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isInstanceOf(LinkedHashMap.class);
        assertThat(result.get(0)).containsEntry("x", 1).containsEntry("y", 2);
        assertThat(result.get(1)).isInstanceOf(LinkedHashMap.class);
        assertThat(result.get(1)).containsEntry("1", Boolean.TRUE).containsEntry("2", "abc");
    }

    @Test(expected = ClassCastException.class)
    public void classCastExceptionIsThrown_whenListContainsSomethingDifferentThanMap() {
        final List<?> list = newArrayList(ImmutableMap.of("x", 1, "y", 2), "foo");
        Events.ensureListOfOrderedMapOfStringsToObjects(list);
    }

    @Test(expected = ClassCastException.class)
    public void classCastExceptionIsThrown_whenListContainsMapWithNoStringKeys() {
        final List<?> list = newArrayList(ImmutableMap.of(1, 2, 2, 3));
        Events.ensureListOfOrderedMapOfStringsToObjects(list);
    }

    @Test
    public void orderedMapFromStringsToObjectsIsReturned_whenAnyMapIsProvided() {
        final Map<?, ?> map = ImmutableMap.of("x", "a", "y", 1);
        final Map<String, Object> result = Events.ensureOrderedMapOfStringsToObjects(map);

        assertThat(result).hasSize(2).isInstanceOf(LinkedHashMap.class);
        assertThat(result).containsKeys("x", "y").containsValues("a", 1);
    }
    
    @Test(expected=ClassCastException.class)
    public void classCastExceptionIsThrown_whenMapHasKeysOtherThenStrings() {
        final Map<?, ?> map = ImmutableMap.of("x", "a", new Object(), 1);
        Events.ensureOrderedMapOfStringsToObjects(map);
    }

    @Test
    public void orderedMapFromStringsToStringsIsReturned_whenAnyMapIsProvided() {
        final Map<?, ?> map = ImmutableMap.of("x", "a", "y", "b");
        final Map<String, String> result = Events.ensureOrderedMapOfStringsToStrings(map);

        assertThat(result).hasSize(2).isInstanceOf(LinkedHashMap.class);
        assertThat(result).containsKeys("x", "y").containsValues("a", "b");
    }

    @Test(expected = ClassCastException.class)
    public void classCastExceptionIsThrown_whenMapHasKeyOtherThenStrings() {
        final Map<?, ?> map = ImmutableMap.of("x", "a", new Object(), "b");
        Events.ensureOrderedMapOfStringsToStrings(map);
    }

    @Test(expected = ClassCastException.class)
    public void classCastExceptionIsThrown_whenMapHasValueOtherThenStrings() {
        final Map<?, ?> map = ImmutableMap.of("x", "a", "y", new Object());
        Events.ensureOrderedMapOfStringsToStrings(map);
    }

}
