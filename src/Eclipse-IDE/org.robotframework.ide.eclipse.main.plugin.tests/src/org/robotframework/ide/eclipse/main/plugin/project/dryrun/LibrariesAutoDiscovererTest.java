/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.VariableMapping;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ResourceCreator;

import com.google.common.collect.ImmutableMap;

@RunWith(MockitoJUnitRunner.class)
public class LibrariesAutoDiscovererTest {

    public static ProjectProvider projectProvider = new ProjectProvider(LibrariesAutoDiscovererTest.class);

    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @ClassRule
    public static TestRule rulesChain = RuleChain.outerRule(projectProvider).around(tempFolder);

    @Rule
    public ResourceCreator resourceCreator = new ResourceCreator();

    @Mock
    private Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler;

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
                "*** Settings ***", "Library  module", "Library  NotExisting.py", "*** Test Cases ***"));

        final ReferencedLibrary lib1 = createLibrary("TestLib", "libs/TestLib.py");
        final ReferencedLibrary lib2 = createLibrary("OtherLib", "other/dir/OtherLib.py");
        final ReferencedLibrary lib3 = createLibrary("module", "module/__init__.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite1, suite2, suite3), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib1, lib2, lib3);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet("TestLib", "OtherLib", "module"),
                DryRunLibraryImportStatus.ALREADY_EXISTING, newHashSet(), DryRunLibraryImportStatus.NOT_ADDED,
                newHashSet("NotExisting.py"));
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forRobotResourceFile() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("resource.robot",
                "*** Settings ***", "Library  ./libs/TestLib.py", "Library  NotExisting.py"));

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet("TestLib"), DryRunLibraryImportStatus.ALREADY_EXISTING,
                newHashSet(), DryRunLibraryImportStatus.NOT_ADDED, newHashSet("NotExisting.py"));
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenIncorrectRelativePathIsUsedInLibraryImport() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  TestLib.py", "*** Test Cases ***"));

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet("TestLib"), DryRunLibraryImportStatus.ALREADY_EXISTING,
                newHashSet(), DryRunLibraryImportStatus.NOT_ADDED, newHashSet());
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
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

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet("OtherLib"), DryRunLibraryImportStatus.ALREADY_EXISTING,
                newHashSet(), DryRunLibraryImportStatus.NOT_ADDED, newHashSet());
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistingLibIsFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(
                projectProvider.createFile("suite.robot", "*** Settings ***", "Library  other/dir/OtherLib.py",
                        "Library  ./libs/TestLib.py", "Library  NotExisting.py", "*** Test Cases ***"));

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), summaryHandler, "TestLib").start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet("TestLib"), DryRunLibraryImportStatus.ALREADY_EXISTING,
                newHashSet(), DryRunLibraryImportStatus.NOT_ADDED, newHashSet());
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
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
                "*** Settings ***", "Library  NotExisting.py", "*** Test Cases ***"));

        final ReferencedLibrary lib1 = createLibrary("TestLib", "libs/TestLib.py");
        final ReferencedLibrary lib2 = createLibrary("OtherLib", "other/dir/OtherLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite1, suite2, suite3), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib1, lib2);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet("TestLib", "OtherLib"),
                DryRunLibraryImportStatus.ALREADY_EXISTING, newHashSet(), DryRunLibraryImportStatus.NOT_ADDED,
                newHashSet("NotExisting.py"));
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_forResourceAndSuite() throws Exception {
        final RobotSuiteFile suite1 = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***", "Library  ./libs/TestLib.py", "*** Test Cases ***"));
        final RobotSuiteFile suite2 = model.createSuiteFile(projectProvider.createFile("resource.robot",
                "*** Settings ***", "Library  ./other/dir/OtherLib.py", "Library  NotExisting.py"));

        final ReferencedLibrary lib1 = createLibrary("TestLib", "libs/TestLib.py");
        final ReferencedLibrary lib2 = createLibrary("OtherLib", "other/dir/OtherLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite1, suite2), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib1, lib2);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet("TestLib", "OtherLib"),
                DryRunLibraryImportStatus.ALREADY_EXISTING, newHashSet(), DryRunLibraryImportStatus.NOT_ADDED,
                newHashSet("NotExisting.py"));
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
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

        final String libPath = projectProvider.getFile("libs/TestLib.py").getLocation().toPortableString();
        projectProvider.createFile(linkedFile, "*** Settings ***", "Library  " + libPath, "*** Test Cases ***");

        final RobotSuiteFile suite = model.createSuiteFile(linkedFile);

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet("TestLib"), DryRunLibraryImportStatus.ALREADY_EXISTING,
                newHashSet(), DryRunLibraryImportStatus.NOT_ADDED, newHashSet());
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenNoLibrariesAreFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  NotExisting.py", "*** Test Cases ***"));

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet(), DryRunLibraryImportStatus.ALREADY_EXISTING, newHashSet(),
                DryRunLibraryImportStatus.NOT_ADDED, newHashSet("NotExisting.py"));
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenNotExistingLibIsNotFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  ./libs/TestLib.py", "*** Test Cases ***"));

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), summaryHandler, "NotExistingLib").start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();

        verify(summaryHandler).accept(Collections.emptyList());
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenImportedLibraryIsAlreadyAdded() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot", "*** Settings ***",
                "Library  ./libs/TestLib.py", "*** Test Cases ***"));

        final ReferencedLibrary lib = createLibrary("TestLib", "libs/TestLib.py");

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(lib);
        projectProvider.configure(config);

        new LibrariesAutoDiscoverer(robotProject, newArrayList(suite), summaryHandler).start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(lib);

        final ImmutableMap<DryRunLibraryImportStatus, Set<String>> namesByStatus = ImmutableMap.of(
                DryRunLibraryImportStatus.ADDED, newHashSet(), DryRunLibraryImportStatus.ALREADY_EXISTING,
                newHashSet("TestLib"), DryRunLibraryImportStatus.NOT_ADDED, newHashSet());
        verify(summaryHandler).accept(argThat(hasLibNames(namesByStatus)));
        verifyNoMoreInteractions(summaryHandler);
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

    private static ArgumentMatcher<Collection<RobotDryRunLibraryImport>> hasLibNames(
            final Map<DryRunLibraryImportStatus, Set<String>> namesByStatus) {
        return toMatch -> {
            final Map<DryRunLibraryImportStatus, Set<String>> actualNamesByStatus = new HashMap<>();
            namesByStatus.forEach((status, names) -> {
                final Set<String> actualNames = toMatch.stream()
                        .filter(lib -> lib.getStatus() == status)
                        .map(RobotDryRunLibraryImport::getName)
                        .collect(Collectors.toSet());
                actualNamesByStatus.put(status, actualNames);

            });
            return actualNamesByStatus.equals(namesByStatus);
        };
    }

    private static File getFile(final File root, final String... path) {
        if (path == null || path.length == 0) {
            return root;
        } else {
            return getFile(new File(root, path[0]), Arrays.copyOfRange(path, 1, path.length));
        }
    }
}
