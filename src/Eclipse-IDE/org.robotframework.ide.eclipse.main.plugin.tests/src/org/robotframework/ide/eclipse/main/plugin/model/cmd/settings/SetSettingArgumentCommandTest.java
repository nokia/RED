/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingArgumentCommandTest {

    @Test
    public void testUndoRedoOnFirstArgumentInNotMovingArgsSetting() {
        for (RobotKeywordCall call : createSettingWithNotMovingFirstArgument()) {
            SetSettingArgumentCommand command = new SetSettingArgumentCommand(call, 0, null);

            final IEventBroker eventBroker = mock(IEventBroker.class);
            ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

            verifyArguments(call, 3, 0, "");

            EditorCommand undoCommand = command.getUndoCommand();
            undoCommand.execute();
            verifyArguments(call, 3, 0, "1");

            EditorCommand redoCommand = undoCommand.getUndoCommand();
            redoCommand.execute();
            verifyArguments(call, 3, 0, "\\");

            verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
        }
    }

    @Test
    public void testUndoRedoOnSecondArgumentInNotMovingArgsSetting() {
        for (RobotKeywordCall call : createSettingWithNotMovingFirstArgument()) {
            SetSettingArgumentCommand command = new SetSettingArgumentCommand(call, 1, null);

            final IEventBroker eventBroker = mock(IEventBroker.class);
            ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

            verifyArguments(call, 2, 1, "3");

            EditorCommand undoCommand = command.getUndoCommand();
            undoCommand.execute();
            verifyArguments(call, 3, 1, "2");

            EditorCommand redoCommand = undoCommand.getUndoCommand();
            redoCommand.execute();
            verifyArguments(call, 2, 1, "3");

            verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
        }
    }

    @Test
    public void testUndoRedoOnFirstArgumentInMovingArgsSetting() {
        for (RobotKeywordCall call : createSettingWithMovingFirstArgument()) {
            SetSettingArgumentCommand command = new SetSettingArgumentCommand(call, 0, null);

            final IEventBroker eventBroker = mock(IEventBroker.class);
            ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

            verifyArguments(call, 2, 0, "2");

            EditorCommand undoCommand = command.getUndoCommand();
            undoCommand.execute();
            verifyArguments(call, 3, 0, "1");

            EditorCommand redoCommand = undoCommand.getUndoCommand();
            redoCommand.execute();
            verifyArguments(call, 2, 0, "2");

            verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, call);
        }
    }

    private void verifyArguments(RobotKeywordCall call, int expectedSize, int indexToVerify, String expectedValue) {
        assertTrue(call.getArguments().size() == expectedSize);
        assertThat(call.getArguments().get(indexToVerify)).isEqualTo(expectedValue);
    }

    private static List<RobotKeywordCall> createSettingWithNotMovingFirstArgument() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup    1   2   3   # old comment")
                .appendLine("Suite Teardown    1   2   3   # old comment")
                .appendLine("Test Setup    1   2   3   # old comment")
                .appendLine("Test Teardown    1   2   3   # old comment")
                .appendLine("Test Template    1   2   3   # old comment")
                .appendLine("Test Timeout    1   2   3   # old comment")
                .appendLine("Metadata    1   2   3   # old comment")
                .appendLine("Library    1   2   3   # old comment")
                .appendLine("Variables    1   2   3   # old comment")
                .appendLine("Resource    1   2   3   # old comment")
                .build();
        return model.findSection(RobotSettingsSection.class).get().getChildren();
    }

    private static List<RobotKeywordCall> createSettingWithMovingFirstArgument() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Force Tags    1   2   3   # old comment")
                .appendLine("Default Tags    1   2   3   # old comment")
                .build();
        return model.findSection(RobotSettingsSection.class).get().getChildren();
    }

}
