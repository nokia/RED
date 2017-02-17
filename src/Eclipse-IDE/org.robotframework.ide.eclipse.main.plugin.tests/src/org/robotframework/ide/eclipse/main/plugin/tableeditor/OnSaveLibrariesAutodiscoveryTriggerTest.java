/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.LibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.OnSaveLibrariesAutodiscoveryTrigger.DiscovererFactory;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.collect.Lists;

public class OnSaveLibrariesAutodiscoveryTriggerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(OnSaveLibrariesAutodiscoveryTriggerTest.class);

    private static RobotModel model = new RobotModel();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite_with_known_libraries.robot",
                "*** Settings ***",
                "Library  known1",
                "Library  known2");
        projectProvider.createFile("suite_with_unknown_library_1.robot",
                "*** Settings ***",
                "Library  known1",
                "Library  unknown",
                "Library  known2");
        projectProvider.createFile("suite_with_unknown_library_2.robot",
                "*** Settings ***",
                "Library  unknown_2");

        final ReferencedLibrary knownLib1 = ReferencedLibrary.create(LibraryType.PYTHON, "known1", "");
        final LibrarySpecification knownLib1spec = new LibrarySpecification();
        knownLib1spec.setName("known1");
        final ReferencedLibrary knownLib2 = ReferencedLibrary.create(LibraryType.PYTHON, "known2", "");
        final LibrarySpecification knownLib2spec = new LibrarySpecification();
        knownLib2spec.setName("known2");
        final Map<ReferencedLibrary, LibrarySpecification> libs = new HashMap<>();
        libs.put(knownLib1, knownLib1spec);
        libs.put(knownLib2, knownLib2spec);

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(knownLib1);
        config.addReferencedLibrary(knownLib2);
        projectProvider.configure(config);

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setReferencedLibraries(libs);
        robotProject.setStandardLibraries(new HashMap<String, LibrarySpecification>());
    }
    
    @AfterClass
    public static void afterSuite() {
        model = null;
    }

    @Test
    public void autodiscovererIsNotStarted_whenSuiteDoesNotContainUnknownLibrary() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite_with_known_libraries.robot"));
        final LibrariesAutoDiscoverer discoverer = mock(LibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.<IFile> anyList())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyZeroInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererIsNotStarted_whenItIsDisabled() {
        turnOffAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_1.robot"));
        final LibrariesAutoDiscoverer discoverer = mock(LibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.<IFile> anyList())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyZeroInteractions(discoverer);

        turnOnAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererStarts_whenUnknownLibraryIsDetected() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_1.robot"));
        final LibrariesAutoDiscoverer discoverer = mock(LibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.<IFile> anyList())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        final List<IFile> suites = Lists
                .<IFile> newArrayList(projectProvider.getFile("suite_with_unknown_library_1.robot"));
        verify(factory).create(any(RobotProject.class), eq(suites));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererStartsOnlyOnce_whenMultipleTriggersExistButSaveAllIsDetected() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite1 = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_1.robot"));
        final RobotSuiteFile suite2 = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_2.robot"));
        final LibrariesAutoDiscoverer discoverer = mock(LibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.<IFile> anyList())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger1 = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        final OnSaveLibrariesAutodiscoveryTrigger trigger2 = new OnSaveLibrariesAutodiscoveryTrigger(factory);

        trigger1.preExecute(OnSaveLibrariesAutodiscoveryTrigger.SAVE_ALL_COMMAND_ID, new ExecutionEvent());
        trigger2.preExecute(OnSaveLibrariesAutodiscoveryTrigger.SAVE_ALL_COMMAND_ID, new ExecutionEvent());

        trigger1.startLibrariesAutoDiscoveryIfRequired(suite1);
        trigger2.startLibrariesAutoDiscoveryIfRequired(suite2);

        trigger1.postExecuteSuccess(OnSaveLibrariesAutodiscoveryTrigger.SAVE_ALL_COMMAND_ID, new Object());
        trigger2.postExecuteSuccess(OnSaveLibrariesAutodiscoveryTrigger.SAVE_ALL_COMMAND_ID, new Object());

        final List<IFile> suites = Lists.<IFile> newArrayList(
                projectProvider.getFile("suite_with_unknown_library_1.robot"),
                projectProvider.getFile("suite_with_unknown_library_2.robot"));
        verify(factory).create(any(RobotProject.class), eq(suites));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    private void turnOnAutoDiscoveringInProjectConfig() {
        final RobotProjectConfig config = model.createRobotProject(projectProvider.getProject())
                .getRobotProjectConfig();
        config.setReferencedLibrariesAutoDiscoveringEnabled(true);
    }

    private void turnOffAutoDiscoveringInProjectConfig() {
        final RobotProjectConfig config = model.createRobotProject(projectProvider.getProject())
                .getRobotProjectConfig();
        config.setReferencedLibrariesAutoDiscoveringEnabled(false);
    }
}
