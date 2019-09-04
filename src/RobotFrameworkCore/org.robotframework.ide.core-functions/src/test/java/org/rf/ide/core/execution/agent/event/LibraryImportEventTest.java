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

public class LibraryImportEventTest {

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_1() {
        final Map<String, Object> eventMap = ImmutableMap.of();
        LibraryImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_2() {
        final Map<String, Object> eventMap = ImmutableMap.of("library_import", new Object());
        LibraryImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_3() {
        final Map<String, Object> eventMap = ImmutableMap.of("library_import", newArrayList());
        LibraryImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_4() {
        final Map<String, Object> eventMap = ImmutableMap.of("library_import", newArrayList("_", "foo"));
        LibraryImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_5() {
        final Map<String, Object> eventMap = ImmutableMap.of("library_import", newArrayList("_", ImmutableMap.of()));
        LibraryImportEvent.from(eventMap);
    }

    @Test
    public void eventIsProperlyConstructed() {
        final Map<String, Object> eventMap = ImmutableMap.of("library_import",
                newArrayList("_", ImmutableMap.of("source", "/lib.py", "importer", "/suite.robot", "originalname",
                        "lib")));
        final LibraryImportEvent event = LibraryImportEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("lib");
        assertThat(event.getSource()).contains(URI.create("file:///lib.py"));
        assertThat(event.getImporter()).contains(URI.create("file:///suite.robot"));
    }

    @Test
    public void eventIsProperlyConstructed_evenWhenThereAreArgumentsOfDifferentTypesProvided() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("source", null);
        attributes.put("importer", null);
        attributes.put("originalname", "lib");
        attributes.put("args", newArrayList("1", Integer.valueOf(2), new Object()));
        final Map<String, Object> eventMap = ImmutableMap.of("library_import", newArrayList("_", attributes));
        final LibraryImportEvent event = LibraryImportEvent.from(eventMap);

        assertThat(event.getName()).isEqualTo("lib");
        assertThat(event.getSource()).isEmpty();
        assertThat(event.getImporter()).isEmpty();
    }

    @Test
    public void equalsTests() {
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")))
                        .isEqualTo(new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                        URI.create("file:///lib.py")));
        assertThat(new LibraryImportEvent("lib", null, null)).isEqualTo(new LibraryImportEvent("lib", null, null));


        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")))
                .isNotEqualTo(
                        new LibraryImportEvent("lib1", URI.create("file:///res.robot"), URI.create("file:///lib.py")));
        assertThat(new LibraryImportEvent("lib1", URI.create("file:///res.robot"), URI.create("file:///lib.py")))
                .isNotEqualTo(
                        new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")))
                .isNotEqualTo(
                        new LibraryImportEvent("lib", URI.create("file:///res1.robot"), URI.create("file:///lib.py")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res1.robot"), URI.create("file:///lib.py")))
                .isNotEqualTo(
                        new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")))
                .isNotEqualTo(
                        new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib1.py")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib1.py")))
                .isNotEqualTo(
                        new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")));

        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")))
                .isNotEqualTo(new Object());
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py")))
                .isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        final LibraryImportEvent event1 = new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                URI.create("file:///lib.py"));
        final LibraryImportEvent event2 = new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                URI.create("file:///lib.py"));
        final LibraryImportEvent event3 = new LibraryImportEvent("lib", null, URI.create("file:///lib.py"));
        final LibraryImportEvent event4 = new LibraryImportEvent("lib", null, URI.create("file:///lib.py"));
        final LibraryImportEvent event5 = new LibraryImportEvent("lib", URI.create("file:///res.robot"), null);
        final LibraryImportEvent event6 = new LibraryImportEvent("lib", URI.create("file:///res.robot"), null);
        final LibraryImportEvent event7 = new LibraryImportEvent("lib", null, null);
        final LibraryImportEvent event8 = new LibraryImportEvent("lib", null, null);

        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event3.hashCode()).isEqualTo(event4.hashCode());
        assertThat(event5.hashCode()).isEqualTo(event6.hashCode());
        assertThat(event7.hashCode()).isEqualTo(event8.hashCode());
    }
}
