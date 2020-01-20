/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PartInitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.Editors;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class LibrariesConfigUpdaterTest {

    private static final ReferencedLibrary LIB_1 = ReferencedLibrary.create(LibraryType.PYTHON, "x", "y");

    private static final ReferencedLibrary LIB_2 = ReferencedLibrary.create(LibraryType.JAVA, "a", "b");

    @Project(createDefaultRedXml = true)
    IProject project;

    private RobotProject robotProject;

    private IEventBroker eventBroker;

    @BeforeEach
    public void before() {
        robotProject = new RobotModel().createRobotProject(project);
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void testIfNoLibrariesAreAdded() {
        final LibrariesConfigUpdater updater = LibrariesConfigUpdater.createFor(robotProject);

        updater.finalizeLibrariesAdding(eventBroker);

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEmpty();
        verifyNoInteractions(eventBroker);
    }

    @Test
    public void testIfLibrariesAreAdded_whenConfigIsClosed() {
        final LibrariesConfigUpdater updater = LibrariesConfigUpdater.createFor(robotProject);

        final List<ReferencedLibrary> libs = Arrays.asList(LIB_1, LIB_2);
        updater.addLibraries(libs);

        updater.finalizeLibrariesAdding(eventBroker);

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEqualTo(libs);
        verify(eventBroker, times(1)).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_ADDED_REMOVED),
                any(RedProjectConfigEventData.class));
    }

    @Test
    public void testIfLibrariesAreAdded_whenConfigIsOpened() throws PartInitException {
        Editors.openInProjectEditor(robotProject.getConfigurationFile());
        final LibrariesConfigUpdater updater = LibrariesConfigUpdater.createFor(robotProject);

        final List<ReferencedLibrary> libs = Arrays.asList(LIB_1, LIB_2);
        updater.addLibraries(libs);

        updater.finalizeLibrariesAdding(eventBroker);

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEmpty();
        verify(eventBroker, times(1)).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_ADDED_REMOVED),
                any(RedProjectConfigEventData.class));
    }

    @Test
    public void testIfAllLibrariesAreAdded() {
        final LibrariesConfigUpdater updater = LibrariesConfigUpdater.createFor(robotProject);

        final List<ReferencedLibrary> libs1 = Collections.singletonList(LIB_1);
        final List<ReferencedLibrary> libs2 = Collections.singletonList(LIB_2);
        updater.addLibraries(libs1);
        updater.addLibraries(libs2);

        updater.finalizeLibrariesAdding(eventBroker);

        final List<ReferencedLibrary> allLibs = new ArrayList<>(libs1);
        allLibs.addAll(libs2);
        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEqualTo(allLibs);
        verify(eventBroker, times(1)).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_ADDED_REMOVED),
                any(RedProjectConfigEventData.class));
    }

    @Test
    public void testIfLibrariesAreAddedOnlyOnce() {
        final LibrariesConfigUpdater updater = LibrariesConfigUpdater.createFor(robotProject);

        final List<ReferencedLibrary> libs = Collections.singletonList(LIB_1);
        updater.addLibraries(libs);

        updater.finalizeLibrariesAdding(eventBroker);
        updater.finalizeLibrariesAdding(eventBroker);

        assertThat(robotProject.getRobotProjectConfig().getReferencedLibraries()).isEqualTo(libs);
        verify(eventBroker, times(1)).send(eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARY_ADDED_REMOVED),
                any(RedProjectConfigEventData.class));
    }

}
