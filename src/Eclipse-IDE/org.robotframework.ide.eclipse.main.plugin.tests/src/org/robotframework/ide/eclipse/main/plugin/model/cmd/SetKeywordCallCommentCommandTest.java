/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallCommentCommandTest {

    @Test
    public void commentsAreProperlyUpdatedForSettingsElements() {
        for (final RobotKeywordCall generalSetting : createSettingsKeywordCalls()) {
            changeAndVerify(generalSetting, "new comment", "#new comment");
        }
    }

    @Test
    public void commentsAreProperlyRemovedForSettingsElements() {
        for (final RobotKeywordCall generalSetting : createSettingsKeywordCalls()) {
            changeAndVerify(generalSetting, null, "");
        }
    }

    @Test
    public void commentsAreProperlyUpdatedForTestCaseElements() {
        for (final RobotKeywordCall caseExecutable : createTestCaseKeywordCalls()) {
            changeAndVerify(caseExecutable, "new comment", "#new comment");
        }
    }

    @Test
    public void commentsAreProperlyRemovedForTestCaseElements() {
        for (final RobotKeywordCall caseExecutable : createTestCaseKeywordCalls()) {
            changeAndVerify(caseExecutable, null, "");
        }
    }

    @Test
    public void commentsAreProperlyUpdatedForKeywordElements() {
        for (final RobotKeywordCall keywordExecutable : createUserKeywordKeywordCalls()) {
            changeAndVerify(keywordExecutable, "new comment", "#new comment");
        }
    }

    @Test
    public void commentsAreProperlyRemovedForKeywordElements() {
        for (final RobotKeywordCall keywordExecutable : createUserKeywordKeywordCalls()) {
            changeAndVerify(keywordExecutable, null, "");
        }
    }

    private void changeAndVerify(final RobotKeywordCall keywordExecutable, final String newComment,
            final String expected) {
        final Object oldComment = keywordExecutable.getComment();

        final IEventBroker eventBroker = mock(IEventBroker.class);
        final SetKeywordCallCommentCommand command = ContextInjector.prepareContext()
                .inWhich(eventBroker)
                .isInjectedInto(new SetKeywordCallCommentCommand(keywordExecutable, newComment));
        command.execute();

        assertThat(keywordExecutable.getComment()).isEqualTo(expected);

        for (final EditorCommand undo : command.getUndoCommands()) {
            undo.execute();
        }

        assertThat(keywordExecutable.getComment()).isEqualTo(oldComment);

        verify(eventBroker, times(2)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, keywordExecutable);
        verifyNoMoreInteractions(eventBroker);
    }

    private static List<RobotKeywordCall> createSettingsKeywordCalls() {
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
        return model.findSection(RobotSettingsSection.class).get().getChildren();
    }
    
    private static List<RobotKeywordCall> createTestCaseKeywordCalls() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Setup]  1  2  3  # old comment")
                .appendLine("  [Teardown]  1  2  3  # old comment")
                .appendLine("  [Template]  1  2  3  # old comment")
                .appendLine("  [Timeout]  1  2  3  # old comment")
                .appendLine("  [Tags]  1  2  3  # old comment")
                .appendLine("  [Documentation]  1  2  3  # old comment")
                .appendLine("  [Unknown]  1  2  3  # old comment")
                .appendLine("  call  1  2  3  # old comment")
                .build();
        return model.findSection(RobotCasesSection.class).get().getChildren().get(0).getChildren();
    }

    private static List<RobotKeywordCall> createUserKeywordKeywordCalls() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Keywords ***")
                .appendLine("keyword")
                .appendLine("  [Arguments]  1  2  3  # old comment")
                .appendLine("  [Teardown]  1  2  3  # old comment")
                .appendLine("  [Return]  1  2  3  # old comment")
                .appendLine("  [Timeout]  1  2  3  # old comment")
                .appendLine("  [Tags]  1  2  3  # old comment")
                .appendLine("  [Documentation]  1  2  3  # old comment")
                .appendLine("  [Unknown]  1  2  3  # old comment")
                .appendLine("  call  1  2  3  # old comment")
                .build();
        return model.findSection(RobotKeywordsSection.class).get().getChildren().get(0).getChildren();
    }
}
