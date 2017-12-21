/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;
import java.util.Collections;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
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
                new URI("file:///suite.robot"), new URI("file:///String.py"), Collections.emptyList()));

        assertThat(libImportCollector.getImportedLibraries()).isEmpty();
    }

    @Test
    public void standardLibraryImportsAreIgnoredWhenUserLibraryImportExists() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of("String", "Xml"));

        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("String",
                new URI("file:///suite.robot"), new URI("file:///String.py"), Collections.emptyList()));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib", new URI("file:///suite.robot"),
                new URI("file:///source.py"), Collections.emptyList()));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("Xml", new URI("file:///suite.robot"),
                new URI("file:///Xml.py"), Collections.emptyList()));

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", new URI("file:///source.py"),
                new URI("file:///suite.robot"), Collections.emptyList());

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.PYTHON);
    }

    @Test
    public void importErrorsAreCollectedFromMessages() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                Collections.emptySet());

        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib1"));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib2"));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib3"));

        final RobotDryRunLibraryImport lib1 = new RobotDryRunLibraryImport("lib1", null, null, Collections.emptyList());
        lib1.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib1.setAdditionalInfo("error in lib1");
        final RobotDryRunLibraryImport lib2 = new RobotDryRunLibraryImport("lib2", null, null, Collections.emptyList());
        lib2.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib2.setAdditionalInfo("error in lib2");
        final RobotDryRunLibraryImport lib3 = new RobotDryRunLibraryImport("lib3", null, null, Collections.emptyList());
        lib3.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib3.setAdditionalInfo("error in lib3");

        assertThat(libImportCollector.getImportedLibraries()).hasSize(3);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib1, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(1), lib2, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(2), lib3, DryRunLibraryType.UNKNOWN);
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
                new URI("file:///lib1.py"), Collections.emptyList()));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib2"));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib3"));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib4", new URI("file:///suite2.robot"),
                new URI("file:///lib4.py"), Collections.emptyList()));
        libImportCollector.collectFromMessageEvent(createImportErrorMessageEvent("lib5"));
        libImportCollector.collectFromLibraryImportEvent(
                new LibraryImportEvent("lib6", new URI("file:///suite3.robot"), null, Collections.emptyList()));
        libImportCollector.collectFromLibraryImportEvent(
                new LibraryImportEvent("lib7", null, new URI("file:///lib7.py"), Collections.emptyList()));

        final RobotDryRunLibraryImport lib1 = new RobotDryRunLibraryImport("lib1", new URI("file:///lib1.py"),
                new URI("file:///suite1.robot"), Collections.emptyList());

        final RobotDryRunLibraryImport lib2 = new RobotDryRunLibraryImport("lib2");
        lib2.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib2.setAdditionalInfo("error in lib2");

        final RobotDryRunLibraryImport lib3 = new RobotDryRunLibraryImport("lib3");
        lib3.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib3.setAdditionalInfo("error in lib3");

        final RobotDryRunLibraryImport lib4 = new RobotDryRunLibraryImport("lib4", new URI("file:///lib4.py"),
                new URI("file:///suite2.robot"), Collections.emptyList());

        final RobotDryRunLibraryImport lib5 = new RobotDryRunLibraryImport("lib5");
        lib5.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib5.setAdditionalInfo("error in lib5");

        final RobotDryRunLibraryImport lib6 = new RobotDryRunLibraryImport("lib6", null,
                new URI("file:///suite3.robot"), Collections.emptyList());

        assertThat(libImportCollector.getImportedLibraries()).hasSize(6);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib1, DryRunLibraryType.PYTHON);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(1), lib2, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(2), lib3, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(3), lib4, DryRunLibraryType.PYTHON);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(4), lib5, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(5), lib6, DryRunLibraryType.UNKNOWN);
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

    private static void assertCollectedLibraryImport(final RobotDryRunLibraryImport actual,
            final RobotDryRunLibraryImport expected, final DryRunLibraryType expectedType) {
        assertThat(actual.getType()).isEqualTo(expectedType);
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getArgs()).isEqualTo(expected.getArgs());
        assertThat(actual.getSourcePath()).isEqualTo(expected.getSourcePath());
        assertThat(actual.getImportersPaths()).isEqualTo(expected.getImportersPaths());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getAdditionalInfo()).isEqualTo(expected.getAdditionalInfo());
    }
}
