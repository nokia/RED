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

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingArgumentCommandTest {

    @Test
    public void testUndoRedoOnFirstArgumentInNotMovingArgsSetting() {
        for (final RobotSetting setting : createSettingWithNotMovingFirstArgument()) {
            final SetSettingArgumentCommand command = new SetSettingArgumentCommand(setting, 0, null);

            final IEventBroker eventBroker = mock(IEventBroker.class);
            ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

            verifyArguments(setting, 3, 0, "");

            final EditorCommand undoCommand = command.getUndoCommands().get(0);
            undoCommand.execute();
            verifyArguments(setting, 3, 0, "1");

            final EditorCommand redoCommand = undoCommand.getUndoCommands().get(0);
            redoCommand.execute();
            verifyArguments(setting, 3, 0, "\\");

            verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, setting);
        }
    }

    @Test
    public void testUndoRedoOnSecondArgumentInNotMovingArgsSetting() {
        for (final RobotSetting setting : createSettingWithNotMovingFirstArgument()) {
            final SetSettingArgumentCommand command = new SetSettingArgumentCommand(setting, 1, null);

            final IEventBroker eventBroker = mock(IEventBroker.class);
            ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

            verifyArguments(setting, 2, 1, "3");

            final EditorCommand undoCommand = command.getUndoCommands().get(0);
            undoCommand.execute();
            verifyArguments(setting, 3, 1, "2");

            final EditorCommand redoCommand = undoCommand.getUndoCommands().get(0);
            redoCommand.execute();
            verifyArguments(setting, 2, 1, "3");

            verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, setting);
        }
    }

    @Test
    public void testUndoRedoOnFirstArgumentInMovingArgsSetting() {
        for (final RobotSetting setting : createSettingWithMovingFirstArgument()) {
            final SetSettingArgumentCommand command = new SetSettingArgumentCommand(setting, 0, null);

            final IEventBroker eventBroker = mock(IEventBroker.class);
            ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

            verifyArguments(setting, 2, 0, "2");

            final EditorCommand undoCommand = command.getUndoCommands().get(0);
            undoCommand.execute();
            verifyArguments(setting, 3, 0, "1");

            final EditorCommand redoCommand = undoCommand.getUndoCommands().get(0);
            redoCommand.execute();
            verifyArguments(setting, 2, 0, "2");

            verify(eventBroker, times(3)).send(RobotModelEvents.ROBOT_KEYWORD_CALL_ARGUMENT_CHANGE, setting);
        }
    }

    private void verifyArguments(final RobotKeywordCall call, final int expectedSize, final int indexToVerify, final String expectedValue) {
        assertThat(call.getArguments()).hasSize(expectedSize);
        assertThat(call.getArguments().get(indexToVerify)).isEqualTo(expectedValue);
    }

    private static List<RobotSetting> createSettingWithNotMovingFirstArgument() {
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
        return model.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .stream()
                .map(RobotSetting.class::cast)
                .collect(toList());
    }

    private static List<RobotSetting> createSettingWithMovingFirstArgument() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Force Tags    1   2   3   # old comment")
                .appendLine("Default Tags    1   2   3   # old comment")
                .build();
        return model.findSection(RobotSettingsSection.class)
                .get()
                .getChildren()
                .stream()
                .map(RobotSetting.class::cast)
                .collect(toList());
    }

}
