/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertTrue;
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

        assertTrue(createdCalls.size() == 10);
        assertTrue(createdCalls.get(0).getName().equals("Suite Teardown"));
        assertTrue(createdCalls.get(3).getName().equals("Metadata"));
        assertTrue(createdCalls.get(5).getName().equals("Variables"));
        assertTrue(createdCalls.get(6).getName().equals("Force Tags"));
        assertTrue(createdCalls.get(7).getName().equals("Library"));
        assertTrue(createdCalls.get(9).getName().equals("Resource"));

        List<EditorCommand> undoCommands = command.getUndoCommands();
        for (EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        assertTrue(createdCalls.size() == 20);
        for (int i = 0; i < indexesToRemove.length; i++) {
            assertTrue(createdCalls.get(indexesToRemove[i]) == settingsToRemove.get(i));
        }

        List<EditorCommand> redoCommands = new ArrayList<>();
        for (EditorCommand undoCommand : undoCommands) {
            redoCommands.addAll(0, undoCommand.getUndoCommands());
        }
        for (EditorCommand redoCommand : redoCommands) {
            redoCommand.execute();
        }

        assertTrue(createdCalls.size() == 10);
        assertTrue(createdCalls.get(0).getName().equals("Suite Teardown"));
        assertTrue(createdCalls.get(3).getName().equals("Metadata"));
        assertTrue(createdCalls.get(5).getName().equals("Variables"));
        assertTrue(createdCalls.get(6).getName().equals("Force Tags"));
        assertTrue(createdCalls.get(7).getName().equals("Library"));
        assertTrue(createdCalls.get(9).getName().equals("Resource"));

        undoCommands = new ArrayList<>();
        for (EditorCommand redoCommand : redoCommands) {
            undoCommands.addAll(0, redoCommand.getUndoCommands());
        }
        for (EditorCommand undoCommand : undoCommands) {
            undoCommand.execute();
        }

        assertTrue(createdCalls.size() == 20);
        for (int i = 0; i < indexesToRemove.length; i++) {
            assertTrue(createdCalls.get(indexesToRemove[i]) == settingsToRemove.get(i));
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
