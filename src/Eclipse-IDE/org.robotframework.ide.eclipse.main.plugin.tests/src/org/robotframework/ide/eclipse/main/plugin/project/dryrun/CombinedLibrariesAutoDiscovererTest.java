/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.ADDED;
import static org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.ALREADY_EXISTING;
import static org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.NOT_ADDED;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.createImport;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.hasLibImports;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import org.assertj.core.api.Condition;
import org.eclipse.core.resources.IFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;
import org.robotframework.red.junit.jupiter.StatefulProject;
import org.robotframework.red.junit.jupiter.StatefulProject.CleanMode;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class })
public class CombinedLibrariesAutoDiscovererTest {

    @Project(dirs = { "libs", "module", "other", "other/dir" }, cleanUpAfterEach = true)
    static StatefulProject project;

    @TempDir
    static File tempFolder;

    private Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler;

    private RobotModel model;

    private RobotProject robotProject;

    @BeforeAll
    public static void beforeClass() throws Exception {
        project.createFile(CleanMode.NONTEMPORAL, "libs/SomePathLib.py", "def kw():", " pass");
        project.createFile(CleanMode.NONTEMPORAL, "other/dir/OtherPathLib.py", "def kw():", " pass");
        project.createFile(CleanMode.NONTEMPORAL, "module/__init__.py", "class module(object):", "  def kw():",
                "   pass");
        project.createFile(CleanMode.NONTEMPORAL, "libs/ErrorLib.py", "error():");
        project.createFile(CleanMode.NONTEMPORAL, "libs/LibWithClasses.py", "class ClassA(object):", "  def kw():",
                "   pass", "class ClassB(object):", "  def kw():", "   pass", "class ClassC(object):", "  def kw():",
                "   pass");

        // this should not be found in any case
        project.createFile(CleanMode.NONTEMPORAL, "notUsedLib.py", "def kw():", " pass");
        project.createFile(CleanMode.NONTEMPORAL, "notUsedTest.robot", "*** Settings ***", "Library  notUsedLib.py",
                "*** Test Cases ***");
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void before() throws Exception {
        summaryHandler = mock(Consumer.class);
        model = new RobotModel();
        project.configure();
        robotProject = model.createRobotProject(project.getProject());
    }

    @AfterEach
    public void after() throws Exception {
        model = null;
        robotProject.clearConfiguration();
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistAndAreCorrect() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(project.createFile("suite1.robot",
                "*** Settings ***", "Library  ./libs/SomePathLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(project.createFile("suite2.robot",
                "*** Settings ***", "Library  ./other/dir/OtherPathLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(project.createFile("suite3.robot",
                "*** Settings ***", "Library  module", "Library  NotExisting.py", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(3);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "module", project.getName() + "/module/__init__.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(1))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib",
                        project.getName() + "/other/dir/OtherPathLib.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(2)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", project.getFile("libs/SomePathLib.py"),
                        newHashSet(suite1.getFile())),
                createImport(ADDED, "OtherPathLib", project.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite2.getFile())),
                createImport(ADDED, "module", project.getFile("module/__init__.py"),
                        newHashSet(suite3.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forRobotResourceFile() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(project.createFile("resource.robot",
                "*** Settings ***", "Library  ./libs/SomePathLib.py", "Library  NotExisting.py"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(1);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", project.getFile("libs/SomePathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenVariableMappingIsUsedInLibraryImport() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(newArrayList(VariableMapping.create("${ABC}", "other"),
                VariableMapping.create("${XYZ}", "${ABC}/dir")));
        project.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(project.createFile("suite.robot", "*** Settings ***",
                "Library  ${var}/SomePathLib.py", "Library  ${xyz}/OtherPathLib.py", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(1);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib",
                        project.getName() + "/other/dir/OtherPathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "OtherPathLib", project.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "${var}/SomePathLib.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forSuitesInNestedDirectory() throws Exception {
        project.createDir("A");
        project.createDir("A/B");
        project.createDir("A/B/C");
        project.createDir("A/B/C/D");
        final RobotSuiteFile suite1 = model.createSuiteFile(project.createFile("A/B/C/D/suite1.robot",
                "*** Settings ***", "Library  ../../../../libs/SomePathLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(project.createFile("A/B/C/D/suite2.robot",
                "*** Settings ***", "Library  ../../../../other/dir/OtherPathLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(project.createFile("A/B/C/D/suite3.robot",
                "*** Settings ***", "Library  NotExisting.py", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(2);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib",
                        project.getName() + "/other/dir/OtherPathLib.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(1)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", project.getFile("libs/SomePathLib.py"),
                        newHashSet(suite1.getFile())),
                createImport(ADDED, "OtherPathLib", project.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite2.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forResourceAndSuite() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(project.createFile("suite.robot",
                "*** Settings ***", "Library  ./libs/SomePathLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(project.createFile("resource.robot",
                "*** Settings ***", "Library  ./other/dir/OtherPathLib.py", "Library  NotExisting.py"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(2);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib",
                        project.getName() + "/other/dir/OtherPathLib.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(1)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", project.getFile("libs/SomePathLib.py"),
                        newHashSet(suite1.getFile())),
                createImport(ADDED, "OtherPathLib", project.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite2.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite2.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forLinkedSuite() throws Exception {
        getFile(tempFolder, "external_dir").mkdir();
        getFile(tempFolder, "external_dir", "external_nested.robot").createNewFile();
        final File tmpFile = getFile(tempFolder, "external_dir", "external_nested.robot");

        final IFile linkedFile = project.getFile("linked_suite.robot");
        project.createFileLink("linked_suite.robot", tmpFile.toURI());

        final String libPath = project.getFile("libs/SomePathLib.py").getLocation().toPortableString();
        project.createFile("linked_suite.robot", "*** Settings ***", "Library  " + libPath, "*** Test Cases ***");

        final RobotSuiteFile suite = model.createSuiteFile(linkedFile);

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(1);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(createImport(ADDED, "SomePathLib",
                project.getFile("libs/SomePathLib.py"), newHashSet(linkedFile)))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenImportedFromSeveralSuites() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(project.createFile("suite1.robot",
                "*** Settings ***", "Library  ./libs/SomePathLib.py", "Library  module", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(
                project.createFile("suite2.robot", "*** Settings ***", "Library  ./libs/SomePathLib.py",
                        "Library  ./other/dir/OtherPathLib.py", "Library  NotExisting.py", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(
                project.createFile("suite3.robot", "*** Settings ***", "Library  ./libs/SomePathLib.py",
                        "Library  module", "Library  NotExisting.py", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(3);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "module", project.getName() + "/module/__init__.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(1))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib",
                        project.getName() + "/other/dir/OtherPathLib.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(2)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", project.getFile("libs/SomePathLib.py"),
                        newHashSet(suite1.getFile(), suite2.getFile(), suite3.getFile())),
                createImport(ADDED, "OtherPathLib", project.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite2.getFile())),
                createImport(ADDED, "module", project.getFile("module/__init__.py"),
                        newHashSet(suite1.getFile(), suite3.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite2.getFile(), suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenQualifiedNamesAreUsed() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(project.getDir("libs").getLocation().toString())));
        project.configure(config);

        final RobotSuiteFile suite1 = model
                .createSuiteFile(project.createFile("suite1.robot", "*** Settings ***",
                        "Library  LibWithClasses.ClassA", "Library  LibWithClasses.ClassC", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(
                project.createFile("suite2.robot", "*** Settings ***", "Library  LibWithClasses.ClassA",
                        "Library  LibWithClasses.ClassB", "Library  NotExisting.ClassName", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(3);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "LibWithClasses.ClassA",
                        project.getName() + "/libs/LibWithClasses.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(1))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "LibWithClasses.ClassB",
                        project.getName() + "/libs/LibWithClasses.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(2))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "LibWithClasses.ClassC",
                        project.getName() + "/libs/LibWithClasses.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "LibWithClasses.ClassA", project.getFile("libs/LibWithClasses.py"),
                        newHashSet(suite1.getFile(), suite2.getFile())),
                createImport(ADDED, "LibWithClasses.ClassB", project.getFile("libs/LibWithClasses.py"),
                        newHashSet(suite2.getFile())),
                createImport(ADDED, "LibWithClasses.ClassC", project.getFile("libs/LibWithClasses.py"),
                        newHashSet(suite1.getFile())),
                createImport(NOT_ADDED, "NotExisting.ClassName", newHashSet(suite2.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void remoteLibsAreAddedToProjectConfig_whenAddressIsWithOrWithoutSlash() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(project.createFile("suite1.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:9000", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(project.createFile("suite2.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:8000", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(project.createFile("suite3.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:9000/",
                "Library  Remote  http://127.0.0.1:8000/", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getRemoteLocations()).containsExactly(
                RemoteLocation.create("http://127.0.0.1:8000/"), RemoteLocation.create("http://127.0.0.1:9000/"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "Remote http://127.0.0.1:9000/", URI.create("http://127.0.0.1:9000/"),
                        newHashSet(suite1.getFile(), suite3.getFile())),
                createImport(ADDED, "Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"),
                        newHashSet(suite2.getFile(), suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void remoteLibsAreAddedToProjectConfig_forSuitesInNestedDirectory() throws Exception {
        project.createDir("E");
        project.createDir("E/F");
        project.createDir("E/F/G");
        project.createDir("E/F/G/H");
        final RobotSuiteFile suite1 = model.createSuiteFile(project.createFile("E/suite1.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:9000", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(project.createFile("E/F/G/suite2.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:8000", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(project.createFile("E/F/G/H/suite3.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:9000/",
                "Library  Remote  http://127.0.0.1:8000/", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getRemoteLocations()).containsExactly(
                RemoteLocation.create("http://127.0.0.1:8000/"), RemoteLocation.create("http://127.0.0.1:9000/"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "Remote http://127.0.0.1:9000/", URI.create("http://127.0.0.1:9000/"),
                        newHashSet(suite1.getFile(), suite3.getFile())),
                createImport(ADDED, "Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"),
                        newHashSet(suite2.getFile(), suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenNoLibrariesAreFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(project.createFile("suite.robot", "*** Settings ***",
                "Library  NotExisting.py", "Library  not_existing/", "Library  SomePathLib.py", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEmpty();

        verify(summaryHandler)
                .accept(argThat(hasLibImports(createImport(NOT_ADDED, "SomePathLib.py", newHashSet(suite.getFile())),
                        createImport(NOT_ADDED, "not_existing/", newHashSet(suite.getFile())),
                        createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenImportedLibraryIsAlreadyAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py"));
        project.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(project.createFile("suite.robot", "*** Settings ***",
                "Library  ./libs/SomePathLib.py", "Library  NotExisting.py", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(1);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ALREADY_EXISTING, "SomePathLib", project.getFile("libs/SomePathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void dryRunDiscoveringIsRun_whenSomeLibrariesImportedByNameAreNotDiscovered() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(project.createFile("suite.robot", "*** Settings ***",
                "Library  SomePathLib", "Library  module", "Library  ./other/dir/OtherPathLib.py", "Library  ErrorLib",
                "Library  NotExisting.py", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        final CombinedLibrariesAutoDiscoverer discovererSpy = spy(discoverer);
        discovererSpy.start().join();

        verify(discovererSpy).startDryRunDiscovering(any(), eq(newHashSet("ErrorLib", "SomePathLib")));

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(3);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "module", project.getName() + "/module/__init__.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(1))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib",
                        project.getName() + "/other/dir/OtherPathLib.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(2)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "module", project.getFile("module/__init__.py"),
                        newHashSet(suite.getFile())),
                createImport(ADDED, "OtherPathLib", project.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(ADDED, "SomePathLib", project.getFile("libs/SomePathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "ErrorLib", newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void dryRunDiscoveringIsNotRun_whenAllLibrariesImportedByNameAreDiscovered() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPaths(newArrayList(SearchPath.create(project.getDir("libs").getLocation().toString())));
        project.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(
                project.createFile("suite.robot", "*** Settings ***", "Library  module", "Library  SomePathLib",
                        "Library  ./other/dir/OtherPathLib.py", "Library  NotExisting.py", "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        final CombinedLibrariesAutoDiscoverer discovererSpy = spy(discoverer);
        discovererSpy.start().join();

        verify(discovererSpy, times(0)).startDryRunDiscovering(any(), anySet());

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(3);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "module", project.getName() + "/module/__init__.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(1))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib",
                        project.getName() + "/other/dir/OtherPathLib.py")));
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(2)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib",
                        project.getName() + "/libs/SomePathLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "module", project.getFile("module/__init__.py"),
                        newHashSet(suite.getFile())),
                createImport(ADDED, "SomePathLib", project.getFile("libs/SomePathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(ADDED, "OtherPathLib", project.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    private static File getFile(final File root, final String... path) {
        if (path == null || path.length == 0) {
            return root;
        } else {
            return getFile(new File(root, path[0]), Arrays.copyOfRange(path, 1, path.length));
        }
    }

    private static Condition<? super ReferencedLibrary> sameFieldsAs(final ReferencedLibrary library) {
        return new Condition<ReferencedLibrary>() {

            @Override
            public boolean matches(final ReferencedLibrary toMatch) {
                return Objects.equals(library.getType(), toMatch.getType())
                        && Objects.equals(library.getName(), toMatch.getName())
                        && Objects.equals(library.getPath(), toMatch.getPath());
            }
        };
    }

}
