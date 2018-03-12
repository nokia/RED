/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.agent.event;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class ResourceImportEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        ResourceImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("resource_import", new Object());
        ResourceImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("resource_import", newArrayList());
        ResourceImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("resource_import", newArrayList("_"));
        ResourceImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("resource_import", newArrayList("_", ImmutableMap.of()));
        ResourceImportEvent.from(eventMap);
    }

    @Test
    public void eventIsProperlyConstructed_whenThereIsNoImporter() {
        final Map<String, String> attributes = new HashMap<>();
        attributes.put("source", "/res.robot");
        attributes.put("importer", null);

        final Map<String, Object> eventMap = ImmutableMap.of("resource_import",
                newArrayList("_", attributes));
        final ResourceImportEvent event = ResourceImportEvent.from(eventMap);

        assertThat(event.isDynamicallyImported()).isTrue();
        assertThat(event.getPath()).isEqualTo(URI.create("file:///res.robot"));
        assertThat(event.getImporterPath()).isEmpty();
    }

    @Test
    public void eventIsProperlyConstructed_whenThereIsImporter() {
        final Map<String, Object> eventMap = ImmutableMap.of("resource_import",
                newArrayList("_", ImmutableMap.of("source", "/res.robot", "importer", "/suite.robot")));
        final ResourceImportEvent event = ResourceImportEvent.from(eventMap);

        assertThat(event.isDynamicallyImported()).isFalse();
        assertThat(event.getPath()).isEqualTo(URI.create("file:///res.robot"));
        assertThat(event.getImporterPath()).contains(URI.create("file:///suite.robot"));
    }

    @Test
    public void equalsTests() {
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), null))
                .isEqualTo(new ResourceImportEvent(URI.create("file:///res.robot"), null));
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot")))
                .isEqualTo(new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot")));

        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), null))
                .isNotEqualTo(new ResourceImportEvent(URI.create("file:///res1.robot"), null));
        assertThat(new ResourceImportEvent(URI.create("file:///res1.robot"), null))
                .isNotEqualTo(new ResourceImportEvent(URI.create("file:///res.robot"), null));
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot")))
                .isNotEqualTo(
                        new ResourceImportEvent(URI.create("file:///res1.robot"), URI.create("file:///suite.robot")));
        assertThat(new ResourceImportEvent(URI.create("file:///res1.robot"), URI.create("file:///suite.robot")))
                .isNotEqualTo(
                        new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot")));
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot")))
                .isNotEqualTo(
                        new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite1.robot")));
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite1.robot")))
                .isNotEqualTo(
                        new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot")));
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot")))
                .isNotEqualTo(new Object());
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot")))
                .isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), null).hashCode())
                .isEqualTo(new ResourceImportEvent(URI.create("file:///res.robot"), null).hashCode());
        assertThat(new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot"))
                .hashCode()).isEqualTo(
                        new ResourceImportEvent(URI.create("file:///res.robot"), URI.create("file:///suite.robot"))
                                .hashCode());
    }
}
