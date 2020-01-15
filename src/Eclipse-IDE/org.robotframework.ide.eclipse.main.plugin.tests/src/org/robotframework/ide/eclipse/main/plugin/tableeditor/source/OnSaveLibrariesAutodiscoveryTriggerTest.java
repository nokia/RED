/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.addRobotNature;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.removeRobotNature;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.rf.ide.core.libraries.LibraryDescriptor;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibraryArgumentsVariant;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.CombinedLibrariesAutoDiscoverer;
import org.robotframework.ide.eclipse.main.plugin.project.dryrun.LibrariesAutoDiscoverer.DiscovererFactory;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ PreferencesExtension.class, ProjectExtension.class })
public class OnSaveLibrariesAutodiscoveryTriggerTest {

    @Project(dirs = { ".hidden_dir", "resources" }, createDefaultRedXml = true, useRobotNature = true)
    static IProject project;

    private static RobotModel model = new RobotModel();

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, ".hidden_dir/from_hidden.robot",
                "*** Settings ***",
                "Library  unknown");
        createFile(project, "suite_with_known_libraries.robot",
                "*** Settings ***",
                "Library  known1",
                "Library  known2");
        createFile(project, "suite_with_unknown_library_1.robot",
                "*** Settings ***",
                "Library  known1",
                "Library  unknown",
                "Library  known2");
        createFile(project, "suite_with_unknown_library_2.robot",
                "*** Settings ***",
                "Library  unknown_2");
        createFile(project, "suite_with_unknown_remote_library.robot",
                "*** Settings ***",
                "Library  Remote  uri=http://127.0.0.1:9000/  timeout=60");
        createFile(project, "suite_with_unknown_library_in_resource.robot",
                "*** Settings ***",
                "Resource  resources/resource_with_unknown_library.robot");
        createFile(project, "resources/resource_with_unknown_library.robot",
                "*** Settings ***",
                "Library  unknown");
        createFile(project, "suite_with_unknown_library_in_resources_with_cycle.robot",
                "*** Settings ***",
                "Resource  resources/resource_with_cycle_1.robot");
        createFile(project, "resources/resource_with_cycle_1.robot",
                "*** Settings ***",
                "Resource  resource_with_cycle_2.robot");
        createFile(project, "resources/resource_with_cycle_2.robot",
                "*** Settings ***",
                "Resource  resource_with_cycle_1.robot",
                "Library  unknown");
        createFile(project, "suite_with_known_libraries_in_resource.robot",
                "*** Settings ***",
                "Library  knownInResource.py",
                "Resource  resources/resource_with_known_libraries.robot");
        createFile(project, "resources/resource_with_known_libraries.robot",
                "*** Settings ***",
                "Library  ../knownInResource.py");
        createFile(project, "knownInResource.py");
        createFile(project, "suite_with_windows_paths.robot",
                "*** Settings ***",
                "Library  C:\\Users\\Lib.py.py");

        final ReferencedLibrary refLib1 = ReferencedLibrary.create(LibraryType.PYTHON, "known1",
                project.getName() + "/known1.py");
        final ReferencedLibrary refLib2 = ReferencedLibrary.create(LibraryType.PYTHON, "known2",
                project.getName() + "/known2.py");
        final ReferencedLibrary refLib3 = ReferencedLibrary.create(LibraryType.PYTHON, "knownInResource",
                project.getName() + "/knownInResource.py");
        final ReferencedLibraryArgumentsVariant variant = ReferencedLibraryArgumentsVariant.create();

        final LibraryDescriptor desc1 = LibraryDescriptor.ofReferencedLibrary(refLib1, variant);
        final LibraryDescriptor desc2 = LibraryDescriptor.ofReferencedLibrary(refLib2, variant);
        final LibraryDescriptor desc3 = LibraryDescriptor.ofReferencedLibrary(refLib3, variant);
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

        final RobotProject robotProject = model.createRobotProject(project);
        robotProject.setReferencedLibraries(libs);
    }

    @AfterAll
    public static void afterSuite() {
        model = null;
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererIsNotStarted_whenSuiteDoesNotContainUnknownLibrary() {
        final RobotSuiteFile suite = model.createSuiteFile(getFile(project, "suite_with_known_libraries.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyNoInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = false)
    @Test
    public void autodiscovererIsNotStarted_whenItIsDisabled() {
        final RobotSuiteFile suite = model
                .createSuiteFile(getFile(project, "suite_with_unknown_library_1.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyNoInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererIsNotStarted_whenProjectDoesNotHaveRobotNature() throws Exception {
        removeRobotNature(project);

        final RobotSuiteFile suite = model
                .createSuiteFile(getFile(project, "suite_with_unknown_library_1.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyNoInteractions(discoverer);

        addRobotNature(project);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererIsNotStarted_whenSuiteIsInEclipseHiddenDirectory() throws Exception {
        final RobotSuiteFile suite = model.createSuiteFile(getFile(project, ".hidden_dir/from_hidden.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyNoInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererIsNotStarted_whenSuiteIsExcludedFromProject() throws Exception {
        excludePathInProjectConfig("suite_with_unknown_library_1.robot");

        final RobotSuiteFile suite = model
                .createSuiteFile(getFile(project, "suite_with_unknown_library_1.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyNoInteractions(discoverer);

        includePathInProjectConfig("suite_with_unknown_library_1.robot");
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererStarts_whenUnknownLibraryIsDetected() {
        final RobotSuiteFile suite = model
                .createSuiteFile(getFile(project, "suite_with_unknown_library_1.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verify(factory).create(suite.getRobotProject(), newArrayList(suite));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererStarts_whenUnknownRemoteLibraryIsDetected() {
        final RobotSuiteFile suite = model
                .createSuiteFile(getFile(project, "suite_with_unknown_remote_library.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verify(factory).create(suite.getRobotProject(), newArrayList(suite));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererStarts_whenUnknownLibraryIsDetectedInImportedResource() {
        final RobotSuiteFile suite = model
                .createSuiteFile(getFile(project, "suite_with_unknown_library_in_resource.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verify(factory).create(suite.getRobotProject(), newArrayList(suite));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererStarts_whenUnknownLibraryIsDetectedInImportedResourcesWithCycle() {

        final RobotSuiteFile suite = model
                .createSuiteFile(getFile(project, "suite_with_unknown_library_in_resources_with_cycle.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verify(factory).create(suite.getRobotProject(), newArrayList(suite));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererStarts_whenLibraryImportedWithWindowsPathSeparatorIsDetected() {
        final RobotSuiteFile suite = model.createSuiteFile(getFile(project, "suite_with_windows_paths.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verify(factory).create(suite.getRobotProject(), newArrayList(suite));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererIsNotStarted_whenSuiteDoesNotContainUnknownLibraryInResource() {
        final RobotSuiteFile suite = model
                .createSuiteFile(getFile(project, "suite_with_known_libraries_in_resource.robot"));
        final CombinedLibrariesAutoDiscoverer discoverer = mock(CombinedLibrariesAutoDiscoverer.class);

        final DiscovererFactory factory = mock(DiscovererFactory.class);
        when(factory.create(any(RobotProject.class), ArgumentMatchers.anyCollection())).thenReturn(discoverer);

        final OnSaveLibrariesAutodiscoveryTrigger trigger = new OnSaveLibrariesAutodiscoveryTrigger(factory);
        trigger.startLibrariesAutoDiscoveryIfRequired(suite);

        verifyNoInteractions(discoverer);
    }

    @BooleanPreference(key = RedPreferences.SAVE_ACTIONS_AUTO_DISCOVERING_ENABLED, value = true)
    @Test
    public void autodiscovererStartsOnlyOnce_whenMultipleTriggersExistButSaveAllIsDetected() {
        final RobotSuiteFile suite1 = model
                .createSuiteFile(getFile(project, "suite_with_unknown_library_1.robot"));
        final RobotSuiteFile suite2 = model
                .createSuiteFile(getFile(project, "suite_with_unknown_library_2.robot"));
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

        verify(factory).create(suite1.getRobotProject(), newArrayList(suite1, suite2));

        verify(discoverer).start();
        verifyNoMoreInteractions(discoverer);
    }

    private void excludePathInProjectConfig(final String path) {
        final RobotProjectConfig config = model.createRobotProject(project).getRobotProjectConfig();
        config.addExcludedPath(path);
    }

    private void includePathInProjectConfig(final String path) {
        final RobotProjectConfig config = model.createRobotProject(project).getRobotProjectConfig();
        config.removeExcludedPath(path);
    }
}
