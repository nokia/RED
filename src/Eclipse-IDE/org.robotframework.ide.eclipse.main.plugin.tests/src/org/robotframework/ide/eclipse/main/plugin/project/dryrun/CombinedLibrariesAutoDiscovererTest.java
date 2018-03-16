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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.ADDED;
import static org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.ALREADY_EXISTING;
import static org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.NOT_ADDED;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.createImport;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.hasLibImports;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import org.eclipse.core.resources.IFile;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.RemoteLocation;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

@RunWith(MockitoJUnitRunner.class)
public class CombinedLibrariesAutoDiscovererTest {

    private static final String PROJECT_NAME = CombinedLibrariesAutoDiscovererTest.class.getSimpleName();

    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    @Mock
    private Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler;

    private RobotModel model;

    private RobotProject robotProject;

    @BeforeClass
    public static void beforeClass() throws Exception {
        projectProvider.createDir("libs");
        projectProvider.createDir("module");
        projectProvider.createDir("other");
        projectProvider.createDir("other/dir");

        projectProvider.createFile("libs/SomePathLib.py", "def kw():", " pass");
        projectProvider.createFile("other/dir/OtherPathLib.py", "def kw():", " pass");
        projectProvider.createFile("module/__init__.py", "class module(object):", "  def kw():", "   pass");
        projectProvider.createFile("libs/ErrorLib.py", "error():");
        projectProvider.createFile("libs/LibWithClasses.py", "class ClassA(object):", "  def kw():", "   pass",
                "class ClassB(object):", "  def kw():", "   pass", "class ClassC(object):", "  def kw():", "   pass");

        // this should not be found in any case
        projectProvider.createFile("notUsedLib.py", "def kw():", " pass");
        projectProvider.createFile("notUsedTest.robot",
                "*** Settings ***",
                "Library  notUsedLib.py",
                "*** Test Cases ***");
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
    public void libsAreAddedToProjectConfig_whenExistAndAreCorrect() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***",
                "Library  ./libs/SomePathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library  ./other/dir/OtherPathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("suite3.robot",
                "*** Settings ***",
                "Library  module",
                "Library  NotExisting.py",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "module", PROJECT_NAME + "/module"),
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib", PROJECT_NAME + "/other/dir"),
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", projectProvider.getFile("libs/SomePathLib.py"),
                        newHashSet(suite1.getFile())),
                createImport(ADDED, "OtherPathLib", projectProvider.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite2.getFile())),
                createImport(ADDED, "module", projectProvider.getFile("module/__init__.py"),
                        newHashSet(suite3.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forRobotResourceFile() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("resource.robot",
                "*** Settings ***",
                "Library  ./libs/SomePathLib.py",
                "Library  NotExisting.py"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries())
                .containsExactly(ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", projectProvider.getFile("libs/SomePathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenVariableMappingIsUsedInLibraryImport() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setVariableMappings(newArrayList(VariableMapping.create("${ABC}", "other"),
                VariableMapping.create("${XYZ}", "${ABC}/dir")));
        projectProvider.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ${var}/SomePathLib.py",
                "Library  ${xyz}/OtherPathLib.py",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib", PROJECT_NAME + "/other/dir"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "OtherPathLib", projectProvider.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "${var}/SomePathLib.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forSuitesInNestedDirectory() throws Exception {
        projectProvider.createDir("A");
        projectProvider.createDir("A/B");
        projectProvider.createDir("A/B/C");
        projectProvider.createDir("A/B/C/D");
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("A/B/C/D/suite1.robot",
                "*** Settings ***",
                "Library  ../../../../libs/SomePathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("A/B/C/D/suite2.robot",
                "*** Settings ***",
                "Library  ../../../../other/dir/OtherPathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("A/B/C/D/suite3.robot",
                "*** Settings ***",
                "Library  NotExisting.py",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib", PROJECT_NAME + "/other/dir"),
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", projectProvider.getFile("libs/SomePathLib.py"),
                        newHashSet(suite1.getFile())),
                createImport(ADDED, "OtherPathLib", projectProvider.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite2.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forResourceAndSuite() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ./libs/SomePathLib.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("resource.robot",
                "*** Settings ***",
                "Library  ./other/dir/OtherPathLib.py",
                "Library  NotExisting.py"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib", PROJECT_NAME + "/other/dir"),
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", projectProvider.getFile("libs/SomePathLib.py"),
                        newHashSet(suite1.getFile())),
                createImport(ADDED, "OtherPathLib", projectProvider.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite2.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite2.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forLinkedSuite() throws Exception {
        final File root = tempFolder.getRoot();
        getFile(root, "external_dir").mkdir();
        getFile(root, "external_dir", "external_nested.robot").createNewFile();
        final File tmpFile = getFile(tempFolder.getRoot(), "external_dir", "external_nested.robot");

        final IFile linkedFile = projectProvider.getFile("linked_suite.robot");
        resourceCreator.createLink(tmpFile.toURI(), linkedFile);

        final String libPath = projectProvider.getFile("libs/SomePathLib.py").getLocation().toPortableString();
        projectProvider.createFile(linkedFile,
                "*** Settings ***",
                "Library  " + libPath,
                "*** Test Cases ***");

        final RobotSuiteFile suite = model.createSuiteFile(linkedFile);

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries())
                .containsExactly(ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(createImport(ADDED, "SomePathLib",
                projectProvider.getFile("libs/SomePathLib.py"), newHashSet(linkedFile)))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenImportedFromSeveralSuites() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***",
                "Library  ./libs/SomePathLib.py",
                "Library  module",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library  ./libs/SomePathLib.py",
                "Library  ./other/dir/OtherPathLib.py",
                "Library  NotExisting.py",
                "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("suite3.robot",
                "*** Settings ***",
                "Library  ./libs/SomePathLib.py",
                "Library  module",
                "Library  NotExisting.py",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "module", PROJECT_NAME + "/module"),
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib", PROJECT_NAME + "/other/dir"),
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "SomePathLib", projectProvider.getFile("libs/SomePathLib.py"),
                        newHashSet(suite1.getFile(), suite2.getFile(), suite3.getFile())),
                createImport(ADDED, "OtherPathLib", projectProvider.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite2.getFile())),
                createImport(ADDED, "module", projectProvider.getFile("module/__init__.py"),
                        newHashSet(suite1.getFile(), suite3.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite2.getFile(), suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenQualifiedNamesAreUsed() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPath(newArrayList(SearchPath.create(projectProvider.getDir("libs").getLocation().toString())));
        projectProvider.configure(config);

        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***",
                "Library  LibWithClasses.ClassA",
                "Library  LibWithClasses.ClassC",
                "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***",
                "Library  LibWithClasses.ClassA",
                "Library  LibWithClasses.ClassB",
                "Library  NotExisting.ClassName",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "LibWithClasses.ClassA", PROJECT_NAME + "/libs"),
                ReferencedLibrary.create(LibraryType.PYTHON, "LibWithClasses.ClassB", PROJECT_NAME + "/libs"),
                ReferencedLibrary.create(LibraryType.PYTHON, "LibWithClasses.ClassC", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "LibWithClasses.ClassA", projectProvider.getFile("libs/LibWithClasses.py"),
                        newHashSet(suite1.getFile(), suite2.getFile())),
                createImport(ADDED, "LibWithClasses.ClassB", projectProvider.getFile("libs/LibWithClasses.py"),
                        newHashSet(suite2.getFile())),
                createImport(ADDED, "LibWithClasses.ClassC", projectProvider.getFile("libs/LibWithClasses.py"),
                        newHashSet(suite1.getFile())),
                createImport(NOT_ADDED, "NotExisting.ClassName", newHashSet(suite2.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void remoteLibsAreAddedToProjectConfig_whenAddressIsWithOrWithoutSlash() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite1.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:9000", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("suite2.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:8000", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("suite3.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:9000/",
                "Library  Remote  http://127.0.0.1:8000/",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite1, suite2, suite3), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getRemoteLocations()).containsExactly(
                RemoteLocation.create("http://127.0.0.1:8000/"), RemoteLocation.create("http://127.0.0.1:9000/"));

        verify(summaryHandler)
                .accept(argThat(hasLibImports(
                        createImport(ADDED, "Remote http://127.0.0.1:9000/", URI.create("http://127.0.0.1:9000/"),
                                newHashSet(suite1.getFile(), suite3.getFile())),
                        createImport(ADDED, "Remote http://127.0.0.1:8000/", URI.create("http://127.0.0.1:8000/"),
                                newHashSet(suite2.getFile(), suite3.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void remoteLibsAreAddedToProjectConfig_forSuitesInNestedDirectory() throws Exception {
        projectProvider.createDir("E");
        projectProvider.createDir("E/F");
        projectProvider.createDir("E/F/G");
        projectProvider.createDir("E/F/G/H");
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("E/suite1.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:9000", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("E/F/G/suite2.robot",
                "*** Settings ***", "Library  Remote  http://127.0.0.1:8000", "*** Test Cases ***"));
        final RobotSuiteFile suite3 = model.createSuiteFile(projectProvider.createFile("E/F/G/H/suite3.robot",
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
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  NotExisting.py",
                "Library  not_existing/",
                "Library  SomePathLib.py",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();

        verify(summaryHandler)
                .accept(argThat(hasLibImports(createImport(NOT_ADDED, "SomePathLib.py", newHashSet(suite.getFile())),
                        createImport(NOT_ADDED, "not_existing/", newHashSet(suite.getFile())),
                        createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenImportedLibraryIsAlreadyAdded() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));
        projectProvider.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ./libs/SomePathLib.py",
                "Library  NotExisting.py",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries())
                .containsExactly(ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ALREADY_EXISTING, "SomePathLib", projectProvider.getFile("libs/SomePathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void dryRunDiscoveringIsRun_whenSomeLibrariesImportedByNameAreNotDiscovered() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  SomePathLib",
                "Library  module",
                "Library  ./other/dir/OtherPathLib.py",
                "Library  ErrorLib",
                "Library  NotExisting.py",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        final CombinedLibrariesAutoDiscoverer discovererSpy = spy(discoverer);
        discovererSpy.start().join();

        verify(discovererSpy).startDryRunDiscovering(any(), eq(newHashSet("ErrorLib", "SomePathLib")));

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "module", PROJECT_NAME + "/module"),
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib", PROJECT_NAME + "/other/dir"),
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "module", projectProvider.getFile("module/__init__.py"),
                        newHashSet(suite.getFile())),
                createImport(ADDED, "OtherPathLib", projectProvider.getFile("other/dir/OtherPathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(ADDED, "SomePathLib", projectProvider.getFile("libs/SomePathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "ErrorLib", newHashSet(suite.getFile())),
                createImport(NOT_ADDED, "NotExisting.py", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void dryRunDiscoveringIsNotRun_whenAllLibrariesImportedByNameAreDiscovered() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.setPythonPath(newArrayList(SearchPath.create(projectProvider.getDir("libs").getLocation().toString())));
        projectProvider.configure(config);

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  module",
                "Library  SomePathLib",
                "Library  ./other/dir/OtherPathLib.py",
                "Library  NotExisting.py",
                "*** Test Cases ***"));

        final CombinedLibrariesAutoDiscoverer discoverer = new CombinedLibrariesAutoDiscoverer(robotProject,
                newArrayList(suite), summaryHandler);
        final CombinedLibrariesAutoDiscoverer discovererSpy = spy(discoverer);
        discovererSpy.start().join();

        verify(discovererSpy, times(0)).startDryRunDiscovering(any(), anySet());

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "module", PROJECT_NAME + "/module"),
                ReferencedLibrary.create(LibraryType.PYTHON, "OtherPathLib", PROJECT_NAME + "/other/dir"),
                ReferencedLibrary.create(LibraryType.PYTHON, "SomePathLib", PROJECT_NAME + "/libs"));

        verify(summaryHandler).accept(argThat(hasLibImports(
                createImport(ADDED, "module", projectProvider.getFile("module/__init__.py"),
                        newHashSet(suite.getFile())),
                createImport(ADDED, "SomePathLib", projectProvider.getFile("libs/SomePathLib.py"),
                        newHashSet(suite.getFile())),
                createImport(ADDED, "OtherPathLib", projectProvider.getFile("other/dir/OtherPathLib.py"),
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
}
