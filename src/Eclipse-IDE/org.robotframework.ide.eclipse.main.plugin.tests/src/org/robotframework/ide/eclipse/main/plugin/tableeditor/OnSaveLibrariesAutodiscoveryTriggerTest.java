/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.LibraryDescriptor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.CombinedLibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.red.junit.ProjectProvider;

public class OnSaveLibrariesAutodiscoveryTriggerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(OnSaveLibrariesAutodiscoveryTriggerTest.class);

    private static RobotModel model = new RobotModel();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("resources");
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
        projectProvider.createFile("suite_with_unknown_library_in_resource.robot",
                "*** Settings ***",
                "Resource  resources/resource_with_unknown_library.robot");
        projectProvider.createFile("resources/resource_with_unknown_library.robot",
                "*** Settings ***",
                "Library  unknown");
        projectProvider.createFile("suite_with_unknown_library_in_resources_with_cycle.robot",
                "*** Settings ***",
                "Resource  resources/resource_with_cycle_1.robot");
        projectProvider.createFile("resources/resource_with_cycle_1.robot",
                "*** Settings ***",
                "Resource  resource_with_cycle_2.robot");
        projectProvider.createFile("resources/resource_with_cycle_2.robot",
                "*** Settings ***",
                "Resource  resource_with_cycle_1.robot",
                "Library  unknown");
        projectProvider.createFile("suite_with_known_libraries_in_resource.robot",
                "*** Settings ***",
                "Library  knownInResource.py",
                "Resource  resources/resource_with_known_libraries.robot");
        projectProvider.createFile("resources/resource_with_known_libraries.robot",
                "*** Settings ***",
                "Library  ../knownInResource.py");
        projectProvider.createFile("knownInResource.py");

        final ReferencedLibrary refLib1 = ReferencedLibrary.create(LibraryType.PYTHON, "known1",
                projectProvider.getProject().getName());
        final ReferencedLibrary refLib2 = ReferencedLibrary.create(LibraryType.PYTHON, "known2",
                projectProvider.getProject().getName());
        final ReferencedLibrary refLib3 = ReferencedLibrary.create(LibraryType.PYTHON, "knownInResource",
                projectProvider.getProject().getName());

        final LibraryDescriptor desc1 = LibraryDescriptor.ofReferencedLibrary(refLib1);
        final LibraryDescriptor desc2 = LibraryDescriptor.ofReferencedLibrary(refLib2);
        final LibraryDescriptor desc3 = LibraryDescriptor.ofReferencedLibrary(refLib3);
        final LibrarySpecification spec1 = LibrarySpecification.create(refLib1.getName());
        spec1.setDescriptor(desc1);
        final LibrarySpecification spec2 = LibrarySpecification.create(refLib2.getName());
        spec2.setDescriptor(desc2);
        final LibrarySpecification spec3 = LibrarySpecification.create(refLib3.getName());
        spec3.setDescriptor(desc3);
        final Map<LibraryDescriptor, LibrarySpecification> libs = new HashMap<>();
        libs.put(desc1, spec1);
        libs.put(desc2, spec2);
        libs.put(desc3, spec3);

        final RobotProjectConfig config = new RobotProjectConfig();
        config.addReferencedLibrary(refLib1);
        config.addReferencedLibrary(refLib2);
        config.addReferencedLibrary(refLib3);

        projectProvider.configure(config);
        projectProvider.addRobotNature();

        final RobotProject robotProject = model.createRobotProject(projectProvider.getProject());
        robotProject.setReferencedLibraries(libs);
    }

    @AfterClass
    public static void afterSuite() {
        model = null;
    }

    @Test
    public void autodiscovererIsNotStarted_whenSuiteDoesNotContainUnknownLibrary() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model.createSuiteFile(projectProvider.getFile("suite_with_known_libraries.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

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
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyZeroInteractions(discoverer);

        turnOnAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererIsNotStarted_whenProjectDoesNotHaveRobotNature() throws Exception {
        turnOnAutoDiscoveringInProjectConfig();
        projectProvider.removeRobotNature();

        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_1.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyZeroInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
        projectProvider.addRobotNature();
    }

    @Test
    public void autodiscovererIsNotStarted_whenSuiteIsExcludedFromProject() throws Exception {
        turnOnAutoDiscoveringInProjectConfig();
        excludePathInProjectConfig("suite_with_unknown_library_1.robot");

        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_1.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyZeroInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
        includePathInProjectConfig("suite_with_unknown_library_1.robot");
    }

    @Test
    public void autodiscovererStarts_whenUnknownLibraryIsDetected() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_1.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verify(factory).create(suite.getProject(), newArrayList(suite));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererStarts_whenUnknownLibraryIsDetectedInImportedResource() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_in_resource.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verify(factory).create(suite.getProject(), newArrayList(suite));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererStarts_whenUnknownLibraryIsDetectedInImportedResourcesWithCycle() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_in_resources_with_cycle.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verify(factory).create(suite.getProject(), newArrayList(suite));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererIsNotStarted_whenSuiteDoesNotContainUnknownLibraryInResource() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite = model
                .createSuiteFile(projectProvider.getFile("suite_with_known_libraries_in_resource.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyZeroInteractions(discoverer);

        turnOffAutoDiscoveringInProjectConfig();
    }

    @Test
    public void autodiscovererStartsOnlyOnce_whenMultipleTriggersExistButSaveAllIsDetected() {
        turnOnAutoDiscoveringInProjectConfig();

        final RobotSuiteFile suite1 = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_1.robot"));
        final RobotSuiteFile suite2 = model
                .createSuiteFile(projectProvider.getFile("suite_with_unknown_library_2.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger1 = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        final OnSaveLibrariesAutodiscoveryTrigger trigger2 = new OnSaveLibrariesAutodiscoveryTrigger(factory);

        trigger1.preExecute(OnSaveLibrariesAutodiscoveryTrigger.SAVE_ALL_COMMAND_ID, new ExecutionEvent());
        trigger2.preExecute(OnSaveLibrariesAutodiscoveryTrigger.SAVE_ALL_COMMAND_ID, new ExecutionEvent());

        trigger1.startLibrariesAutoDiscoveryIfRequired(suite1);
        trigger2.startLibrariesAutoDiscoveryIfRequired(suite2);

        trigger1.postExecuteSuccess(OnSaveLibrariesAutodiscoveryTrigger.SAVE_ALL_COMMAND_ID, new Object());
        trigger2.postExecuteSuccess(OnSaveLibrariesAutodiscoveryTrigger.SAVE_ALL_COMMAND_ID, new Object());

        verify(factory).create(suite1.getProject(), newArrayList(suite1, suite2));

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

    private void excludePathInProjectConfig(final String path) {
        final RobotProjectConfig config = model.createRobotProject(projectProvider.getProject())
                .getRobotProjectConfig();
        config.addExcludedPath(path);
    }

    private void includePathInProjectConfig(final String path) {
        final RobotProjectConfig config = model.createRobotProject(projectProvider.getProject())
                .getRobotProjectConfig();
        config.removeExcludedPath(path);
    }
}
