/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockeclipse.ContextInjector;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteSettingCommandTest {

    @Test
    public void testDeleteSettingsAndReturnToPreviousState() {
        final List<RobotKeywordCall> createdCalls = createSettings();
        final List<RobotSetting> settingsToRemove = newArrayList();

        final int[] indexesToRemove = new int[] { 0, 2, 5, 7, 8, 10, 12, 14, 16, 19 };
        for (int i = 0; i < indexesToRemove.length; i++) {
            settingsToRemove.add((RobotSetting) createdCalls.get(indexesToRemove[i]));
        }

        final EditorCommand command = new DeleteSettingCommand(settingsToRemove);

        final IEventBroker eventBroker = mock(IEventBroker.class);
        ContextInjector.prepareContext().inWhich(eventBroker).isInjectedInto(command).execute();

        assertThat(createdCalls).hasSize(10);
        assertThat(createdCalls.get(0).getName()).isEqualTo("Suite Teardown");
        assertThat(createdCalls.get(3).getName()).isEqualTo("Metadata");
        assertThat(createdCalls.get(5).getName()).isEqualTo("Variables");
        assertThat(createdCalls.get(6).getName()).isEqualTo("Force Tags");
        assertThat(createdCalls.get(7).getName()).isEqualTo("Library");
        assertThat(createdCalls.get(9).getName()).isEqualTo("Resource");

        List<EditorCommand> undoCommands = command.getUndoCommands();
        for (final EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        assertThat(createdCalls).hasSize(20);
        for (int i = 0; i < indexesToRemove.length; i++) {
            assertThat(createdCalls.get(indexesToRemove[i])).isEqualTo(settingsToRemove.get(i));
        }

        final List<EditorCommand> redoCommands = new ArrayList<>();
        for (final EditorCommand undoCommand : undoCommands) {
            redoCommands.addAll(0, undoCommand.getUndoCommands());
        }
        for (final EditorCommand redoCommand : redoCommands) {
            redoCommand.execute();
        }

        assertThat(createdCalls).hasSize(10);
        assertThat(createdCalls.get(0).getName()).isEqualTo("Suite Teardown");
        assertThat(createdCalls.get(3).getName()).isEqualTo("Metadata");
        assertThat(createdCalls.get(5).getName()).isEqualTo("Variables");
        assertThat(createdCalls.get(6).getName()).isEqualTo("Force Tags");
        assertThat(createdCalls.get(7).getName()).isEqualTo("Library");
        assertThat(createdCalls.get(9).getName()).isEqualTo("Resource");

        undoCommands = new ArrayList<>();
        for (final EditorCommand redoCommand : redoCommands) {
            undoCommands.addAll(0, redoCommand.getUndoCommands());
        }
        for (final EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        assertThat(createdCalls).hasSize(20);
        for (int i = 0; i < indexesToRemove.length; i++) {
            assertThat(createdCalls.get(indexesToRemove[i])).isEqualTo(settingsToRemove.get(i));
        }
    }

    private static List<RobotKeywordCall> createSettings() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Suite Setup    1   2   3   #  comment")
                .appendLine("Suite Teardown    1   2   3   #  comment")
                .appendLine("Test Setup    1   2   3   #  comment")
                .appendLine("Test Teardown    1   2   3   #  comment")
                .appendLine("Test Template    1   2   3   #  comment")
                .appendLine("Test Timeout    1   2   3   #  comment")
                .appendLine("Metadata    1   2    #  comment")
                .appendLine("Metadata    4   5   #  comment")
                .appendLine("Metadata    7   8   #  comment")
                .appendLine("Metadata    10   11  #  comment")
                .appendLine("Library    lib1   2   3   #  comment")
                .appendLine("Variables    var1   2   3   #  comment")
                .appendLine("Resource    res1   #  comment")
                .appendLine("Force Tags    t1   t2  #  comment")
                .appendLine("Default Tags    t3   t4  #  comment")
                .appendLine("Library    lib2   2   3   #  comment")
                .appendLine("Library    lib3   2   3   #  comment")
                .appendLine("Variables    var2   2   3   #  comment")
                .appendLine("Resource    res2   #  comment")
                .appendLine("Resource    res3   #  comment")
                .build();
        return model.findSection(RobotSettingsSection.class).get().getChildren();
    }
}
