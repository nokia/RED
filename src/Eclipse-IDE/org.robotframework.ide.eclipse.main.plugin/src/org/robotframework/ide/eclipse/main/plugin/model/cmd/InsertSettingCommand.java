/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Optional;

public class InsertSettingCommand extends EditorCommand {

    private final RobotSettingsSection section;

    private Optional<RobotSetting> firstSelectedSetting;

    private final List<RobotKeywordCall> settingsToInsert;

    public InsertSettingCommand(final RobotSettingsSection parent, final Optional<RobotSetting> firstSelectedSetting,
            final RobotKeywordCall[] settingsToInsert) {
        this.section = parent;
        this.firstSelectedSetting = firstSelectedSetting;
        this.settingsToInsert = Arrays.asList(settingsToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {

        int tableIndex = -1;
        int settingsElementsIndex = section.getChildren().size();
        if (firstSelectedSetting.isPresent() && !settingsToInsert.isEmpty()) {
            if (settingsToInsert.get(0).getName().equals(SettingsGroup.METADATA.getName())) {
                tableIndex = section.getMetadataSettings().indexOf(firstSelectedSetting.get());
            } else {
                tableIndex = section.getImportSettings().indexOf(firstSelectedSetting.get());
            }
            settingsElementsIndex = section.getChildren().indexOf(firstSelectedSetting.get());
        }

        int shift = 0;
        for (final RobotKeywordCall call : settingsToInsert) {
            section.insertSetting(call.getName(), call.getComment(), call.getArguments(), tableIndex + shift,
                    settingsElementsIndex + shift);
            shift++;
        }

        eventBroker.post(RobotModelEvents.ROBOT_SETTING_ADDED, section);
    }
}
