/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ltk.core.refactoring.Change;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.red.junit.ProjectProvider;


/**
 * @author Jakub Szkatula
 */
public class LibraryPathAddChangeTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibraryPathAddChangeTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
    }

    @Test
    public void checkChangeName() {
        final RobotProjectConfig config = new RobotProjectConfig();
        final ReferencedLibrary libriaryPathToAdd = ReferencedLibrary.create(LibraryType.PYTHON, "a", "a/b/c");

        final LibraryPathAddChange change = new LibraryPathAddChange(projectProvider.getFile(new Path("red.xml")),
                config, libriaryPathToAdd);

        assertThat(change.getName()).isEqualTo("The path 'a/b/c' will be added");
        assertThat(change.getModifiedElement()).isSameAs(libriaryPathToAdd);
    }

    @Test
    public void libraryPathIsAdded_whenChangeIsPerfomed() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        final ReferencedLibrary libriaryPathToAdd = ReferencedLibrary.create(LibraryType.PYTHON, "a", "a/b/c");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final LibraryPathAddChange change = new LibraryPathAddChange(projectProvider.getFile(new Path("red.xml")),
                config, libriaryPathToAdd, eventBroker);

        change.initializeValidationData(null);
        assertThat(change.isValid(null).isOK()).isTrue();
        final Change undoOperation = change.perform(null);

        assertThat(undoOperation).isInstanceOf(LibraryPathRemoveChange.class);
        assertThat(config.getLibraries()).contains(ReferencedLibrary.create(LibraryType.PYTHON, "a", "a/b/c"));
        verify(eventBroker, times(1)).send(
                eq(RobotProjectConfigEvents.ROBOT_CONFIG_LIBRARIES_STRUCTURE_CHANGED),
                any(RedProjectConfigEventData.class));

        undoOperation.perform(null);
        assertThat(config.getLibraries()).isEmpty();
    }
}
