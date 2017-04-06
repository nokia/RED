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
import java.util.List;
import java.util.function.Consumer;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;

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
        final RobotDryRunLibraryImportCollector libraryImportCollector = mock(RobotDryRunLibraryImportCollector.class);
        final RobotDryRunKeywordSourceCollector keywordSourceCollector = mock(RobotDryRunKeywordSourceCollector.class);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libraryImportCollector,
                keywordSourceCollector, startSuiteHandler);

        listener.handleSuiteStarted("abc", new File("path"));

        verify(startSuiteHandler).accept("abc");
        verifyNoMoreInteractions(startSuiteHandler);
        verifyZeroInteractions(libraryImportCollector);
        verifyZeroInteractions(keywordSourceCollector);
    }

    @Test
    public void standardLibraryImportIsHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libraryImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of("String"));
        final RobotDryRunKeywordSourceCollector keywordSourceCollector = mock(RobotDryRunKeywordSourceCollector.class);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libraryImportCollector,
                keywordSourceCollector, startSuiteHandler);

        listener.handleLibraryImport("String", "testProject/testSuite.robot", "python/String.py", Arrays.asList());

        assertThat(libraryImportCollector.getImportedLibraries()).isEmpty();
        verifyZeroInteractions(keywordSourceCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void keywordMessageEventsAreHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libraryImportCollector = mock(RobotDryRunLibraryImportCollector.class);
        final RobotDryRunKeywordSourceCollector keywordSourceCollector = new RobotDryRunKeywordSourceCollector();

        final RobotDryRunKeywordSource kw1 = new RobotDryRunKeywordSource();
        kw1.setName("kw1");
        kw1.setLibraryName("lib1");
        kw1.setFilePath("testProject/lib1.py");
        kw1.setLine(3);
        kw1.setOffset(5);
        kw1.setLength(7);

        final RobotDryRunKeywordSource kw2 = new RobotDryRunKeywordSource();
        kw2.setName("kw2");
        kw2.setLibraryName("lib1");
        kw2.setFilePath("testProject/lib1.py");
        kw2.setLine(5);
        kw2.setOffset(6);
        kw2.setLength(4);

        final RobotDryRunKeywordSource kw3 = new RobotDryRunKeywordSource();
        kw3.setName("other_kw");
        kw3.setLibraryName("lib2");
        kw3.setFilePath("testProject/lib2.py");
        kw3.setLine(2);
        kw3.setOffset(4);
        kw3.setLength(6);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libraryImportCollector,
                keywordSourceCollector, startSuiteHandler);

        listener.handleMessage(createKeywordMessage(kw1), "NONE");
        listener.handleMessage(createKeywordMessage(kw3), "NONE");
        listener.handleMessage(createKeywordMessage(kw2), "NONE");

        verifyCollectedKeywordSources(keywordSourceCollector.getKeywordSources(), kw1, kw3, kw2);
        verifyZeroInteractions(libraryImportCollector);
        verifyZeroInteractions(startSuiteHandler);
    }

    @Test
    public void keywordMessageEventAndFailMessageEventAreHandled() throws Exception {
        final RobotDryRunLibraryImportCollector libraryImportCollector = new RobotDryRunLibraryImportCollector(
                ImmutableSet.of());
        final RobotDryRunKeywordSourceCollector keywordSourceCollector = new RobotDryRunKeywordSourceCollector();

        final RobotDryRunKeywordSource kw = new RobotDryRunKeywordSource();
        kw.setName("kw");
        kw.setLibraryName("lib1");
        kw.setFilePath("testProject/lib1.py");
        kw.setLine(3);
        kw.setOffset(5);
        kw.setLength(7);

        final RobotDryRunLibraryImport lib = new RobotDryRunLibraryImport("lib2", "", "", Arrays.asList());
        lib.setAdditionalInfo("(Importing test library lib2 failed)");
        lib.setStatus(DryRunLibraryImportStatus.NOT_ADDED);

        final RobotDryRunEventListener listener = new RobotDryRunEventListener(libraryImportCollector,
                keywordSourceCollector, startSuiteHandler);

        listener.handleMessage(createKeywordMessage(kw), "NONE");
        listener.handleMessage(createFailMessageJSON(lib.getName()), "FAIL");

        verifyCollectedLibraryImports(DryRunLibraryType.UNKNOWN, libraryImportCollector.getImportedLibraries(), lib);
        verifyCollectedKeywordSources(keywordSourceCollector.getKeywordSources(), kw);
        verifyZeroInteractions(startSuiteHandler);
    }

    private void verifyCollectedLibraryImports(final DryRunLibraryType expectedType,
            final List<RobotDryRunLibraryImport> actualLibImports,
            final RobotDryRunLibraryImport... expectedLibImports) {
        assertThat(actualLibImports).hasSameSizeAs(expectedLibImports);

        for (int i = 0; i < expectedLibImports.length; i++) {
            final RobotDryRunLibraryImport actual = actualLibImports.get(i);
            final RobotDryRunLibraryImport expected = expectedLibImports[i];

            assertThat(actual.getType()).isEqualTo(expectedType);
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getArgs()).isEqualTo(expected.getArgs());
            assertThat(actual.getSourcePath()).isEqualTo(expected.getSourcePath());
            assertThat(actual.getImportersPaths()).isEqualTo(expected.getImportersPaths());
            assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
            assertThat(actual.getAdditionalInfo()).isEqualTo(expected.getAdditionalInfo());
        }
    }

    private void verifyCollectedKeywordSources(final List<RobotDryRunKeywordSource> actualKwSources,
            final RobotDryRunKeywordSource... expectedKwSources) {
        assertThat(actualKwSources).hasSameSizeAs(expectedKwSources);

        for (int i = 0; i < expectedKwSources.length; i++) {
            final RobotDryRunKeywordSource actual = actualKwSources.get(i);
            final RobotDryRunKeywordSource expected = expectedKwSources[i];

            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getLibraryName()).isEqualTo(expected.getLibraryName());
            assertThat(actual.getFilePath()).isEqualTo(expected.getFilePath());
            assertThat(actual.getLine()).isEqualTo(expected.getLine());
            assertThat(actual.getOffset()).isEqualTo(expected.getOffset());
            assertThat(actual.getLength()).isEqualTo(expected.getLength());
        }
    }

    private static String createKeywordMessage(final RobotDryRunKeywordSource kwSource) throws Exception {
        final Object kwAttributes = ImmutableMap.builder()
                .put("filePath", kwSource.getFilePath())
                .put("length", kwSource.getLength())
                .put("libraryName", kwSource.getLibraryName())
                .put("line", kwSource.getLine())
                .put("name", kwSource.getName())
                .put("offset", kwSource.getOffset())
                .build();
        return toJson(ImmutableMap.of("keyword", kwAttributes));
    }

    private String createFailMessageJSON(final String libName) throws Exception {
        final String message = "{LIB_ERROR: " + libName + ", value: VALUE_START((Importing test library " + libName
                + " failed))VALUE_END, lib_file_import:None}";
        return toJson(ImmutableMap.of("message",
                Arrays.asList(ImmutableMap.of("level", "FAIL", "message", message, "timestamp", "12345"))));
    }

    private static String toJson(final Object object) throws Exception {
        return new ObjectMapper().writeValueAsString(object);
    }
}
