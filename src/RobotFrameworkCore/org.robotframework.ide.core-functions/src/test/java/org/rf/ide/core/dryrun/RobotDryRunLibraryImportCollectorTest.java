/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;
import java.util.Collections;
import java.util.Objects;

import org.assertj.core.api.Condition;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class RobotDryRunLibraryImportCollectorTest {

    @Test
    public void standardLibraryImportIsNotCollected() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of("String"));

        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("String",
                new URI("file:///suite.robot"), new URI("file:///String.py"), emptyList()));

        assertThat(libImportCollector.getImportedLibraries()).isEmpty();
    }

    @Test
    public void standardLibraryImportsAreIgnoredWhenUserLibraryImportExists() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of("String", "Xml"));

        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("String",
                new URI("file:///suite.robot"), new URI("file:///String.py"), emptyList()));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib", new URI("file:///suite.robot"),
                new URI("file:///source.py"), emptyList()));
        libImportCollector.collectFromLibraryImportEvent(
                new LibraryImportEvent("Xml", new URI("file:///suite.robot"), new URI("file:///Xml.py"), emptyList()));

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertThat(libImportCollector.getImportedLibraries().get(0))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createKnown("lib", new URI("file:///source.py"),
                        newHashSet(new URI("file:///suite.robot")), emptyList())));
    }

    @Test
    public void importErrorsAreCollectedFromMessages() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                Collections.emptySet());

        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib1"));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib2"));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib3"));

        assertThat(libImportCollector.getImportedLibraries()).hasSize(3);
        assertThat(libImportCollector.getImportedLibraries().get(0))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createUnknown("lib1", "error in lib1")));
        assertThat(libImportCollector.getImportedLibraries().get(1))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createUnknown("lib2", "error in lib2")));
        assertThat(libImportCollector.getImportedLibraries().get(2))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createUnknown("lib3", "error in lib3")));
    }

    @Test
    public void importErrorEventMappingExceptionIsHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                Collections.emptySet());

        final MessageEvent event = new MessageEvent("{\"import_error\":\"incorrect\"}", LogLevel.NONE, null);

        assertThatExceptionOfType(JsonMessageMapper.JsonMessageMapperException.class)
                .isThrownBy(() -> libImportCollector.collectFromMessageEvent(event))
                .withMessage("Problem with mapping message for key 'import_error'")
                .withCauseInstanceOf(JsonMappingException.class);
    }

    @Test
    public void libraryImportsAndMultipleMessagesAreCollected() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                Collections.emptySet());

        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib1", new URI("file:///suite1.robot"),
                new URI("file:///lib1.py"), emptyList()));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib2"));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib3"));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib4", new URI("file:///suite2.robot"),
                new URI("file:///lib4.py"), emptyList()));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib5"));
        libImportCollector.collectFromLibraryImportEvent(
                new LibraryImportEvent("lib6", new URI("file:///suite3.robot"), null, emptyList()));
        libImportCollector.collectFromLibraryImportEvent(
                new LibraryImportEvent("lib7", null, new URI("file:///lib7.py"), emptyList()));

        assertThat(libImportCollector.getImportedLibraries()).hasSize(6);
        assertThat(libImportCollector.getImportedLibraries().get(0))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createKnown("lib1", new URI("file:///lib1.py"),
                        newHashSet(new URI("file:///suite1.robot")), emptyList())));
        assertThat(libImportCollector.getImportedLibraries().get(1))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createUnknown("lib2", "error in lib2")));
        assertThat(libImportCollector.getImportedLibraries().get(2))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createUnknown("lib3", "error in lib3")));
        assertThat(libImportCollector.getImportedLibraries().get(3))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createKnown("lib4", new URI("file:///lib4.py"),
                        newHashSet(new URI("file:///suite2.robot")), emptyList())));
        assertThat(libImportCollector.getImportedLibraries().get(4))
                .has(sameFieldsAs(RobotDryRunLibraryImport.createUnknown("lib5", "error in lib5")));
        assertThat(libImportCollector.getImportedLibraries().get(5)).has(sameFieldsAs(RobotDryRunLibraryImport
                .createKnown("lib6", null, newHashSet(new URI("file:///suite3.robot")), emptyList())));
    }

    @Test
    public void additionalInfoIsFormatted() throws Exception {
        assertThat(RobotDryRunLibraryImportCollector.formatAdditionalInfo("")).isEmpty();
        assertThat(RobotDryRunLibraryImportCollector.formatAdditionalInfo("abc")).isEqualTo("abc");
        assertThat(RobotDryRunLibraryImportCollector.formatAdditionalInfo("a\\nb\\nc")).isEqualTo("a\nb\nc");
        assertThat(RobotDryRunLibraryImportCollector.formatAdditionalInfo("a\\'b\\'c")).isEqualTo("a'b'c");
        assertThat(RobotDryRunLibraryImportCollector.formatAdditionalInfo("a\\nb\\'c\\'\\nd")).isEqualTo("a\nb'c'\nd");
        assertThat(RobotDryRunLibraryImportCollector
                .formatAdditionalInfo("(<class 'robot.errors.DataError'>, DataError(u'message'), traceback)"))
                        .isEqualTo("((u'message'), traceback)");
    }

    private static MessageEvent createImportErrorMessageEvent(final String name) throws Exception {
        final Object libErrorAttributes = ImmutableMap.builder()
                .put("name", name)
                .put("error", "error in " + name)
                .build();
        final String message = new ObjectMapper()
                .writeValueAsString((ImmutableMap.of("import_error", libErrorAttributes)));
        return new MessageEvent(message, LogLevel.NONE, null);
    }

    private static Condition<? super RobotDryRunLibraryImport> sameFieldsAs(final RobotDryRunLibraryImport library) {
        return new Condition<RobotDryRunLibraryImport>() {

            @Override
            public boolean matches(final RobotDryRunLibraryImport toMatch) {
                return Objects.equals(library.getName(), toMatch.getName())
                        && Objects.equals(library.getSourcePath(), toMatch.getSourcePath())
                        && Objects.equals(library.getType(), toMatch.getType())
                        && Objects.equals(library.getImportersPaths(), toMatch.getImportersPaths())
                        && Objects.equals(library.getArgs(), toMatch.getArgs())
                        && Objects.equals(library.getStatus(), toMatch.getStatus())
                        && Objects.equals(library.getAdditionalInfo(), toMatch.getAdditionalInfo());
            }
        };
    }
}
