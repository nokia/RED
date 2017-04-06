/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
import org.rf.ide.core.execution.MessageLevel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class RobotDryRunEventListenerTest {

    @Mock
    private Consumer<String> startSuiteHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void suiteStartingEventIsHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = mock(RobotDryRunLibraryImportCollector.class);
        final RobotDryRunKeywordSourceCollector kwSourceCollector = mock(RobotDryRunKeywordSourceCollector.class);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleSuiteStarted("abc", new File("path"));

        verify(startSuiteHandler).accept("abc");
        verifyNoMoreInteractions(startSuiteHandler);
        verifyZeroInteractions(libImportCollector);
        verifyZeroInteractions(kwSourceCollector);
    }

    @Test
    public void standardLibraryImportIsHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of("String"));
        final RobotDryRunKeywordSourceCollector kwSourceCollector = mock(RobotDryRunKeywordSourceCollector.class);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleLibraryImport("String", "suite.robot", "String.py", Arrays.asList());

        assertThat(libImportCollector.getImportedLibraries()).isEmpty();
        verifyZeroInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void userLibraryImportEventIsHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());
        final RobotDryRunKeywordSourceCollector kwSourceCollector = mock(RobotDryRunKeywordSourceCollector.class);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleLibraryImport("lib", "suite.robot", "source.py", Arrays.asList());

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", "source.py", "suite.robot",
                Arrays.asList());

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.PYTHON);
        verifyZeroInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void failMessageEventIsHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());
        final RobotDryRunKeywordSourceCollector kwSourceCollector = mock(RobotDryRunKeywordSourceCollector.class);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage(createFailMessage("lib"), MessageLevel.FAIL);

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", "", "", Arrays.asList());
        lib.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib.setAdditionalInfo("(Importing test library lib failed)");

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.UNKNOWN);
        verifyZeroInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void errorMessageEventIsHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());
        final RobotDryRunKeywordSourceCollector kwSourceCollector = mock(RobotDryRunKeywordSourceCollector.class);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage(createErrorMessage("lib", "suite.robot"), MessageLevel.ERROR);

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", "", "suite.robot", Arrays.asList());
        lib.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib.setAdditionalInfo("Error in file 'suite.robot': Test library 'lib' does not exist.");

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.UNKNOWN);
        verifyZeroInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void multipleImportersLibraryImportEventsAreHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());
        final RobotDryRunKeywordSourceCollector kwSourceCollector = mock(RobotDryRunKeywordSourceCollector.class);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleLibraryImport("lib", "suite1.robot", "lib.py", Arrays.asList());
        listener.handleLibraryImport("lib", "suite2.robot", "lib.py", Arrays.asList());
        listener.handleLibraryImport("lib", "suite3.robot", "lib.py", Arrays.asList());

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib", "lib.py", "suite1.robot",
                Arrays.asList());
        lib.addImporterPath("suite2.robot");
        lib.addImporterPath("suite3.robot");

        assertThat(libImportCollector.getImportedLibraries()).hasSize(1);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib, DryRunLibraryType.PYTHON);
        verifyZeroInteractions(kwSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void libraryImportAndMultipleMessageEventsAreCombined() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());
        final RobotDryRunKeywordSourceCollector kwSourceCollector = new RobotDryRunKeywordSourceCollector();

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleLibraryImport("lib1", "suite1.robot", "lib1.py", Arrays.asList());
        listener.handleMessage(createErrorMessage("lib2", "suite2.robot"), MessageLevel.ERROR);
        listener.handleMessage(createFailMessage("lib3"), MessageLevel.FAIL);
        listener.handleMessage(createFailMessage("lib4"), MessageLevel.FAIL);
        listener.handleMessage(createErrorMessage("lib5", "suite5.robot"), MessageLevel.ERROR);
        listener.handleMessage(createErrorMessage("lib5", "other.robot"), MessageLevel.ERROR);
        listener.handleLibraryImport("lib6", "suite6.robot", "lib6.py", Arrays.asList());
        listener.handleLibraryImport("lib6", "other.robot", "lib6.py", Arrays.asList());
        listener.handleMessage(createKeywordMessage("kw", "lib1", "lib1.py", 3, 5, 7), MessageLevel.NONE);

        final RobotDryRunLibraryImport lib1 = new RobotDryRunLibraryImport("lib1", "lib1.py", "suite1.robot",
                Arrays.asList());

        final RobotDryRunLibraryImport lib2 = new RobotDryRunLibraryImport("lib2", "", "suite2.robot", Arrays.asList());
        lib2.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib2.setAdditionalInfo("Error in file 'suite2.robot': Test library 'lib2' does not exist.");

        final RobotDryRunLibraryImport lib3 = new RobotDryRunLibraryImport("lib3", "", "", Arrays.asList());
        lib3.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib3.setAdditionalInfo("(Importing test library lib3 failed)");

        final RobotDryRunLibraryImport lib4 = new RobotDryRunLibraryImport("lib4", "", "", Arrays.asList());
        lib4.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib4.setAdditionalInfo("(Importing test library lib4 failed)");

        final RobotDryRunLibraryImport lib5 = new RobotDryRunLibraryImport("lib5", "", "suite5.robot", Arrays.asList());
        lib5.addImporterPath("other.robot");
        lib5.setStatus(DryRunLibraryImportStatus.NOT_ADDED);
        lib5.setAdditionalInfo("Error in file 'other.robot': Test library 'lib5' does not exist.");

        final RobotDryRunLibraryImport lib6 = new RobotDryRunLibraryImport("lib6", "lib6.py", "suite6.robot",
                Arrays.asList());
        lib6.addImporterPath("other.robot");

        final RobotDryRunKeywordSource kw = new RobotDryRunKeywordSource();
        kw.setName("kw");
        kw.setLibraryName("lib1");
        kw.setFilePath("lib1.py");
        kw.setLine(3);
        kw.setOffset(5);
        kw.setLength(7);

        assertThat(libImportCollector.getImportedLibraries()).hasSize(6);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(0), lib1, DryRunLibraryType.PYTHON);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(1), lib2, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(2), lib3, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(3), lib4, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(4), lib5, DryRunLibraryType.UNKNOWN);
        assertCollectedLibraryImport(libImportCollector.getImportedLibraries().get(5), lib6, DryRunLibraryType.PYTHON);
        assertThat(kwSourceCollector.getKeywordSources()).hasSize(1);
        assertCollectedKeywordSource(kwSourceCollector.getKeywordSources().get(0), kw);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void keywordMessageEventsAreHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libImportCollector = mock(RobotDryRunLibraryImportCollector.class);
        final RobotDryRunKeywordSourceCollector kwSourceCollector = new RobotDryRunKeywordSourceCollector();

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libImportCollector, kwSourceCollector,
                startSuiteHandler);

        listener.handleMessage(createKeywordMessage("kw1", "lib1", "lib1.py", 3, 5, 7), MessageLevel.NONE);
        listener.handleMessage(createKeywordMessage("kw2", "lib1", "lib1.py", 5, 6, 4), MessageLevel.NONE);
        listener.handleMessage(createKeywordMessage("other_kw", "lib2", "lib2.py", 2, 4, 6), MessageLevel.NONE);

        final RobotDryRunKeywordSource kw1 = new RobotDryRunKeywordSource();
        kw1.setName("kw1");
        kw1.setLibraryName("lib1");
        kw1.setFilePath("lib1.py");
        kw1.setLine(3);
        kw1.setOffset(5);
        kw1.setLength(7);

        final RobotDryRunKeywordSource kw2 = new RobotDryRunKeywordSource();
        kw2.setName("kw2");
        kw2.setLibraryName("lib1");
        kw2.setFilePath("lib1.py");
        kw2.setLine(5);
        kw2.setOffset(6);
        kw2.setLength(4);

        final RobotDryRunKeywordSource kw3 = new RobotDryRunKeywordSource();
        kw3.setName("other_kw");
        kw3.setLibraryName("lib2");
        kw3.setFilePath("lib2.py");
        kw3.setLine(2);
        kw3.setOffset(4);
        kw3.setLength(6);

        assertThat(kwSourceCollector.getKeywordSources()).hasSize(3);
        assertCollectedKeywordSource(kwSourceCollector.getKeywordSources().get(0), kw1);
        assertCollectedKeywordSource(kwSourceCollector.getKeywordSources().get(1), kw2);
        assertCollectedKeywordSource(kwSourceCollector.getKeywordSources().get(2), kw3);
        verifyZeroInteractions(libImportCollector);
        verifyZeroInteractions(startSuiteHandler);
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

    private static void assertCollectedKeywordSource(final RobotDryRunKeywordSource actual,
            final RobotDryRunKeywordSource expected) {
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getLibraryName()).isEqualTo(expected.getLibraryName());
        assertThat(actual.getFilePath()).isEqualTo(expected.getFilePath());
        assertThat(actual.getLine()).isEqualTo(expected.getLine());
        assertThat(actual.getOffset()).isEqualTo(expected.getOffset());
        assertThat(actual.getLength()).isEqualTo(expected.getLength());
    }

    private static String createErrorMessage(final String libName, final String fileName) {
        return "Error in file '" + fileName + "': Test library '" + libName + "' does not exist.";
    }

    private static String createFailMessage(final String libName) {
        return "{LIB_ERROR: " + libName + ", value: VALUE_START((Importing test library " + libName
                + " failed))VALUE_END, lib_file_import:None}";
    }

    private static String createKeywordMessage(final String name, final String libraryName, final String path,
            final int line, final int offset, final int length) throws Exception {
        final Object kwAttributes = ImmutableMap.builder()
                .put("filePath", path)
                .put("length", length)
                .put("libraryName", libraryName)
                .put("line", line)
                .put("name", name)
                .put("offset", offset)
                .build();
        return new ObjectMapper().writeValueAsString((ImmutableMap.of("keyword", kwAttributes)));
    }
}
