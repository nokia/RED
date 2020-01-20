/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.ADDED;
import static org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.NOT_ADDED;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.createImport;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.hasLibImports;
import static org.robotframework.red.junit.jupiter.ProjectExtension.configure;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import org.assertj.core.api.Condition;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.execution.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.SearchPath;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class SimpleLibrariesAutoDiscovererTest {

    private static final String PROJECT_NAME = SimpleLibrariesAutoDiscovererTest.class.getSimpleName();

    @Project(dirs = { "libs", "proj_module", "excluded", "python_path", "python_path/module" },
            files = { "proj_module/__init__.py", "python_path/module/__init__.py" })

    static IProject project;

    private Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler;

    private RobotModel model;

    private RobotProject robotProject;

    @BeforeAll
    public static void beforeClass() throws Exception {
        createFile(project, "libs/CorrectLib.py", "def kw():", " pass");
        createFile(project, "libs/CorrectLibWithClasses.py", "class ClassA(object):", "  def kw():", "   pass",
                "class ClassB(object):", "  def kw():", "   pass", "class ClassC(object):", "  def kw():", "   pass");
        createFile(project, "libs/ErrorLib.py", "error():");
        createFile(project, "excluded/ExcludedLib.py", "def kw():", " pass");
        createFile(project, "python_path/module/ModuleLib.py", "def kw():", " pass");

        // this should not be found in any case
        createFile(project, "libs/notUsedLib.py", "def kw():", " pass");
        createFile(project, "notUsedTest.robot",
                "*** Settings ***",
                "Library  libs/notUsedLib.py",
                "*** Test Cases ***");
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void beforeTest() throws Exception {
        model = new RobotModel();
        robotProject = model.createRobotProject(project);
        configure(project);
        summaryHandler = mock(Consumer.class);
    }

    @AfterEach
    public void after() throws Exception {
        model = null;
        robotProject.clearConfiguration();
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistingLibIsFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  CorrectLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "CorrectLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(1);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0))
                .has(sameFieldsAs(ReferencedLibrary.create(LibraryType.PYTHON, "CorrectLib",
                        PROJECT_NAME + "/libs/CorrectLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(createImport(ADDED, "CorrectLib",
                getFile(project, "libs/CorrectLib.py"), newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistingModuleFromProjectIsFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  proj_module",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "proj_module", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(1);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0))
                .has(sameFieldsAs(
                        ReferencedLibrary.create(LibraryType.PYTHON, "proj_module",
                                PROJECT_NAME + "/proj_module/__init__.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(createImport(ADDED, "proj_module",
                getFile(project, "proj_module/__init__.py"), newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistingLibIsFoundByQualifiedName() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  CorrectLibWithClasses.ClassB",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "CorrectLibWithClasses.ClassB", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(1);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "CorrectLibWithClasses.ClassB",
                        PROJECT_NAME + "/libs/CorrectLibWithClasses.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(createImport(ADDED, "CorrectLibWithClasses.ClassB",
                getFile(project, "libs/CorrectLibWithClasses.py"), newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistingLibFromModuleFromPythonPathIsFoundByQualifiedName()
            throws Exception {
        final String pythonPath = getDir(project, "python_path").getLocation().toFile().getAbsolutePath();
        robotProject.getRobotProjectConfig().addPythonPath(SearchPath.create(pythonPath));

        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  module.ModuleLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "module.ModuleLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).hasSize(1);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries().get(0)).has(sameFieldsAs(
                ReferencedLibrary.create(LibraryType.PYTHON, "module.ModuleLib",
                        PROJECT_NAME + "/python_path/module/ModuleLib.py")));

        verify(summaryHandler).accept(argThat(hasLibImports(createImport(ADDED, "module.ModuleLib",
                getFile(project, "python_path/module/ModuleLib.py"), newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenExistingLibContainsError() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ErrorLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "ErrorLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEmpty();

        verify(summaryHandler)
                .accept(argThat(hasLibImports(createImport(NOT_ADDED, "ErrorLib", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenNotExistingLibIsNotFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  NotExistingLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "NotExistingLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEmpty();

        verify(summaryHandler)
                .accept(argThat(hasLibImports(createImport(NOT_ADDED, "NotExistingLib", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenPathWithExistingLibIsExcluded() throws Exception {
        robotProject.getRobotProjectConfig().addExcludedPath("excluded");

        final RobotSuiteFile suite = model.createSuiteFile(createFile(project, "suite.robot",
                "*** Settings ***",
                "Library  ExcludedLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "ExcludedLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEmpty();

        verify(summaryHandler)
                .accept(argThat(hasLibImports(createImport(NOT_ADDED, "ExcludedLib", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
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
