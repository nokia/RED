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

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_6() {
        final Map<String, Object> eventMap = ImmutableMap.of("library_import",
                newArrayList("_", ImmutableMap.of("originalname", "lib")));
        LibraryImportEvent.from(eventMap);
    }

    @Test(expected = RuntimeException.class)
    public void exceptionIsThrownForWronglyConstructedJsonDictionary_7() {
        final Map<String, Object> eventMap = ImmutableMap.of("library_import",
                newArrayList("_", ImmutableMap.of("args", newArrayList("1"))));
        LibraryImportEvent.from(eventMap);
    }

    @Test
    public void eventIsProperlyConstructed_1() {
        final Map<String, Object> eventMap = ImmutableMap.of("library_import",
                newArrayList("_", ImmutableMap.of("source", "/lib.py", "importer", "/suite.robot", "originalname",
                        "lib", "args", newArrayList("1", "2"))));
        final LibraryImportEvent event = LibraryImportEvent.from(eventMap);

        assertThat(event.getArguments()).containsExactly("1", "2");
        assertThat(event.getName()).isEqualTo("lib");
        assertThat(event.getSource()).contains(URI.create("file:///lib.py"));
        assertThat(event.getImporter()).contains(URI.create("file:///suite.robot"));
    }

    @Test
    public void eventIsProperlyConstructed_2() {
        final Map<String, Object> attributes = new HashMap<>();
        attributes.put("source", null);
        attributes.put("importer", null);
        attributes.put("originalname", "lib");
        attributes.put("args", newArrayList("1", "2"));
        final Map<String, Object> eventMap = ImmutableMap.of("library_import", newArrayList("_", attributes));
        final LibraryImportEvent event = LibraryImportEvent.from(eventMap);

        assertThat(event.getArguments()).containsExactly("1", "2");
        assertThat(event.getName()).isEqualTo("lib");
        assertThat(event.getSource()).isEmpty();
        assertThat(event.getImporter()).isEmpty();
    }

    @Test
    public void equalsTests() {
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("1")))
                        .isEqualTo(new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                                URI.create("file:///lib.py"), newArrayList("1")));
        assertThat(new LibraryImportEvent("lib", null, null, newArrayList("1")))
                .isEqualTo(new LibraryImportEvent("lib", null, null, newArrayList("1")));


        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("1")))
                        .isNotEqualTo(new LibraryImportEvent("lib1", URI.create("file:///res.robot"),
                                URI.create("file:///lib.py"), newArrayList("1")));
        assertThat(new LibraryImportEvent("lib1", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("1")))
                        .isNotEqualTo(new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                                URI.create("file:///lib.py"), newArrayList("1")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("1")))
                        .isNotEqualTo(new LibraryImportEvent("lib", URI.create("file:///res1.robot"),
                                URI.create("file:///lib.py"), newArrayList("1")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res1.robot"), URI.create("file:///lib.py"),
                newArrayList("1")))
                        .isNotEqualTo(new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                                URI.create("file:///lib.py"), newArrayList("1")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("1")))
                        .isNotEqualTo(new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                                URI.create("file:///lib1.py"), newArrayList("1")));
        assertThat(new LibraryImportEvent(
                "lib", URI.create("file:///res.robot"), URI.create("file:///lib1.py"), newArrayList("1")))
                        .isNotEqualTo(new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                                URI.create("file:///lib.py"), newArrayList("1")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("1")))
                        .isNotEqualTo(new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                                URI.create("file:///lib.py"), newArrayList("2")));
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("2")))
                        .isNotEqualTo(new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                                URI.create("file:///lib.py"), newArrayList("1")));

        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("1"))).isNotEqualTo(new Object());
        assertThat(new LibraryImportEvent("lib", URI.create("file:///res.robot"), URI.create("file:///lib.py"),
                newArrayList("1"))).isNotEqualTo(null);
    }

    @Test
    public void hashCodeTests() {
        final LibraryImportEvent event1 = new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                URI.create("file:///lib.py"), newArrayList("1"));
        final LibraryImportEvent event2 = new LibraryImportEvent("lib", URI.create("file:///res.robot"),
                URI.create("file:///lib.py"), newArrayList("1"));
        final LibraryImportEvent event3 = new LibraryImportEvent("lib", null, URI.create("file:///lib.py"),
                newArrayList("1"));
        final LibraryImportEvent event4 = new LibraryImportEvent("lib", null, URI.create("file:///lib.py"),
                newArrayList("1"));
        final LibraryImportEvent event5 = new LibraryImportEvent("lib", URI.create("file:///res.robot"), null,
                newArrayList("1"));
        final LibraryImportEvent event6 = new LibraryImportEvent("lib", URI.create("file:///res.robot"), null,
                newArrayList("1"));
        final LibraryImportEvent event7 = new LibraryImportEvent("lib", null, null, newArrayList("1"));
        final LibraryImportEvent event8 = new LibraryImportEvent("lib", null, null, newArrayList("1"));

        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event3.hashCode()).isEqualTo(event4.hashCode());
        assertThat(event5.hashCode()).isEqualTo(event6.hashCode());
        assertThat(event7.hashCode()).isEqualTo(event8.hashCode());
    }
}
