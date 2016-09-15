/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteSettingCommand extends DeleteKeywordCallCommand {

    public DeleteSettingCommand(final List<RobotSetting> settingsToRemove) {
        super(settingsToRemove, RobotModelEvents.ROBOT_SETTING_REMOVED);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(setupUndoCommandsForDeletedSettings());
    }

    private List<EditorCommand> setupUndoCommandsForDeletedSettings() {
        final List<EditorCommand> commands = new ArrayList<>();
        if (callsToDelete.size() == deletedCallsIndexes.size()) {
            for (int i = 0; i < callsToDelete.size(); i++) {
                final RobotSetting setting = (RobotSetting) callsToDelete.get(i);
                commands.add(new InsertSettingCommand(setting.getParent(), deletedCallsIndexes.get(i),
                        new RobotKeywordCall[] { setting }));
            }
        }
        return commands;
    }
}
