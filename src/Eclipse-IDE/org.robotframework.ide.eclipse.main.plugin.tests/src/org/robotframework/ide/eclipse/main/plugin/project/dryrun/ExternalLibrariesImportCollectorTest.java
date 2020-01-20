/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.ADDED;
import static org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.NOT_ADDED;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.createImport;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.onlyLibImports;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class ExternalLibrariesImportCollectorTest {

    @Project(dirs = { "libs", "module", "other", "other/dir", "A", "A/B", "A/B/C" })
    static IProject project;

    private RobotModel model;

    private RobotProject robotProject;

    @BeforeAll
    public static void beforeClass() throws Exception {
        createFile(project, "libs/PathLib.py", "def kw():", " pass");
        createFile(project, "other/dir/OtherPathLib.py", "def kw():", " pass");
        createFile(project, "module/__init__.py", "class module(object):", "  def kw():", "   pass");
        createFile(project, "libs/ErrorLib.py", "error():");
        createFile(project, "libs/NameLib.py", "def kw():", " pass");
        createFile(project, "libs/OtherNameLib.py", "def kw():", " pass");

        // this should not be found in any case
        createFile(project, "notUsedLib.py", "def kw():", " pass");
        createFile(project, "notUsedTest.robot",
                "*** Settings ***",
                "Library  notUsedLib.py",
                "*** Test Cases ***");
    }

    @BeforeEach
    public void beforeTest() throws Exception {
        model = new RobotModel();
        robotProject = model.createRobotProject(project);
        configure(project);
    }

    @AfterEach
    public void afterTest() throws Exception {
        model = null;
        robotProject.clearConfiguration();
    }

    @Test
    public void nothingIsCollected_whenLibrariesAreNotImported() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite1.robot",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void nothingIsCollected_whenLibraryNameIsNotSpecified() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite2.robot",
                "*** Settings ***",
                "Library",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void nothingIsCollected_whenStandardLibrariesAreImported() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite3.robot",
                "*** Settings ***",
                "Library  BuiltIn",
                "Library  String",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void librariesImportedByNameAreCollected_whenNamesContainSpaces() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  R e m o t e  http://127.0.0.1:10000/  30",
                "Library  Name Lib",
                "Library  Unknown Lib",
                "Library  Operation System",
                "Library  C o l l e c t i o n s",
                "Library  R e m o t e  https://127.0.0.1:8270/",
                "*** Test Cases ***"));
        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(NOT_ADDED, "R e m o t e");
        final RobotDryRunLibraryImport libImport2 = createImport(NOT_ADDED, "Name Lib");
        final RobotDryRunLibraryImport libImport3 = createImport(NOT_ADDED, "Unknown Lib");
        final RobotDryRunLibraryImport libImport4 = createImport(NOT_ADDED, "Operation System");
        final RobotDryRunLibraryImport libImport5 = createImport(NOT_ADDED, "C o l l e c t i o n s");
        assertThat(collector.getLibraryImports())
                .has(onlyLibImports(libImport1, libImport2, libImport3, libImport4, libImport5));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(5);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite, suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport3, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport4, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport5, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportIsCollected_whenLibraryIsImportedByPath() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportWithMultipleImportersIsCollected_whenLibraryIsImportedByPath() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "A/suite.robot",
                "*** Settings ***",
                "Library  ../libs/PathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(createFile(project, "A/B/suite.robot",
                "*** Settings ***",
                "Library  ../../libs/PathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(createFile(project, "A/B/C/suite.robot",
                "*** Settings ***",
                "Library  ../../../libs/PathLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsAreCollected_whenLibrariesAreImportedByPath() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "Library  ./other/dir/OtherPathLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "OtherPathLib",
                getFile(project, "other/dir/OtherPathLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsWithMultipleImportersAreCollected_whenLibrariesAreImportedByPath()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "Library  ./other/dir/OtherPathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "A/suite.robot",
                "*** Settings ***",
                "Library  ../other/dir/OtherPathLib.py",
                "Library  ../module/",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(createFile(project, "A/B/suite.robot",
                "*** Settings ***",
                "Library  ../../libs/PathLib.py",
                "Library  ../../module/",
                "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(createFile(project, "A/B/C/suite.robot",
                "*** Settings ***",
                "Library  ../../../module/",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "OtherPathLib",
                getFile(project, "other/dir/OtherPathLib.py"));
        final RobotDryRunLibraryImport libImport3 = createImport(ADDED, "module",
                getFile(project, "module/__init__.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2, libImport3));
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
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportWithMultipleImportersIsCollected_whenLibraryIsImportedByName() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "A/suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(createFile(project, "A/B/suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(createFile(project, "A/B/C/suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsAreCollected_whenLibrariesAreImportedByName() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "Library  OtherNameLib",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "OtherNameLib",
                getFile(project, "libs/OtherNameLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsWithMultipleImportersAreCollected_whenLibrariesAreImportedByName()
            throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString()),
                SearchPath.create(project.getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "Library  OtherNameLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "A/suite.robot",
                "*** Settings ***",
                "Library  OtherNameLib",
                "Library  module",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(createFile(project, "A/B/suite.robot",
                "*** Settings ***",
                "Library  NameLib",
                "Library  module",
                "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(createFile(project, "A/B/C/suite.robot",
                "*** Settings ***",
                "Library  module",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "OtherNameLib",
                getFile(project, "libs/OtherNameLib.py"));
        final RobotDryRunLibraryImport libImport3 = createImport(ADDED, "module",
                getFile(project, "module/__init__.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2, libImport3));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(3);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1, suite3));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite1, suite2));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport3,
                newArrayList(suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportIsCollected_whenRemoteLibraryIsImportedWithoutArgument() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForSingleRemoteImport("suite.robot", "Library  Remote");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8270/RPC2/", URI.create("http://127.0.0.1:8270/RPC2/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportIsCollected_whenRemoteLibraryIsImportedWithTimeoutOnly() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForSingleRemoteImport("suite.robot", "Library  Remote  timeout=60");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8270/RPC2/", URI.create("http://127.0.0.1:8270/RPC2/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportIsCollected_whenRemoteLibraryIsImportedWithPositionalUri() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForSingleRemoteImport("suite.robot",
                "Library  Remote  http://127.0.0.1:9000/");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown("Remote http://127.0.0.1:9000/",
                URI.create("http://127.0.0.1:9000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportIsCollected_whenRemoteLibraryIsImportedWithNamedUri() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForSingleRemoteImport("suite.robot",
                "Library  Remote  uri=http://127.0.0.1:9000/");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown("Remote http://127.0.0.1:9000/",
                URI.create("http://127.0.0.1:9000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportWithMultipleImportersIsCollected_whenRemoteLibraryIsImportedWithPositionalUri()
            throws Exception {
        final RobotSuiteFile suite1 = createSuiteFileForSingleRemoteImport("suite.robot",
                "Library  Remote  http://127.0.0.1:9000/");
        final RobotSuiteFile suite2 = createSuiteFileForSingleRemoteImport("A/suite.robot",
                "Library  Remote  http://127.0.0.1:9000");
        final RobotSuiteFile suite3 = createSuiteFileForSingleRemoteImport("A/B/suite.robot",
                "Library  Remote  127.0.0.1:9000/");
        final RobotSuiteFile suite4 = createSuiteFileForSingleRemoteImport("A/B/C/suite.robot",
                "Library  Remote  https://127.0.0.1:9000");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown("Remote http://127.0.0.1:9000/",
                URI.create("http://127.0.0.1:9000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportWithMultipleImportersIsCollected_whenRemoteLibraryIsImportedWithNamedUri()
            throws Exception {
        final RobotSuiteFile suite1 = createSuiteFileForSingleRemoteImport(
                "suite.robot", "Library  Remote  uri=http://127.0.0.1:9000/");
        final RobotSuiteFile suite2 = createSuiteFileForSingleRemoteImport(
                "A/suite.robot", "Library  Remote  uri=http://127.0.0.1:9000");
        final RobotSuiteFile suite3 = createSuiteFileForSingleRemoteImport("A/B/suite.robot",
                "Library  Remote  uri=127.0.0.1:9000/");
        final RobotSuiteFile suite4 = createSuiteFileForSingleRemoteImport("A/B/C/suite.robot",
                "Library  Remote  uri=https://127.0.0.1:9000");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown("Remote http://127.0.0.1:9000/",
                URI.create("http://127.0.0.1:9000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportWithMultipleImportersIsCollected_whenRemoteLibraryIsImportedWithPositionalUriAndTimeout()
            throws Exception {
        final RobotSuiteFile suite1 = createSuiteFileForSingleRemoteImport("suite.robot",
                "Library  Remote  http://127.0.0.1:9000/  30");
        final RobotSuiteFile suite2 = createSuiteFileForSingleRemoteImport("A/suite.robot",
                "Library  Remote  http://127.0.0.1:9000  30");
        final RobotSuiteFile suite3 = createSuiteFileForSingleRemoteImport("A/B/suite.robot",
                "Library  Remote  127.0.0.1:9000/  30");
        final RobotSuiteFile suite4 = createSuiteFileForSingleRemoteImport("A/B/C/suite.robot",
                "Library  Remote  https://127.0.0.1:9000  30");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown("Remote http://127.0.0.1:9000/",
                URI.create("http://127.0.0.1:9000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportWithMultipleImportersIsCollected_whenRemoteLibraryIsImportedWithNamedUriAndTimed()
            throws Exception {
        final RobotSuiteFile suite1 = createSuiteFileForSingleRemoteImport("suite.robot",
                "Library  Remote  uri=http://127.0.0.1:9000/  timeout=30");
        final RobotSuiteFile suite2 = createSuiteFileForSingleRemoteImport("A/suite.robot",
                "Library  Remote  uri=http://127.0.0.1:9000  timeout=30");
        final RobotSuiteFile suite3 = createSuiteFileForSingleRemoteImport("A/B/suite.robot",
                "Library  Remote  uri=127.0.0.1:9000/  timeout=30");
        final RobotSuiteFile suite4 = createSuiteFileForSingleRemoteImport("A/B/C/suite.robot",
                "Library  Remote  uri=https://127.0.0.1:9000  timeout=30");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown("Remote http://127.0.0.1:9000/",
                URI.create("http://127.0.0.1:9000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportWithMultipleImportersIsCollected_whenRemoteLibraryIsImportedWithInvertedNamedUriAndTimed()
            throws Exception {
        final RobotSuiteFile suite1 = createSuiteFileForSingleRemoteImport("suite.robot",
                "Library  Remote  timeout=30  uri=http://127.0.0.1:9000/");
        final RobotSuiteFile suite2 = createSuiteFileForSingleRemoteImport("A/suite.robot",
                "Library  Remote  timeout=30  uri=http://127.0.0.1:9000");
        final RobotSuiteFile suite3 = createSuiteFileForSingleRemoteImport("A/B/suite.robot",
                "Library  Remote  timeout=30  uri=127.0.0.1:9000/");
        final RobotSuiteFile suite4 = createSuiteFileForSingleRemoteImport("A/B/C/suite.robot",
                "Library  Remote  timeout=30  uri=https://127.0.0.1:9000");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport.createKnown("Remote http://127.0.0.1:9000/",
                URI.create("http://127.0.0.1:9000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite1, suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportWithMultipleImportersIsCollected_whenRemoteLibraryWithoutArgumentsIsImported()
            throws Exception {
        final RobotSuiteFile suite1 = createSuiteFileForSingleRemoteImport("suite.robot", "Library  Remote");
        final RobotSuiteFile suite2 = createSuiteFileForSingleRemoteImport("A/suite.robot", "Library  Remote");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8270/RPC2/", URI.create("http://127.0.0.1:8270/RPC2/"));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite1, suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleRemoteLibraryImportWithMultipleImportersIsCollected_whenRemoteLibraryIsImportedWithTimeoutOnly()
            throws Exception {
        final RobotSuiteFile suite1 = createSuiteFileForSingleRemoteImport("suite.robot",
                "Library  Remote  timeout=30");
        final RobotSuiteFile suite2 = createSuiteFileForSingleRemoteImport("A/suite.robot",
                "Library  Remote  timeout=60");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8270/RPC2/", URI.create("http://127.0.0.1:8270/RPC2/"));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite1, suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleRemoteLibraryImportsAreCollected_whenRemoteLibrariesAreImportedWithPositionalUri()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForMultipleRemoteImport("suite.robot",
                "Library  Remote  http://127.0.0.1:9000/",
                "Library  Remote  127.0.0.1:8000");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:9000/", URI.create("http://127.0.0.1:9000/"));
        final RobotDryRunLibraryImport libImport2 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleRemoteLibraryImportsAreCollected_whenRemoteLibrariesAreImportedWithNamedUri() throws Exception {
        final RobotSuiteFile suite = createSuiteFileForMultipleRemoteImport("suite.robot",
                "Library  Remote  uri=http://127.0.0.1:9000/", "Library  Remote  uri=127.0.0.1:8000");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:9000/", URI.create("http://127.0.0.1:9000/"));
        final RobotDryRunLibraryImport libImport2 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleRemoteLibraryImportsAreCollected_whenRemoteLibrariesAreImportedWithPositionalUriAndTimeout()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForMultipleRemoteImport("suite.robot",
                "Library  Remote  http://127.0.0.1:9000/  30", "Library  Remote  127.0.0.1:8000  30");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:9000/", URI.create("http://127.0.0.1:9000/"));
        final RobotDryRunLibraryImport libImport2 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleRemoteLibraryImportsAreCollected_whenRemoteLibrariesAreImportedWithNamedUriAndTimeout()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForMultipleRemoteImport("suite.robot",
                "Library  Remote  uri=http://127.0.0.1:9000/  timeout=30",
                "Library  Remote  uri=127.0.0.1:8000  timeout=30");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:9000/", URI.create("http://127.0.0.1:9000/"));
        final RobotDryRunLibraryImport libImport2 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleRemoteLibraryImportsAreCollected_whenRemoteLibrariesAreImportedWithInvertedNamedUriAndTimeout()
            throws Exception {
        final RobotSuiteFile suite = createSuiteFileForMultipleRemoteImport("suite.robot",
                "Library  Remote  timeout=30  uri=http://127.0.0.1:9000/",
                "Library  Remote  timeout=30  uri=127.0.0.1:8000");

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:9000/", URI.create("http://127.0.0.1:9000/"));
        final RobotDryRunLibraryImport libImport2 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleRemoteLibraryImportsWithMultipleImportersAreCollected_whenRemoteLibrariesAreImportedWithPositionalUriAndTimeout()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:10000/  30",
                "Library  Remote  http://127.0.0.1:8000  30", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "A/suite.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:8000/  30",
                "Library  Remote  http://127.0.0.1:7000  30", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(createFile(project, "A/B/suite.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:10000  30",
                "Library  Remote  http://127.0.0.1:7000  30", "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(createFile(project, "A/B/C/suite.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:7000/  30", "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:10000/", URI.create("http://127.0.0.1:10000/"));
        final RobotDryRunLibraryImport libImport2 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"));
        final RobotDryRunLibraryImport libImport3 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:7000/", URI.create("http://127.0.0.1:7000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport2, libImport1, libImport3));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(3);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1, suite3));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite1, suite2));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport3,
                newArrayList(suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleRemoteLibraryImportsWithMultipleImportersAreCollected_whenRemoteLibrariesAreImportedWithNamedUriAndTimeout()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***", "Library  Remote  uri=http://127.0.0.1:10000/  timeout=30",
                "Library  Remote  uri=http://127.0.0.1:8000  timeout=30", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "A/suite.robot",
                "*** Settings ***", "Library  Remote  uri=http://127.0.0.1:8000/  timeout=30",
                "Library  Remote  uri=http://127.0.0.1:7000  timeout=30", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(createFile(project, "A/B/suite.robot",
                "*** Settings ***", "Library  Remote  uri=http://127.0.0.1:10000  timeout=30",
                "Library  Remote  uri=http://127.0.0.1:7000  timeout=30", "*** Test Cases ***"));
        final RobotSuiteFile suite4 = model.createSuiteFile(createFile(project, "A/B/C/suite.robot",
                "*** Settings ***", "Library  Remote  uri=http://127.0.0.1:7000/  timeout=30", "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:10000/", URI.create("http://127.0.0.1:10000/"));
        final RobotDryRunLibraryImport libImport2 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"));
        final RobotDryRunLibraryImport libImport3 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:7000/", URI.create("http://127.0.0.1:7000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport2, libImport1, libImport3));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(3);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1, suite3));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite1, suite2));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport3,
                newArrayList(suite2, suite3, suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleRemoteLibraryImportsWithMultipleImportersAreCollected_whenRemoteLibrariesAreImportedWithMixedArguments()
            throws Exception {
        final RobotSuiteFile suite1 = createSuiteFileForMultipleRemoteImport("suite.robot",
                "Library  Remote  http://127.0.0.1:10000/  30", "Library  Remote  http://127.0.0.1:8000  timeout=30");
        final RobotSuiteFile suite2 = createSuiteFileForMultipleRemoteImport("A/suite.robot",
                "Library  Remote  http://127.0.0.1:8000/", "Library  Remote  uri=http://127.0.0.1:7000  timeout=30");
        final RobotSuiteFile suite3 = createSuiteFileForMultipleRemoteImport("A/B/suite.robot",
                "Library  Remote  http://127.0.0.1:10000  timeout=30",
                "Library  Remote  timeout=30  uri=http://127.0.0.1:7000");
        final RobotSuiteFile suite4 = createSuiteFileForMultipleRemoteImport("A/B/C/suite.robot",
                "Library  Remote  uri=http://127.0.0.1:7000/  timeout=30", "Library  Remote  timeout=30");
        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2, suite3, suite4), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:10000/", URI.create("http://127.0.0.1:10000/"));
        final RobotDryRunLibraryImport libImport2 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"));
        final RobotDryRunLibraryImport libImport3 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:7000/", URI.create("http://127.0.0.1:7000/"));
        final RobotDryRunLibraryImport libImport4 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:8270/RPC2/", URI.create("http://127.0.0.1:8270/RPC2/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport2, libImport1, libImport3, libImport4));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(4);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1, suite3));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite1, suite2));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport3,
                newArrayList(suite2, suite3, suite4));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport4, newArrayList(suite4));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void remoteLibraryImportIsCollected_whenLibraryImportHasUnknownArguments() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  Remote  http://127.0.0.1:10000/  http://127.0.0.1:10001/  http://127.0.0.1:10002/",
                "*** Test Cases ***"));
        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(NOT_ADDED, "Remote");
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void remoteLibraryImportIsCollectedOnce_forSeveralImports() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  Remote  http://127.0.0.1:10000/  30",
                "Library  Remote  http://127.0.0.1:10000/  30",
                "Library  Remote  http://127.0.0.1:10000/  60",
                "Library  Remote  http://127.0.0.1:10000/  90",
                "*** Test Cases ***"));
        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:10000/", URI.create("http://127.0.0.1:10000/"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport,
                newArrayList(suite, suite, suite, suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void remoteLibraryImportIsCollected_togetherWithOtherImports() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  Remote  http://127.0.0.1:10000/  30",
                "Library  NameLib",
                "Library  ./libs/PathLib.py",
                "Library  UnknownLib",
                "Library  unknown_lib.py",
                "Library  Remote  http://127.0.0.1:10000/  60",
                "*** Test Cases ***"));
        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = RobotDryRunLibraryImport
                .createKnown("Remote http://127.0.0.1:10000/", URI.create("http://127.0.0.1:10000/"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        final RobotDryRunLibraryImport libImport3 = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport4 = createImport(NOT_ADDED, "unknown_lib.py");
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2, libImport3, libImport4));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(4);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite, suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport3, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport4, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(1);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(suite));
    }

    @Test
    public void singleLibraryNameIsCollected_whenUnknownLibraryIsImportedByName() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(1);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(suite));
    }

    @Test
    public void singleLibraryNameWithMultipleImportersIsCollected_whenUnknownLibraryIsImportedByName()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite2.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "suite1.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(1);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib",
                newArrayList(suite1, suite2));
    }

    @Test
    public void multipleLibraryNamesAreCollected_whenUnknownLibrariesAreImportedByName() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "Library  ErrorLib",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
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
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite1.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "Library  ErrorLib",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "suite2.robot",
                "*** Settings ***",
                "Library  UnknownLib",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        assertThat(collector.getLibraryImports()).isEmpty();
        assertThat(collector.getLibraryImporters().asMap()).isEmpty();
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(2);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib",
                newArrayList(suite1, suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("ErrorLib", newArrayList(suite1));
    }

    @Test
    public void singleLibraryImportIsCollected_whenLibraryIsImportedByUnknownPath() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(NOT_ADDED, "./libs/UnknownLib.py");
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void singleLibraryImportWithMultipleImportersIsCollected_whenLibraryIsImportedByUnknownPath()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite1.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "suite2.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(NOT_ADDED, "./libs/UnknownLib.py");
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite1, suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsAreCollected_whenLibrariesAreImportedByUnknownPath() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "Library  ../other/OtherUnknownLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(NOT_ADDED, "./libs/UnknownLib.py");
        final RobotDryRunLibraryImport libImport2 = createImport(NOT_ADDED, "../other/OtherUnknownLib.py");
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsWithMultipleImportersAreCollected_whenLibrariesAreImportedByUnknownPath()
            throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite1.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "suite2.robot",
                "*** Settings ***",
                "Library  ./libs/UnknownLib.py",
                "Library  ../other/OtherUnknownLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(NOT_ADDED, "./libs/UnknownLib.py");
        final RobotDryRunLibraryImport libImport2 = createImport(NOT_ADDED, "../other/OtherUnknownLib.py");
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1, suite2));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void multipleLibraryImportsAreCollected_whenLibrariesAreImportedByPathWithNotSupportedSeparator() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite_with_windows.robot",
                "*** Settings ***",
                "Library  C:\\folder\\path_lib.py",
                "Library  ${EXECDIR}\\OtherLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(NOT_ADDED, "C:\\folder\\path_lib.py");
        final RobotDryRunLibraryImport libImport2 = createImport(NOT_ADDED,
                robotProject.getRobotProjectHolder().getVariableMappings().get("${execdir}") + "\\OtherLib.py");
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void libraryImportsAreCollected_whenVariablesAreNotResolved() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite1.robot",
                "*** Settings ***",
                "Library  ./${unknown_path}/UnknownLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "suite2.robot",
                "*** Settings ***",
                "Library  ${unknown_name}",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(NOT_ADDED, "./${unknown_path}/UnknownLib.py");
        final RobotDryRunLibraryImport libImport2 = createImport(NOT_ADDED, "${unknown_name}");
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void libraryImportsAreCollected_whenVariablesAreResolved() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        config.setVariableMappings(newArrayList(VariableMapping.create("${known_path}", "libs"),
                VariableMapping.create("${known_name}", "NameLib")));
        configure(project, config);

        final RobotSuiteFile suite1 = model.createSuiteFile(createFile(project, "suite1.robot",
                "*** Settings ***",
                "Library  ./${known_path}/PathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(createFile(project, "suite2.robot",
                "*** Settings ***",
                "Library  ${known_name}",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite1, suite2), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite1));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite2));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    @Test
    public void libraryImportsAndUnknownLibraryNamesAreCollected_fromSuiteWithImportedResources() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ./libs/PathLib.py",
                "Resource  res1.robot",
                "*** Test Cases ***"));
        final RobotSuiteFile res1 = model.createSuiteFile(createFile(project, "res1.robot",
                "*** Settings ***",
                "Resource  ./A/B/res3.robot",
                "Resource  ./A/B/C/res4.robot",
                "Resource  unknown.robot",
                "Library  NameLib"));
        final RobotSuiteFile res2 = model.createSuiteFile(createFile(project, "A/res2.robot",
                "*** Settings ***",
                "Library  NameLib",
                "Library  UnknownLib"));
        final RobotSuiteFile res3 = model.createSuiteFile(createFile(project, "A/B/res3.robot",
                "*** Settings ***",
                "Resource  ../res2.robot",
                "Library  UnknownLib",
                "Library  ../../libs/PathLib.py"));
        final RobotSuiteFile res4 = model.createSuiteFile(createFile(project, "A/B/C/res4.robot",
                "*** Settings ***",
                "Resource  ../res3.robot",
                "Resource  ../../res2.robot",
                "Library  NameLib"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(res3, suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(res1, res4, res2));
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(1);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(res3, res2));
    }

    @Test
    public void libraryImportsAndUnknownLibraryNamesAreCollected_fromInitFile() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        createDir(project, "init_without_suite");
        final RobotSuiteFile init = model.createSuiteFile(createFile(project, "init_without_suite/__init__.robot",
                "*** Settings ***",
                "Library  ../libs/PathLib.py",
                "Library  NameLib",
                "Library  UnknownLib"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(init), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(init));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(init));
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(1);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(init));
    }

    @Test
    public void libraryImportsAndUnknownLibraryNamesAreCollected_fromSuiteWithInitFile() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(getDir(project, "libs").getLocation().toString())));
        configure(project, config);

        createDir(project, "suite_with_init");
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite_with_init/suite.robot",
                "*** Settings ***",
                "Library  ../libs/PathLib.py",
                "Library  NameLib",
                "Library  UnknownLib",
                "*** Test Cases ***"));
        model.createSuiteFile(createFile(project, "suite_with_init/__init__.robot",
                "*** Settings ***",
                "Library  ../other/dir/OtherPathLib.py.py",
                "Library  OtherNameLib",
                "Library  OtherUnknownLib"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport1 = createImport(ADDED, "PathLib",
                getFile(project, "libs/PathLib.py"));
        final RobotDryRunLibraryImport libImport2 = createImport(ADDED, "NameLib",
                getFile(project, "libs/NameLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport1, libImport2));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(2);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport1, newArrayList(suite));
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport2, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).hasSize(1);
        assertThat(collector.getUnknownLibraryNames().asMap()).containsEntry("UnknownLib", newArrayList(suite));
    }

    @Test
    public void erroneousLibraryImportByPath_hasCorrectSource() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ./libs/ErrorLib.py",
                "*** Test Cases ***"));

        final ExternalLibrariesImportCollector collector = new ExternalLibrariesImportCollector(robotProject);
        collector.collectFromSuites(newArrayList(suite), new NullProgressMonitor());

        final RobotDryRunLibraryImport libImport = createImport(NOT_ADDED, "./libs/ErrorLib.py",
                getFile(project, "libs/ErrorLib.py"));
        assertThat(collector.getLibraryImports()).has(onlyLibImports(libImport));
        assertThat(collector.getLibraryImporters().asMap()).hasSize(1);
        assertThat(collector.getLibraryImporters().asMap()).containsEntry(libImport, newArrayList(suite));
        assertThat(collector.getUnknownLibraryNames().asMap()).isEmpty();
    }

    private RobotSuiteFile createSuiteFileForSingleRemoteImport(final String suiteNameOrPath,
            final String librarySetting) throws Exception {
        return model.createSuiteFile(
                createFile(project, suiteNameOrPath, "*** Settings ***", librarySetting, "*** Test Cases ***"));
    }

    private RobotSuiteFile createSuiteFileForMultipleRemoteImport(final String suiteNameOrPath,
            final String librarySetting1, final String librarySetting2) throws Exception {
        return model.createSuiteFile(createFile(project, suiteNameOrPath, "*** Settings ***",
                librarySetting1, librarySetting2, "*** Test Cases ***"));
    }
}
