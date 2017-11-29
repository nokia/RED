/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class LibrariesAutoDiscovererTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibrariesAutoDiscovererTest.class);

    private static RobotModel model = new RobotModel();

    private RobotProject robotProject;

    @BeforeClass
    public static void beforeClass() throws Exception {
        projectProvider.createDir("libs");
        projectProvider.createFile("libs/TestLib.py", "def kw():", " pass");

        projectProvider.createDir("other");
        projectProvider.createDir("other/dir");
        projectProvider.createFile("other/dir/OtherLib.py", "def other_kw():", " pass");
        projectProvider.createFile("other/ErrorLib.py", "error():");

        projectProvider.createDir("module");
        projectProvider.createFile("module/__init__.py", "class module(object):", "  def mod_kw():", "   pass");

        // this should not be found in any cases
        projectProvider.createFile("mainLib.py", "def main_kw():", " pass");
        projectProvider.createFile("mainTest.robot", "*** Settings ***", "Library  mainLib.py", "*** Test Cases ***",
                "  case 1");
    }

    @AfterClass
    public static void afterClass() {
        model = null;
    }

    @Before
    public void before() throws Exception {
        robotProject = model.createRobotProject(projectProvider.getProject());
        projectProvider.configure();
    }

    @After
    public void after() throws Exception {
        robotProject.clearConfiguration();
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistAndAreCorrect() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***", "Library  ./libs/TestLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***", "Library  ./other/dir/OtherLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("suite3.robot",
                "*** Settings ***", "Library  module", "Library  ./libs/NotExisting.py", "*** Test Cases ***"));

        final ReferencedLibrary lib1 = createLibrary("TestLib", "libs/TestLib.py");
        final ReferencedLibrary lib2 = createLibrary("OtherLib", "other/dir/OtherLib.py");
        final ReferencedLibrary lib3 = createLibrary("module", "module/__init__.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite1, suite2, suite3), false).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib1, lib2, lib3);
    }

    @Test
    public void libsAreAddedToProjectConfig_forRobotResourceFile() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("resource.robot",
                "*** Settings ***", "Library  ./libs/TestLib.py", "Library  ./libs/NotExisting.py"));

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), false).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenIncorrectRelativePathIsUsedInLibraryImport() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  TestLib.py", "*** Test Cases ***"));

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), false).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenVariableMappingIsUsedInLibraryImport() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  ${var}/TestLib.py", "Library  ${xyz}/OtherLib.py", "*** Test Cases ***"));

        final ReferencedLibrary lib = createLibrary("OtherLib", "other/dir/OtherLib.py");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(newArrayList(VariableMapping.create("${ABC}", "other"),
                VariableMapping.create("${XYZ}", "${ABC}/dir")));
        projectProvider.configure(config);

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), false).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistingLibIsFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(
                projectProvider.createFile("suite.robot", "*** Settings ***", "Library  other/dir/OtherLib.py",
                        "Library  ./libs/TestLib.py", "Library  ./libs/NotExisting.py", "*** Test Cases ***"));

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), false, "TestLib").start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);
    }

    @Test
    public void libsAreAddedToProjectConfig_forSuitesInNestedDirectory() throws Exception {
        projectProvider.createDir("A");
        projectProvider.createDir("A/B");
        projectProvider.createDir("A/B/C");
        projectProvider.createDir("A/B/C/D");
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("A/B/C/D/suite1.robot",
                "*** Settings ***", "Library  ../../../../libs/TestLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("A/B/C/D/suite2.robot",
                "*** Settings ***", "Library  ../../../../other/dir/OtherLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("A/B/C/D/suite3.robot",
                "*** Settings ***", "Library  ../../../../libs/NotExisting.py", "*** Test Cases ***"));

        final ReferencedLibrary lib1 = createLibrary("TestLib", "libs/TestLib.py");
        final ReferencedLibrary lib2 = createLibrary("OtherLib", "other/dir/OtherLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite1, suite2, suite3), false).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib1, lib2);
    }

    @Test
    public void libsAreAddedToProjectConfig_forResourceAndSuite() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***", "Library  ./libs/TestLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("resource.robot",
                "*** Settings ***", "Library  ./other/dir/OtherLib.py", "Library  ./libs/NotExisting.py"));

        final ReferencedLibrary lib1 = createLibrary("TestLib", "libs/TestLib.py");
        final ReferencedLibrary lib2 = createLibrary("OtherLib", "other/dir/OtherLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite1, suite2), false).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib1, lib2);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenNoLibrariesAreFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  NotExisting.py", "*** Test Cases ***"));

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), false).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenNotExistingLibIsNotFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  ./libs/TestLib.py", "*** Test Cases ***"));

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), false, "NotExistingLib").start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenImportedLibraryIsAlreadyAdded() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  ./libs/TestLib.py", "*** Test Cases ***"));

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);
        projectProvider.configure(config);

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), false).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);
    }

    private ReferencedLibrary createLibrary(final String name, final String filePath)
            throws IOException, CoreException {
        return ReferencedLibrary.create(LibraryType.PYTHON, name,
                projectProvider.getFile(filePath)
                        .getFullPath()
                        .makeRelative()
                        .removeLastSegments(1)
                        .toPortableString());
    }
}
