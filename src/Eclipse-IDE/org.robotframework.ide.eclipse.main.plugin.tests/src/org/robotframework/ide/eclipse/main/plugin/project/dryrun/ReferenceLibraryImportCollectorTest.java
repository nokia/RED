/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.ADDED;
import static org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.NOT_ADDED;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.createImport;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class ReferenceLibraryImportCollectorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ReferenceLibraryImportCollectorTest.class);

    private RobotModel model;

    private RobotProject robotProject;

    @BeforeClass
    public static void beforeClass() throws Exception {
        projectProvider.createDir("libs");
        projectProvider.createDir("module");
        projectProvider.createDir("other");
        projectProvider.createDir("other/dir");

        projectProvider.createDir("A");
        projectProvider.createDir("A/B");
        projectProvider.createDir("A/B/C");

        projectProvider.createFile("libs/PathLib.py", "def kw():", " pass");
        projectProvider.createFile("other/dir/OtherPathLib.py", "def kw():", " pass");
        projectProvider.createFile("module/__init__.py", "class module(object):", "  def kw():", "   pass");
        projectProvider.createFile("libs/ErrorLib.py", "error():");
        projectProvider.createFile("libs/NameLib.py", "def kw():", " pass");
        projectProvider.createFile("libs/OtherNameLib.py", "def kw():", " pass");

        // this should not be found in any cases
        projectProvider.createFile("notUsedLib.py", "def kw():", " pass");
        projectProvider.createFile("notUsedTest.robot",
                "*** Settings ***",
                "Library  notUsedLib.py",
                "*** Test Cases ***",
                "  case 1");
    }

    @Before
    public void before() throws Exception {
        model = new RobotModel();
        robotProject = model.createRobotProject(projectProvider.getProject());
        projectProvider.configure();
    }

    @After
    public void after() throws Exception {
        model = null;
        robotProject.clearConfiguration();
    }

    @Test
    public void nothingIsCollected_whenLibrariesAreNotImported() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void nothingIsCollected_whenLibraryNameIsNotSpecified() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void nothingIsCollected_whenStandardLibrariesAreImported() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite3.robot",
                "*** Settings ***",
                "Library  BuiltIn",
                "Library  String",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportIsCollected_whenLibraryIsImportedByPath() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(ADDED, "PathLib",
                projectProvider.getFile("libs/PathLib.py"));
        assertThat(collector.getLibraryImports()).containsOnly(libImport);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportWithMultipleImportersIsCollected_whenLibraryIsImportedByPath() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("A/suite.robot",
                "*** Settings ***",
                "Library  ../libs/PathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("A/B/suite.robot",
                "*** Settings ***",
                "Library  ../../libs/PathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(projectProvider.createFile("A/B/C/suite.robot",
                "*** Settings ***",
                "Library  ../../../libs/PathLib.py",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(ADDED, "PathLib",
                projectProvider.getFile("libs/PathLib.py"));
        assertThat(collector.getLibraryImports()).containsOnly(libImport);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsAreCollected_whenLibrariesAreImportedByPath() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "Library  ./other/dir/OtherPathLib.py",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "PathLib",
                projectProvider.getFile("libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "OtherPathLib",
                projectProvider.getFile("other/dir/OtherPathLib.py"));
        assertThat(collector.getLibraryImports()).containsOnly(libImport1, libImport2);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsWithMultipleImportersAreCollected_whenLibrariesAreImportedByPath()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "Library  ./other/dir/OtherPathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("A/suite.robot",
                "*** Settings ***",
                "Library  ../other/dir/OtherPathLib.py",
                "Library  ../module/",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("A/B/suite.robot",
                "*** Settings ***",
                "Library  ../../libs/PathLib.py",
                "Library  ../../module/",
                "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(projectProvider.createFile("A/B/C/suite.robot",
                "*** Settings ***",
                "Library  ../../../module/",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "PathLib",
                projectProvider.getFile("libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "OtherPathLib",
                projectProvider.getFile("other/dir/OtherPathLib.py"));
        final RobotDryRunLibraryImport libImport3 = createImport(ADDED, "module",
                projectProvider.getFile("module/__init__.py"));
        assertThat(collector.getLibraryImports()).containsOnly(libImport1, libImport2, libImport3);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(3);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1, suite3));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite1, suite2));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport3,
                newArrayList(suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportIsCollected_whenLibraryIsImportedByName() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPath(newArrayList(SearchPath.create(projectProvider.getDir("libs").getLocation().toString())));
        projectProvider.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(ADDED, "NameLib",
                projectProvider.getFile("libs/NameLib.py"));
        assertThat(collector.getLibraryImports()).containsOnly(libImport);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportWithMultipleImportersIsCollected_whenLibraryIsImportedByName() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPath(newArrayList(SearchPath.create(projectProvider.getDir("libs").getLocation().toString())));
        projectProvider.configure(config);

        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("A/suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("A/B/suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(projectProvider.createFile("A/B/C/suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(ADDED, "NameLib",
                projectProvider.getFile("libs/NameLib.py"));
        assertThat(collector.getLibraryImports()).containsOnly(libImport);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsAreCollected_whenLibrariesAreImportedByName() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPath(newArrayList(SearchPath.create(projectProvider.getDir("libs").getLocation().toString())));
        projectProvider.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "Library  OtherNameLib",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "NameLib",
                projectProvider.getFile("libs/NameLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "OtherNameLib",
                projectProvider.getFile("libs/OtherNameLib.py"));
        assertThat(collector.getLibraryImports()).containsOnly(libImport1, libImport2);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsWithMultipleImportersAreCollected_whenLibrariesAreImportedByName()
            throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPath(newArrayList(SearchPath.create(projectProvider.getDir("libs").getLocation().toString()),
                SearchPath.create(projectProvider.getProject().getLocation().toString())));
        projectProvider.configure(config);

        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "Library  OtherNameLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("A/suite.robot",
                "*** Settings ***",
                "Library  OtherNameLib",
                "Library  module",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("A/B/suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "Library  module",
                "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(projectProvider.createFile("A/B/C/suite.robot",
                "*** Settings ***",
                "Library  module",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "NameLib",
                projectProvider.getFile("libs/NameLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "OtherNameLib",
                projectProvider.getFile("libs/OtherNameLib.py"));
        final RobotDryRunLibraryImport libImport3 = createImport(ADDED, "module",
                projectProvider.getFile("module/__init__.py"));
        assertThat(collector.getLibraryImports()).containsOnly(libImport1, libImport2, libImport3);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(3);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1, suite3));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite1, suite2));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport3,
                newArrayList(suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryNameIsCollected_whenUnknownLibraryIsImportedByName() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(1);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(suite));
    }

    @Test
    public void singleLibraryNameWithMultipleImportersIsCollected_whenUnknownLibraryIsImportedByName()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(1);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(suite1, suite2));
    }

    @Test
    public void multipleLibraryNamesAreCollected_whenUnknownLibrariesAreImportedByName() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPath(newArrayList(SearchPath.create(projectProvider.getDir("libs").getLocation().toString())));
        projectProvider.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "Library  ErrorLib",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(2);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("ErrorLib", newArrayList(suite));
    }

    @Test
    public void multipleLibraryNamesWithMultipleImportersAreCollected_whenUnknownLibrariesAreImportedByName()
            throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPath(newArrayList(SearchPath.create(projectProvider.getDir("libs").getLocation().toString())));
        projectProvider.configure(config);

        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "Library  ErrorLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(2);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(suite1, suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("ErrorLib", newArrayList(suite1));
    }

    @Test
    public void singleLibraryImportIsCollected_whenLibraryIsImportedByUnknownPath() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(NOT_ADDED, "./libs/UnknownLib.py");
        assertThat(collector.getLibraryImports()).containsOnly(libImport);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportWithMultipleImportersIsCollected_whenLibraryIsImportedByUnknownPath()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(NOT_ADDED, "./libs/UnknownLib.py");
        assertThat(collector.getLibraryImports()).containsOnly(libImport);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite1, suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsAreCollected_whenLibrariesAreImportedByUnknownPath() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "Library  ../other/OtherUnknownLib.py",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(NOT_ADDED, "./libs/UnknownLib.py");
        final RobotDryRunLibraryImport libImport2 = createImport(NOT_ADDED, "../other/OtherUnknownLib.py");
        assertThat(collector.getLibraryImports()).containsOnly(libImport1, libImport2);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsWithMultipleImportersAreCollected_whenLibrariesAreImportedByUnknownPath()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "Library  ../other/OtherUnknownLib.py",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(NOT_ADDED, "./libs/UnknownLib.py");
        final RobotDryRunLibraryImport libImport2 = createImport(NOT_ADDED, "../other/OtherUnknownLib.py");
        assertThat(collector.getLibraryImports()).containsOnly(libImport1, libImport2);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1, suite2));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void libraryImportsAreCollected_whenVariablesAreNotResolved() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***",
                "Library  ./${unknown_path}/UnknownLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library  ${unknown_name}",
                "*** Test Cases ***"));

        final ReferenceLibraryImportCollector collector = new ReferenceLibraryImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(NOT_ADDED, "./${unknown_path}/UnknownLib.py");
        final RobotDryRunLibraryImport libImport2 = createImport(NOT_ADDED, "${unknown_name}");
        assertThat(collector.getLibraryImports()).containsOnly(libImport1, libImport2);
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }
}
