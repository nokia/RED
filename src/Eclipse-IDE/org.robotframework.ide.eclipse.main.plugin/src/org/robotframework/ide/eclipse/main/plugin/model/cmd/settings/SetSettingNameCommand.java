/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.LibraryImport;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.rf.ide.core.testdata.model.table.setting.ResourceImport;
import org.rf.ide.core.testdata.model.table.setting.VariablesImport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetSettingNameCommand extends EditorCommand {

    private final RobotSetting setting;

    private final String oldName;

    private final String newName;

    public SetSettingNameCommand(final RobotSetting setting, final String newName) {
        this.setting = setting;
        this.newName = newName;
        this.oldName = setting.getName();
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (setting.getName().equals(newName)) {
            return;
        }
        final SettingTable table = setting.getParent().getLinkedElement();
        new SettingTableModelUpdater().remove(table, setting.getLinkedElement());
        final AModelElement<?> newSetting = new SettingTableModelUpdater().create(
                table, setting.getIndex(), newName, setting.getComment(),
                setting.getArguments());
        setting.setLinkedElement(newSetting);

        if (newSetting instanceof LibraryImport) {
            setting.setGroup(SettingsGroup.LIBRARIES);
        } else if (newSetting instanceof ResourceImport) {
            setting.setGroup(SettingsGroup.RESOURCES);
        } else if (newSetting instanceof VariablesImport) {
            setting.setGroup(SettingsGroup.VARIABLES);
        } else if (newSetting instanceof Metadata) {
            setting.setGroup(SettingsGroup.METADATA);
        } else {
            setting.setGroup(SettingsGroup.NO_GROUP);
        }
        setting.resetStored();

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_NAME_CHANGE, setting);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetSettingNameCommand(setting, oldName));
    }
}
