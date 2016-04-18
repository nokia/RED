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
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryType;
import org.rf.ide.core.dryrun.RobotDryRunOutputParser;

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
        List<String> args = newArrayList("a", "b");
        List<String> importers = newArrayList("testProject/testSuite.robot");
        String source = "testProject/lib1.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.PYTHON, libName, args, importers, source);

        libName = "lib2";
        source = "testProject/lib2.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 1, 2, DryRunLibraryType.PYTHON, libName, args, importers, source);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndSpecificSourceExtension() {
        String libName = "lib1";
        List<String> args = newArrayList("a", "b");
        List<String> importers = newArrayList("testProject/testSuite.robot");
        String source = "testProject/lib1.pyc";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));

        String expectedSource = "testProject/lib1.py";
        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.PYTHON, libName, args, importers,
                expectedSource);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndAnotherSpecificSourceExtension() {
        String libName = "lib1";
        List<String> args = newArrayList("a", "b");
        List<String> importers = newArrayList("testProject/testSuite.robot");
        String source = "testProject/lib1$py.class";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));

        String expectedSource = "testProject/lib1.py";
        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.PYTHON, libName, args, importers,
                expectedSource);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndMultipleImporters() {
        String libName = "lib1";
        List<String> args = newArrayList();
        List<String> importers = newArrayList("testProject/testSuite1.robot", "testProject/testSuite2.robot",
                "testProject/testSuite3.robot");
        String source = "testProject/lib1.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(1), source));
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(2), source));

        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.PYTHON, libName, args, importers, source);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndClasses() {
        String libName = "lib1.class1";
        List<String> args = newArrayList();
        List<String> importers = newArrayList("testProject/testSuite.robot");
        String source = "testProject/lib1.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.PYTHON, libName, args, importers, source);

        libName = "lib1.class2";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 1, 2, DryRunLibraryType.PYTHON, libName, args, importers, source);
    }

    @Test
    public void testProcessLine_withPythonLibraryImportAndNullSource() {
        String libName = "lib1";
        List<String> args = newArrayList();
        List<String> importers = newArrayList();
        String source = null;

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, "", source));

        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.UNKNOWN, libName, args, importers, "");
    }

    @Test
    public void testProcessLine_withJavaLibraryImport() {
        String libName = "org.core.Library";
        List<String> args = newArrayList();
        List<String> importers = newArrayList("testProject/testSuite.robot");
        String source = "testProject/LibrarySource.jar";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));

        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.JAVA, libName, args, importers, source);
    }

    @Test
    public void testProcessLine_withStandardLibraryImport() {
        String libName = "String";
        Set<String> standardLibs = new HashSet<String>();
        standardLibs.add(libName);
        dryRunOutputParser = new RobotDryRunOutputParser();
        dryRunOutputParser.setupRobotDryRunLibraryImportCollector(standardLibs);
        List<String> args = newArrayList();
        List<String> importers = newArrayList("testProject/testSuite.robot");
        String source = "python/String.py";

        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));

        assertTrue(dryRunOutputParser.getImportedLibraries().isEmpty());
    }

    @Test
    public void testProcessLine_withErrorMessagesAndMultipleImporters() {
        String libName = "lib1.py";
        List<String> importers = newArrayList("testProject/testSuite1.robot", "testProject/testSuite2.robot");

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
        String libName = "lib1";
        List<String> args = newArrayList();
        List<String> importers = newArrayList("testProject/testSuite1.robot", "testProject/testSuite2.robot");
        String source = null;

        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        verifyMessage(dryRunOutputParser, 0, 1, libName, new ArrayList<String>());
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.UNKNOWN, libName, args,
                newArrayList(importers.get(0)), "");

        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        verifyMessage(dryRunOutputParser, 0, 1, libName, new ArrayList<String>());
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(1), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.UNKNOWN, libName, args, importers, "");
    }

    @Test
    public void testProcessLine_withFailMessagesAndExistingLibrary() {
        String libName = "lib1";
        List<String> args = newArrayList();
        List<String> importers = newArrayList("testProject/testSuite1.robot");
        String source = "testProject/lib1.py";

        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        verifyMessage(dryRunOutputParser, 0, 1, libName, new ArrayList<String>());
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 0, 1, DryRunLibraryType.UNKNOWN, libName, args, importers, "");

        libName = "lib2";
        source = "testProject/lib2.py";

        dryRunOutputParser.processLine(createFailMessageJSON(libName));
        verifyMessage(dryRunOutputParser, 1, 2, libName, new ArrayList<String>());
        dryRunOutputParser.processLine(createLibraryImportJSON(libName, args, importers.get(0), source));
        verifyLibraryImport(dryRunOutputParser, 1, 2, DryRunLibraryType.UNKNOWN, libName, args, importers, "");
    }
    
    @Test
    public void testProcessLine_withStartSuiteEvent() {
        final String suiteName = "testSuite1";
        dryRunOutputParser.setStartSuiteHandler(new IDryRunStartSuiteHandler() {

            @Override
            public void processStartSuiteEvent(String name) {
                assertEquals(suiteName, name);
            }
        });
        dryRunOutputParser.processLine(createStartSuiteJSON(suiteName));
    }

    private void verifyLibraryImport(RobotDryRunOutputParser dryRunOutputParser, int importedLibraryIndex,
            int expectedImportedLibrariesSize, DryRunLibraryType expectedType, String expectedLibName,
            List<String> expectedArgs, List<String> expectedImporters, String expectedSource) {

        List<RobotDryRunLibraryImport> importedLibraries = dryRunOutputParser.getImportedLibraries();
        assertEquals(expectedImportedLibrariesSize, importedLibraries.size());
        RobotDryRunLibraryImport dryRunLibraryImport = importedLibraries.get(importedLibraryIndex);
        assertEquals(expectedLibName, dryRunLibraryImport.getName());
        assertTrue(dryRunLibraryImport.getArgs().containsAll(expectedArgs));
        assertTrue(dryRunLibraryImport.getImportersPaths().containsAll(expectedImporters));
        assertEquals(expectedSource, dryRunLibraryImport.getSourcePath());
        assertEquals(expectedType, dryRunLibraryImport.getType());
    }

    private void verifyMessage(RobotDryRunOutputParser dryRunOutputParser, int importedLibraryIndex,
            int expectedImportedLibrariesSize, String expectedLibName, List<String> expectedImporters) {
        List<RobotDryRunLibraryImport> importedLibraries = dryRunOutputParser.getImportedLibraries();
        assertEquals(expectedImportedLibrariesSize, importedLibraries.size());
        RobotDryRunLibraryImport dryRunLibraryImport = importedLibraries.get(importedLibraryIndex);
        assertEquals(expectedLibName, dryRunLibraryImport.getName());
        assertTrue(dryRunLibraryImport.getArgs().isEmpty());
        assertTrue(dryRunLibraryImport.getImportersPaths().containsAll(expectedImporters));
        assertTrue(dryRunLibraryImport.getSourcePath().isEmpty());
        assertEquals(DryRunLibraryType.UNKNOWN, dryRunLibraryImport.getType());
        assertEquals(DryRunLibraryImportStatus.NOT_ADDED, dryRunLibraryImport.getStatus());
        assertFalse(dryRunLibraryImport.getAdditionalInfo().isEmpty());
    }

    private String createLibraryImportJSON(String libName, List<String> args, String importer, String source) {
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

    private String createErrorMessageJSON(String fileName, String libName) {
        return "{\"message\":[{\"html\":\"no\",\"level\":\"ERROR\",\"message\":\"Error in file '" + fileName
                + "': Test library '" + libName + "' does not exist.\",\"timestamp\":\"12345\"}]}";
    }

    private String createFailMessageJSON(String libName) {
        return "{\"message\":[{\"html\":\"no\",\"level\":\"FAIL\",\"message\":\"{LIB_ERROR: " + libName
                + ", value: VALUE_START((Importing test library " + libName
                + " failed))VALUE_END, lib_file_import:None}\",\"timestamp\":\"12345\"}]}";
    }
    
    private String createStartSuiteJSON(String suiteName) {
        return "{\"start_suite\":[\"" + suiteName + "\",{\"longname\":\"" + suiteName + "\"}]}";
    }
}
