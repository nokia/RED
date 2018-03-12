/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class OutputFileEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        OutputFileEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("something", newArrayList("/file.txt"));
        OutputFileEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("output_file", newArrayList());
        OutputFileEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("output_file", newArrayList(new Object()));
        OutputFileEvent.from(eventMap);
    }

    @Test
    public void eventWithNoPathIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("output_file", newArrayList((Object) null));
        final OutputFileEvent event = OutputFileEvent.from(eventMap);

        assertThat(event.getPath()).isEmpty();
    }

    @Test
    public void eventWithPathIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("output_file", newArrayList("/file.txt"));
        final OutputFileEvent event = OutputFileEvent.from(eventMap);

        assertThat(event.getPath()).contains(URI.create("file:///file.txt"));
    }

    @Test
    public void equalsTests() {
        assertThat(new OutputFileEvent(URI.create("file:///file.txt")))
                .isEqualTo(new OutputFileEvent(URI.create("file:///file.txt")));
        assertThat(new OutputFileEvent(null)).isEqualTo(new OutputFileEvent(null));

        assertThat(new OutputFileEvent(URI.create("file:///file.txt"))).isNotEqualTo(new OutputFileEvent(null));
        assertThat(new OutputFileEvent(null)).isNotEqualTo(new OutputFileEvent(URI.create("file:///file.txt")));
        assertThat(new OutputFileEvent(URI.create("file:///file.txt")))
                .isNotEqualTo(new OutputFileEvent(URI.create("file:///file1.txt")));
        assertThat(new OutputFileEvent(URI.create("file:///file1.txt")))
                .isNotEqualTo(new OutputFileEvent(URI.create("file:///file.txt")));
        assertThat(new OutputFileEvent(URI.create("file:///file.txt"))).isNotEqualTo(new Object());
        assertThat(new OutputFileEvent(URI.create("file:///file.txt"))).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new OutputFileEvent(null).hashCode()).isEqualTo(new OutputFileEvent(null).hashCode());
        assertThat(new OutputFileEvent(URI.create("file:///file.txt")).hashCode())
                .isEqualTo(new OutputFileEvent(URI.create("file:///file.txt")).hashCode());
    }
}
