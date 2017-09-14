/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Arrays;

import org.junit.Test;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;

import com.google.common.collect.ImmutableSet;

public class RobotDryRunLibraryImportCollectorTest {

    @Test
    public void standardLibraryImportIsNotCollected() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of("String"));

        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("String",
                new URI("file:///suite.robot"), new URI("file:///String.py"), Arrays.asList()));

        assertThat(libImportCollector.getImportedLibraries()).isEmpty();
    }

    @Test
    public void standardLibraryImportsAreIgnoredWhenUserLibraryImportExists() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of("String", "Xml"));

        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("String",
                new URI("file:///suite.robot"), new URI("file:///String.py"), Arrays.asList()));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib", new URI("file:///suite.robot"),
                new URI("file:///source.py"), Arrays.asList()));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("Xml", new URI("file:///suite.robot"),
                new URI("file:///Xml.py"), Arrays.asList()));

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", new URI("file:///source.py"),
                new URI("file:///suite.robot"), Arrays.asList());

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.PYTHON);
    }

    @Test
    public void failMessageIsRetrieved() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());

        libImportCollector.collectFromFailMessageEvent(createFailMessageEvent("lib"));

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", null, null, Arrays.asList());
        lib.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib.setAdditionalInfo("(Importing test library lib failed)");

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.UNKNOWN);
    }

    @Test
    public void errorMessageIsRetrieved() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());

        libImportCollector.collectFromErrorMessageEvent(createErrorMessageEvent("lib", "/suite.robot"));

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", null, new URI("file:///suite.robot"),
                Arrays.asList());
        lib.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib.setAdditionalInfo("Error in file '/suite.robot': Test library 'lib' does not exist.");

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.UNKNOWN);
    }

    @Test
    public void multipleImportersAreCombined() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());

        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib", new URI("file:///suite1.robot"),
                new URI("file:///lib.py"), Arrays.asList()));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib", new URI("file:///suite2.robot"),
                new URI("file:///lib.py"), Arrays.asList()));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib", new URI("file:///suite3.robot"),
                new URI("file:///lib.py"), Arrays.asList()));

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", new URI("file:///lib.py"),
                new URI("file:///suite1.robot"), Arrays.asList());
        lib.addImporterPath(new URI("file:///suite2.robot"));
        lib.addImporterPath(new URI("file:///suite3.robot"));

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.PYTHON);
    }

    @Test
    public void libraryImportsAndMultipleMessagesAreCollected() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());

        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib1", new URI("file:///suite1.robot"),
                new URI("file:///lib1.py"), Arrays.asList()));
        libImportCollector.collectFromErrorMessageEvent(createErrorMessageEvent("lib2", "/suite2.robot"));
        libImportCollector.collectFromFailMessageEvent(createFailMessageEvent("lib3"));
        libImportCollector.collectFromFailMessageEvent(createFailMessageEvent("lib4"));
        libImportCollector.collectFromErrorMessageEvent(createErrorMessageEvent("lib5", "/suite5.robot"));
        libImportCollector.collectFromErrorMessageEvent(createErrorMessageEvent("lib5", "/other.robot"));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib6", new URI("file:///suite6.robot"),
                new URI("file:///lib6.py"), Arrays.asList()));
        libImportCollector.collectFromLibraryImportEvent(new LibraryImportEvent("lib6", new URI("file:///other.robot"),
                new URI("file:///lib6.py"), Arrays.asList()));

        final RobotDryRunLibraryImport lib1 = new RobotDryRunLibraryImport("lib1", new URI("file:///lib1.py"),
                new URI("file:///suite1.robot"), Arrays.asList());

        final RobotDryRunLibraryImport lib2 = new RobotDryRunLibraryImport("lib2", null,
                new URI("file:///suite2.robot"), Arrays.asList());
        lib2.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib2.setAdditionalInfo("Error in file '/suite2.robot': Test library 'lib2' does not exist.");

        final RobotDryRunLibraryImport lib3 = new RobotDryRunLibraryImport("lib3", null, null, Arrays.asList());
        lib3.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib3.setAdditionalInfo("(Importing test library lib3 failed)");

        final RobotDryRunLibraryImport lib4 = new RobotDryRunLibraryImport("lib4", null, null, Arrays.asList());
        lib4.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib4.setAdditionalInfo("(Importing test library lib4 failed)");

        final RobotDryRunLibraryImport lib5 = new RobotDryRunLibraryImport("lib5", null,
                new URI("file:///suite5.robot"), Arrays.asList());
        lib5.addImporterPath(new URI("file:///other.robot"));
        lib5.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib5.setAdditionalInfo("Error in file '/other.robot': Test library 'lib5' does not exist.");

        final RobotDryRunLibraryImport lib6 = new RobotDryRunLibraryImport("lib6", new URI("file:///lib6.py"),
                new URI("file:///suite6.robot"), Arrays.asList());
        lib6.addImporterPath(new URI("file:///other.robot"));

        assertThat(libImportCollector.getImportedLibraries()).hasSize(6);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib1, DryRunLibraryType.PYTHON);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(1), lib2, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(2), lib3, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(3), lib4, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(4), lib5, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(5), lib6, DryRunLibraryType.PYTHON);
    }

    private static MessageEvent createFailMessageEvent(final String libName) {
        final String message = "{LIB_ERROR: " + libName + ", value: VALUE_START((Importing test library " + libName
                + " failed))VALUE_END, lib_file_import:None}";
        return new MessageEvent(message, LogLevel.FAIL, null);
    }

    private static MessageEvent createErrorMessageEvent(final String libName, final String fileName) {
        final String message = "Error in file '" + fileName + "': Test library '" + libName + "' does not exist.";
        return new MessageEvent(message, LogLevel.ERROR, null);
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
