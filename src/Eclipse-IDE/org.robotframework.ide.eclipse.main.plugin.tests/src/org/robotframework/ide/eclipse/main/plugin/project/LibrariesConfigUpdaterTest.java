/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Matchers;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.hyperlink.KeywordInLibrarySourceHyperlinkTest;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.Editors;
import org.robotframework.red.junit.ProjectProvider;

public class LibrariesConfigUpdaterTest {

    private static final ReferencedLibrary LIB_1 = ReferencedLibrary.create(LibraryType.PYTHON, "x", "y");

    private static final ReferencedLibrary LIB_2 = ReferencedLibrary.create(LibraryType.JAVA, "a", "b");

    @Rule
    public ProjectProvider projectProvider = new ProjectProvider(KeywordInLibrarySourceHyperlinkTest.class);

    private RobotProject robotProject;

    private IEventBroker eventBroker;

    @Before
    public void before() throws Exception {
        final RobotModel model = new RobotModel();
        projectProvider.configure();
        robotProject = model.createRobotProject(projectProvider.getProject());
        eventBroker = mock(IEventBroker.class);
    }

    @Test
    public void testIfNoLibrariesAreAdded() throws Exception {
        final LibrariesConfigUpdater updater = new LibrariesConfigUpdater(robotProject);

        updater.finalizeLibrariesAdding(eventBroker);

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();
        verifyZeroInteractions(eventBroker);
    }

    @Test
    public void testIfLibrariesAreAdded_whenConfigIsClosed() throws Exception {
        final LibrariesConfigUpdater updater = new LibrariesConfigUpdater(robotProject);

        final List<ReferencedLibrary> libs = Arrays.asList(LIB_1, LIB_2);
        updater.addLibraries(libs);

        updater.finalizeLibrariesAdding(eventBroker);

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEqualTo(libs);
        verify(eventBroker, times(1)).send(
                Matchers.eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED),
                Matchers.any(RedProjectConfigEventData.class));
    }

    @Test
    public void testIfLibrariesAreAdded_whenConfigIsOpened() throws Exception {
        Editors.openInProjectEditor(robotProject.getConfigurationFile());
        final LibrariesConfigUpdater updater = new LibrariesConfigUpdater(robotProject);

        final List<ReferencedLibrary> libs = Arrays.asList(LIB_1, LIB_2);
        updater.addLibraries(libs);

        updater.finalizeLibrariesAdding(eventBroker);

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEmpty();
        verify(eventBroker, times(1)).send(
                Matchers.eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED),
                Matchers.any(RedProjectConfigEventData.class));
    }

    @Test
    public void testIfAllLibrariesAreAdded() throws Exception {
        final LibrariesConfigUpdater updater = new LibrariesConfigUpdater(robotProject);

        final List<ReferencedLibrary> libs1 = Collections.singletonList(LIB_1);
        final List<ReferencedLibrary> libs2 = Collections.singletonList(LIB_2);
        updater.addLibraries(libs1);
        updater.addLibraries(libs2);

        updater.finalizeLibrariesAdding(eventBroker);

        final List<ReferencedLibrary> allLibs = new ArrayList<>(libs1);
        allLibs.addAll(libs2);
        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEqualTo(allLibs);
        verify(eventBroker, times(1)).send(
                Matchers.eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED),
                Matchers.any(RedProjectConfigEventData.class));
    }

    @Test
    public void testIfLibrariesAreAddedOnlyOnce() throws Exception {
        final LibrariesConfigUpdater updater = new LibrariesConfigUpdater(robotProject);

        final List<ReferencedLibrary> libs = Collections.singletonList(LIB_1);
        updater.addLibraries(libs);

        updater.finalizeLibrariesAdding(eventBroker);
        updater.finalizeLibrariesAdding(eventBroker);

        assertThat(robotProject.getRobotProjectConfig().getLibraries()).isEqualTo(libs);
        verify(eventBroker, times(1)).send(
                Matchers.eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED),
                Matchers.any(RedProjectConfigEventData.class));
    }

}
