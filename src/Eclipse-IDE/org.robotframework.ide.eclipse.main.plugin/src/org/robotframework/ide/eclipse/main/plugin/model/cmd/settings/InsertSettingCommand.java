/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class InsertSettingCommand extends EditorCommand {

    private final RobotSettingsSection section;

    private final int index;

    private final List<RobotKeywordCall> settingsToInsert;

    final List<RobotSetting> insertedSettings = new ArrayList<>();

    public InsertSettingCommand(final RobotSettingsSection parent, final Optional<RobotSetting> firstSelectedSetting,
            final RobotKeywordCall[] settingsToInsert) {

        this(parent, firstSelectedSetting.isPresent() ? parent.getChildren().indexOf(firstSelectedSetting.get())
                : parent.getChildren().size(), settingsToInsert);
    }

    public InsertSettingCommand(final RobotSettingsSection parent, final int index,
            final RobotKeywordCall[] settingsToInsert) {
        this.section = parent;
        this.index = index;
        this.settingsToInsert = Arrays.asList(settingsToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {

        int shift = 0;
        insertedSettings.clear();
        for (final RobotKeywordCall call : settingsToInsert) {
            insertedSettings.add(section.insertSetting(call, index + shift));
            shift++;
        }

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(insertedSettings.isEmpty() ? SettingsGroup.NO_GROUP : insertedSettings.get(0).getGroup())
                .send(RobotModelEvents.ROBOT_SETTING_ADDED, section);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new DeleteSettingCommand(insertedSettings));
    }
}
