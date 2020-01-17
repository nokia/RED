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
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ltk.core.refactoring.Change;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExcludedPath;
import org.robotframework.ide.eclipse.main.plugin.project.RedProjectConfigEventData;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectConfigEvents;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class ExcludedPathRemoveChangeTest {

    @Project(createDefaultRedXml = true)
    static IProject project;

    @Test
    public void checkChangeName() {
        final RobotProjectConfig config = new RobotProjectConfig();
        final ExcludedPath excludedPathToAdd = ExcludedPath.create("a/b/c");

        final ExcludedPathRemoveChange change = new ExcludedPathRemoveChange(getFile(project, "red.xml"), config,
                excludedPathToAdd);

        assertThat(change.getName()).isEqualTo("The path 'a/b/c' will be removed");
        assertThat(change.getModifiedElement()).isSameAs(excludedPathToAdd);
    }

    @Test
    public void excludedPathIsRemoved_whenChangeIsPerformed() throws Exception {
        final RobotProjectConfig config = new RobotProjectConfig();
        config.addExcludedPath("a/b/c");

        final ExcludedPath excludedPathToRemove = config.getExcludedPaths().get(0);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final ExcludedPathRemoveChange change = new ExcludedPathRemoveChange(getFile(project, "red.xml"), config,
                excludedPathToRemove, eventBroker);

        change.initializeValidationData(null);
        assertThat(change.isValid(null).isOK()).isTrue();
        final Change undoOperation = change.perform(null);

        assertThat(undoOperation).isInstanceOf(ExcludedPathAddChange.class);
        assertThat(config.getExcludedPaths()).isEmpty();
        verify(eventBroker, times(1)).send(
                eq(RobotProjectConfigEvents.ROBOT_CONFIG_VALIDATION_EXCLUSIONS_STRUCTURE_CHANGED),
                any(RedProjectConfigEventData.class));

        undoOperation.perform(null);
        assertThat(config.getExcludedPaths()).contains(ExcludedPath.create("a/b/c"));
    }
}
