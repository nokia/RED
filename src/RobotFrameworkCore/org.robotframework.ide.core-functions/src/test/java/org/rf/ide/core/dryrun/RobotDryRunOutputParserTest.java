/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;

public class RobotDryRunOutputParserTest {

    private RobotDryRunOutputParser dryRunOutputParser;

    @Before
    public void setUp() {
        dryRunOutputParser = new RobotDryRunOutputParser();
        dryRunOutputParser.setupRobotDryRunLibraryImportCollector(new HashSet<String>());
    }

    @Test
    public void testProcessLine_withPythonLibraryImports() {
        String libName = "lib1";
        final List<String> args = newArrayList("a", "b");
        final List<String> importers = newArrayList("testProject/testSuite.robot");
        String source = "testProject/lib1.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, source, DryRunLibraryType.PYTHON);

        libName = "lib2";
        source = "testProject/lib2.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 1, 2, libName, importers, args, source, DryRunLibraryType.PYTHON);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndSpecificSourceExtension() {
        final String libName = "lib1";
        final List<String> args = newArrayList("a", "b");
        final List<String> importers = newArrayList("testProject/testSuite.robot");
        final String source = "testProject/lib1.pyc";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));

        final String expectedSource = "testProject/lib1.py";
        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, expectedSource,
                DryRunLibraryType.PYTHON);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndAnotherSpecificSourceExtension() {
        final String libName = "lib1";
        final List<String> args = newArrayList("a", "b");
        final List<String> importers = newArrayList("testProject/testSuite.robot");
        final String source = "testProject/lib1$py.class";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));

        final String expectedSource = "testProject/lib1.py";
        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, expectedSource,
                DryRunLibraryType.PYTHON);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndMultipleImporters() {
        final String libName = "lib1";
        final List<String> args = newArrayList();
        final List<String> importers = newArrayList("testProject/testSuite1.robot", "testProject/testSuite2.robot",
                "testProject/testSuite3.robot");
        final String source = "testProject/lib1.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(1), source));
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(2), source));

        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, source, DryRunLibraryType.PYTHON);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndClasses() {
        String libName = "lib1.class1";
        final List<String> args = newArrayList();
        final List<String> importers = newArrayList("testProject/testSuite.robot");
        final String source = "testProject/lib1.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, source, DryRunLibraryType.PYTHON);

        libName = "lib1.class2";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 1, 2, libName, importers, args, source, DryRunLibraryType.PYTHON);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndNullSource() {
        final String libName = "lib1";
        final List<String> args = newArrayList();
        final List<String> importers = newArrayList();
        final String source = null;

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, "", source));

        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, "", DryRunLibraryType.UNKNOWN);
    }

    @Test
    public void testProcessLine_withJavaLibraryImport() {
        final String libName = "org.core.Library";
        final List<String> args = newArrayList();
        final List<String> importers = newArrayList("testProject/testSuite.robot");
        final String source = "testProject/LibrarySource.jar";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));

        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, source, DryRunLibraryType.JAVA);
    }

    @Test
    public void testProcessLine_withStandardLibraryImport() {
        final String libName = "String";
        final Set<String> standardLibs = new HashSet<String>();
        standardLibs.add(libName);
        dryRunOutputParser = new RobotDryRunOutputParser();
        dryRunOutputParser.setupRobotDryRunLibraryImportCollector(standardLibs);
        final List<String> args = newArrayList();
        final List<String> importers = newArrayList("testProject/testSuite.robot");
        final String source = "python/String.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));

        assertTrue(dryRunOutputParser.getImportedLibraries().isEmpty());
    }

    @Test
    public void testProcessLine_withErrorMessagesAndMultipleImporters() {
        String libName = "lib1.py";
        final List<String> importers = newArrayList("testProject/testSuite1.robot", "testProject/testSuite2.robot");

        dryRunOutputParser.processLine(createErrorMessageJSON(importers.get(0), libName));
        dryRunOutputParser.processLine(createErrorMessageJSON(importers.get(1), libName));

        verifyMessage(dryRunOutputParser, 0, 1, libName, importers);

        libName = "lib2.py";
        dryRunOutputParser.processLine(createErrorMessageJSON(importers.get(0), libName));
        dryRunOutputParser.processLine(createErrorMessageJSON(importers.get(1), libName));

        verifyMessage(dryRunOutputParser, 1, 2, libName, importers);
    }

    @Test
    public void testProcessLine_withFailMessages() {
        String libName = "lib1";
        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        dryRunOutputParser.processLine(createFailMessageJSON(libName));

        verifyMessage(dryRunOutputParser, 0, 1, libName, new ArrayList<String>());

        libName = "lib2";
        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        dryRunOutputParser.processLine(createFailMessageJSON(libName));

        verifyMessage(dryRunOutputParser, 1, 2, libName, new ArrayList<String>());
    }

    @Test
    public void testProcessLine_withFailMessageAndNotExistingLibraryAndMultipleImporters() {
        final String libName = "lib1";
        final List<String> args = newArrayList();
        final List<String> importers = newArrayList("testProject/testSuite1.robot", "testProject/testSuite2.robot");
        final String source = null;

        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        verifyMessage(dryRunOutputParser, 0, 1, libName, new ArrayList<String>());
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, newArrayList(importers.get(0)), args, "",
                DryRunLibraryType.UNKNOWN);

        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        verifyMessage(dryRunOutputParser, 0, 1, libName, new ArrayList<String>());
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(1), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, "", DryRunLibraryType.UNKNOWN);
    }

    @Test
    public void testProcessLine_withFailMessagesAndExistingLibrary() {
        String libName = "lib1";
        final List<String> args = newArrayList();
        final List<String> importers = newArrayList("testProject/testSuite1.robot");
        String source = "testProject/lib1.py";

        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        verifyMessage(dryRunOutputParser, 0, 1, libName, new ArrayList<String>());
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, libName, importers, args, "", DryRunLibraryType.UNKNOWN);

        libName = "lib2";
        source = "testProject/lib2.py";

        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        verifyMessage(dryRunOutputParser, 1, 2, libName, new ArrayList<String>());
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 1, 2, libName, importers, args, "", DryRunLibraryType.UNKNOWN);
    }

    @Test
    public void testProcessLine_withStartSuiteEvent() {
        final String suiteName = "testSuite1";
        dryRunOutputParser.setStartSuiteHandler(new IDryRunStartSuiteHandler() {

            @Override
            public void processStartSuiteEvent(final String name) {
                assertEquals(suiteName, name);
            }
        });
        dryRunOutputParser.processLine(createStartSuiteJSON(suiteName));
    }

    @Test
    public void testProcessLine_withKeywordMessage() {
        final RobotDryRunKeywordSource kwSource = new RobotDryRunKeywordSource();
        kwSource.setName("kw");
        kwSource.setLibraryName("lib1");
        kwSource.setFilePath("testProject/lib1.py");
        kwSource.setLine(3);
        kwSource.setOffset(5);
        kwSource.setLength(7);

        dryRunOutputParser.processLine(createKeywordMessageJSON(kwSource));
        verifyKeywordMessage(dryRunOutputParser, 0, 1, kwSource);

        kwSource.setName("other_kw");
        kwSource.setLibraryName("lib2");
        kwSource.setFilePath("testProject/lib2.py");
        kwSource.setLine(2);
        kwSource.setOffset(4);
        kwSource.setLength(6);

        dryRunOutputParser.processLine(createKeywordMessageJSON(kwSource));
        verifyKeywordMessage(dryRunOutputParser, 1, 2, kwSource);
    }

    @Test
    public void testProcessLine_withKeywordMessageAndFailMessage() {
        final RobotDryRunKeywordSource kwSource = new RobotDryRunKeywordSource();
        kwSource.setName("kw");
        kwSource.setLibraryName("lib1");
        kwSource.setFilePath("testProject/lib1.py");
        kwSource.setLine(3);
        kwSource.setOffset(5);
        kwSource.setLength(7);

        dryRunOutputParser.processLine(createKeywordMessageJSON(kwSource));
        verifyKeywordMessage(dryRunOutputParser, 0, 1, kwSource);

        dryRunOutputParser.processLine(createFailMessageJSON(kwSource.getLibraryName()));
        verifyMessage(dryRunOutputParser, 0, 1, kwSource.getLibraryName(), new ArrayList<String>());
    }

    private void verifyLibraryImport(final RobotDryRunOutputParser dryRunOutputParser, final int importedLibraryIndex,
            final int expectedImportedLibrariesSize, final String expectedLibName, final List<String> expectedImporters,
            final List<String> expectedArgs, final String expectedSource, final DryRunLibraryType expectedType) {
        final List<RobotDryRunLibraryImport> importedLibraries = dryRunOutputParser.getImportedLibraries();
        assertEquals(expectedImportedLibrariesSize, importedLibraries.size());
        final RobotDryRunLibraryImport dryRunLibraryImport = importedLibraries.get(importedLibraryIndex);
        assertEquals(expectedLibName, dryRunLibraryImport.getName());
        assertTrue(dryRunLibraryImport.getImportersPaths().containsAll(expectedImporters));

        assertTrue(dryRunLibraryImport.getArgs().containsAll(expectedArgs));
        assertEquals(expectedSource, dryRunLibraryImport.getSourcePath());
        assertEquals(expectedType, dryRunLibraryImport.getType());
    }

    private void verifyMessage(final RobotDryRunOutputParser dryRunOutputParser, final int importedLibraryIndex,
            final int expectedImportedLibrariesSize, final String expectedLibName,
            final List<String> expectedImporters) {
        final List<RobotDryRunLibraryImport> importedLibraries = dryRunOutputParser.getImportedLibraries();
        assertEquals(expectedImportedLibrariesSize, importedLibraries.size());
        final RobotDryRunLibraryImport dryRunLibraryImport = importedLibraries.get(importedLibraryIndex);
        assertEquals(expectedLibName, dryRunLibraryImport.getName());
        assertTrue(dryRunLibraryImport.getImportersPaths().containsAll(expectedImporters));

        assertTrue(dryRunLibraryImport.getArgs().isEmpty());
        assertTrue(dryRunLibraryImport.getSourcePath().isEmpty());
        assertEquals(DryRunLibraryType.UNKNOWN, dryRunLibraryImport.getType());
        assertEquals(DryRunLibraryImportStatus.NOT_ADDED, dryRunLibraryImport.getStatus());
        assertFalse(dryRunLibraryImport.getAdditionalInfo().isEmpty());
    }

    private void verifyKeywordMessage(final RobotDryRunOutputParser dryRunOutputParser, final int keywordSourceIndex,
            final int expectedKeywordSourceSize, final RobotDryRunKeywordSource expectedKeywordSource) {
        final List<RobotDryRunKeywordSource> keywordSources = dryRunOutputParser.getKeywordSources();
        assertEquals(expectedKeywordSourceSize, keywordSources.size());

        final RobotDryRunKeywordSource keywordSource = keywordSources.get(keywordSourceIndex);
        assertEquals(expectedKeywordSource.getName(), keywordSource.getName());
        assertEquals(expectedKeywordSource.getLibraryName(), keywordSource.getLibraryName());
        assertEquals(expectedKeywordSource.getFilePath(), keywordSource.getFilePath());
        assertEquals(expectedKeywordSource.getLine(), keywordSource.getLine());
        assertEquals(expectedKeywordSource.getOffset(), keywordSource.getOffset());
        assertEquals(expectedKeywordSource.getLength(), keywordSource.getLength());
    }

    private String createLibraryImportJSON(final String libName, final List<String> args, final String importer,
            final String source) {
        String argsString = "";
        for (int i = 0; i < args.size(); i++) {
            argsString += "\"" + args.get(i) + "\"";
            if (i < args.size() - 1) {
                argsString += ",";
            }
        }
        String sourceString = "";
        if (source != null) {
            sourceString = "\"" + source + "\"";
        } else {
            sourceString = "null";
        }

        return "{\"library_import\":[\"" + libName + "\",{\"args\":[" + argsString + "],\"importer\":\"" + importer
                + "\"," + "\"originalname\":\"" + libName + "\",\"source\":" + sourceString + "}]}";
    }

    private String createErrorMessageJSON(final String fileName, final String libName) {
        final String message = "Error in file '" + fileName + "': Test library '" + libName + "' does not exist.";
        return createMessageJSON("ERROR", message);
    }

    private String createFailMessageJSON(final String libName) {
        final String message = "{LIB_ERROR: " + libName + ", value: VALUE_START((Importing test library " + libName
                + " failed))VALUE_END, lib_file_import:None}";
        return createMessageJSON("FAIL", message);
    }

    private String createKeywordMessageJSON(final RobotDryRunKeywordSource source) {
        final String message = "{\\\"keyword\\\":{\\\"filePath\\\":\\\"" + source.getFilePath() + "\\\",\\\"length\\\":"
                + source.getLength() + ",\\\"libraryName\\\":\\\"" + source.getLibraryName() + "\\\",\\\"line\\\":"
                + source.getLine() + ",\\\"name\\\":\\\"" + source.getName() + "\\\",\\\"offset\\\":"
                + source.getOffset() + "}}";
        return createMessageJSON("NONE", message);
    }

    private String createMessageJSON(final String level, final String message) {
        return "{\"message\":[{\"html\":\"no\",\"level\":\"" + level + "\",\"message\":\"" + message
                + "\",\"timestamp\":\"12345\"}]}";
    }

    private String createStartSuiteJSON(final String suiteName) {
        return "{\"start_suite\":[\"" + suiteName + "\",{\"longname\":\"" + suiteName + "\"}]}";
    }
}
