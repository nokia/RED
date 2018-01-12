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
import org.rf.ide.core.project.RobotProjectConfig.ExcludedFolderPath;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.red.junit.ProjectProvider;

public class ExcludedPathModifyChangeTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ExcludedPathModifyChangeTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.configure();
    }

    @Test
    public void checkChangeName() {
        final ExcludedFolderPath excludedPathToModify = ExcludedFolderPath.create("a/b/c");
        final ExcludedFolderPath modifiedPath = ExcludedFolderPath.create("d/e/f/b/c");

        final ExcludedPathModifyChange change = new ExcludedPathModifyChange(
                projectProvider.getFile(new Path("red.xml")), excludedPathToModify, modifiedPath);

        assertThat(change.getName()).isEqualTo("The path 'a/b/c' will change to 'd/e/f/b/c'");
        assertThat(change.getModifiedElement()).isSameAs(excludedPathToModify);
    }

    @Test
    public void excludedPathIsModified_whenChangeIsPerformed() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("a/b/c");

        final ExcludedFolderPath excludedPathToModify = config.getExcludedPath().get(0);
        final ExcludedFolderPath modifiedPath = ExcludedFolderPath.create("d/e/f/b/c");

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final ExcludedPathModifyChange change = new ExcludedPathModifyChange(
                projectProvider.getFile(new Path("red.xml")), excludedPathToModify, modifiedPath, eventBroker);

        change.initializeValidationData(null);
        assertThat(change.isValid(null).isOK()).isTrue();
        final Change undoOperation = change.perform(null);

        assertThat(undoOperation).isInstanceOf(ExcludedPathModifyChange.class);
        assertThat(config.getExcludedPath()).contains(ExcludedFolderPath.create("d/e/f/b/c"));
        verify(eventBroker, times(1)).send(
                eq(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED),
                any(RedProjectConfigEventData.class));

        undoOperation.perform(null);
        assertThat(config.getExcludedPath()).contains(ExcludedFolderPath.create("a/b/c"));
    }
}
