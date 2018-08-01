/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.SettingTable;
import org.rf.ide.core.testdata.model.table.setting.AImported;
import org.rf.ide.core.testdata.model.table.setting.Metadata;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting.SettingsGroup;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class MoveSettingDownCommand extends EditorCommand {

    private final RobotSetting setting;

    public MoveSettingDownCommand(final RobotSetting setting) {
        this.setting = setting;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotSettingsSection section = setting.getParent();
        final int currentIndex = section.getChildren().indexOf(setting);
        final int downIndex = findNextIndexDown(currentIndex, setting);
        if (downIndex >= section.getChildren().size()) {
            return;
        }
        Collections.swap(section.getChildren(), currentIndex, downIndex);

        final ARobotSectionTable linkedElement = section.getLinkedElement();
        if (linkedElement != null && linkedElement instanceof SettingTable) {
            if (setting.getGroup() == SettingsGroup.METADATA) {
                ((SettingTable) linkedElement).moveDownMetadata((Metadata) setting.getLinkedElement());
            } else {
                ((SettingTable) linkedElement).moveDownImported((AImported) setting.getLinkedElement());
            }
        }

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(setting.getGroup())
                .send(RobotModelEvents.ROBOT_SETTING_MOVED, section);
    }

    private int findNextIndexDown(final int currentIndex, final RobotSetting setting) {
        final List<RobotKeywordCall> children = setting.getParent().getChildren();
        for (int i = currentIndex + 1; i < children.size(); i++) {
            if (getSettingsGroupSet(((RobotSetting) children.get(i)).getGroup()).equals(
                    getSettingsGroupSet(setting.getGroup()))) {
                return i;
            }
        }
        return children.size();
    }

    private EnumSet<SettingsGroup> getSettingsGroupSet(final SettingsGroup group) {
        final EnumSet<SettingsGroup> imports = SettingsGroup.getImportsGroupsSet();
        if (imports.contains(group)) {
            return imports;
        }
        return EnumSet.of(group);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new MoveSettingUpCommand(setting));
    }
}
