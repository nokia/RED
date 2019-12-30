/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingCommentCommandTest {

    @Test
    public void commentsAreProperlyUpdatedForSettingsElements() {
        for (final RobotSetting generalSetting : createSettingsKeywordCalls()) {
            changeAndVerify(generalSetting, "new comment", "#new comment");
        }
    }

    @Test
    public void nothingChangesWhenTryingToSetSameCommentForSettingsElements() {
        for (final RobotSetting generalSetting : createSettingsKeywordCalls()) {
            changeToSameAndVerify(generalSetting);
        }
    }

    @Test
    public void commentsAreProperlyRemovedForSettingsElements() {
        for (final RobotSetting generalSetting : createSettingsKeywordCalls()) {
            changeAndVerify(generalSetting, null, "");
        }
    }

    private void changeAndVerify(final RobotSetting setting, final String newComment,
            final String expected) {
        final String oldComment = setting.getComment();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingCommentCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetSettingCommentCommand(setting, newComment));
        command.execute();

        assertThat(setting.getComment()).isEqualTo(expected);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(setting.getComment()).isEqualTo(oldComment);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, setting);
        verifyNoMoreInteractions(eventBroker);
    }

    private static void changeToSameAndVerify(final RobotSetting setting) {
        final String oldComment = setting.getComment();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetSettingCommentCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetSettingCommentCommand(setting, oldComment));
        command.execute();

        assertThat(setting.getComment()).isEqualTo(oldComment);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }
        assertThat(setting.getComment()).isEqualTo(oldComment);

        verifyNoInteractions(eventBroker);
    }

    private static List<RobotSetting> createSettingsKeywordCalls() {
        final RobotSuiteFile model = new RobotSuiteFileCreator()
                .appendLine("*** Settings ***")
                .appendLine("Suite Setup    1   2   3   # old comment")
                .appendLine("Suite Teardown    1   2   3   # old comment")
                .appendLine("Test Setup    1   2   3   # old comment")
                .appendLine("Test Teardown    1   2   3   # old comment")
                .appendLine("Test Template    1   2   3   # old comment")
                .appendLine("Test Timeout    1   2   3   # old comment")
                .appendLine("Force Tags    1   2   3   # old comment")
                .appendLine("Default Tags    1   2   3   # old comment")
                .appendLine("Metadata    1   2   3   # old comment")
                .appendLine("Library    1   2   3   # old comment")
                .appendLine("Variables    1   2   3   # old comment")
                .appendLine("Resource    1   2   3   # old comment")
                .build();
        return model.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .stream()
                .map(RobotSetting.class::cast)
                .collect(toList());
    }
}
