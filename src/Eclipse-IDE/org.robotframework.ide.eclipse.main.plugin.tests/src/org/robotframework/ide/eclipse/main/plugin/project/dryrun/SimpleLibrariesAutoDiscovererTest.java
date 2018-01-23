/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.ADDED;
import static org.rf.ide.core.dryrun.RobotDryRunLibraryImport.DryRunLibraryImportStatus.NOT_ADDED;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.createImport;
import static org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibraryImports.hasLibImports;

import java.util.Collection;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.rf.ide.core.dryrun.RobotDryRunLibraryImport;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

@RunWith(MockitoJUnitRunner.class)
public class SimpleLibrariesAutoDiscovererTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(SimpleLibrariesAutoDiscovererTest.class);

    @Mock
    private Consumer<Collection<RobotDryRunLibraryImport>> summaryHandler;

    private RobotModel model;

    private RobotProject robotProject;

    @BeforeClass
    public static void beforeClass() throws Exception {
        projectProvider.createFile("CorrectLib.py", "def kw():", " pass");
        projectProvider.createFile("CorrectLibWithClasses.py", "class ClassA(object):", "  def kw():", "   pass",
                "class ClassB(object):", "  def kw():", "   pass", "class ClassC(object):", "  def kw():", "   pass");
        projectProvider.createFile("ErrorLib.py", "error():");
        projectProvider.createDir("excluded");
        projectProvider.createFile("excluded/ExcludedLib.py", "def kw():", " pass");

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
    public void libsAreAddedToProjectConfig_whenExistingLibIsFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  CorrectLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "CorrectLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "CorrectLib", projectProvider.getProject().getName()));

        verify(summaryHandler).accept(argThat(hasLibImports(createImport(ADDED, "CorrectLib",
                projectProvider.getFile("CorrectLib.py"), newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void libsAreAddedToProjectConfig_whenExistingLibIsFoundByQualifiedName() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  CorrectLibWithClasses.ClassB",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "CorrectLibWithClasses.ClassB", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).containsExactly(ReferencedLibrary
                .create(LibraryType.PYTHON, "CorrectLibWithClasses.ClassB", projectProvider.getProject().getName()));

        verify(summaryHandler).accept(argThat(hasLibImports(createImport(ADDED, "CorrectLibWithClasses.ClassB",
                projectProvider.getFile("CorrectLibWithClasses.py"), newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenExistingLibContainsError() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ErrorLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "ErrorLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();

        verify(summaryHandler)
                .accept(argThat(hasLibImports(createImport(NOT_ADDED, "ErrorLib", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenNotExistingLibIsNotFound() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  NotExistingLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "NotExistingLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();

        verify(summaryHandler)
                .accept(argThat(hasLibImports(createImport(NOT_ADDED, "NotExistingLib", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }

    @Test
    public void nothingIsAddedToProjectConfig_whenPathWithExistingLibIsExcluded() throws Exception {
        robotProject.getRobotProjectConfig().addExcludedPath("excluded");

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  ExcludedLib",
                "*** Test Cases ***"));

        final SimpleLibrariesAutoDiscoverer discoverer = new SimpleLibrariesAutoDiscoverer(robotProject, suite,
                "ExcludedLib", summaryHandler);
        discoverer.start().join();

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();

        verify(summaryHandler)
                .accept(argThat(hasLibImports(createImport(NOT_ADDED, "ExcludedLib", newHashSet(suite.getFile())))));
        verifyNoMoreInteractions(summaryHandler);
    }
}
