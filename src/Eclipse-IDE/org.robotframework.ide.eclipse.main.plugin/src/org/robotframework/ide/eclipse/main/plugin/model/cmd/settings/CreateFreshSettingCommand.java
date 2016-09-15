/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshSettingCommand extends EditorCommand {

    private final RobotSettingsSection settingsSection;

    private final String keywordName;

    private final List<String> args;

    private final String comment;

    private RobotSetting newSetting;

    public CreateFreshSettingCommand(final RobotSettingsSection settingsSection, final String keywordName,
            final List<String> args) {
        this(settingsSection, keywordName, args, "");
    }

    public CreateFreshSettingCommand(final RobotSettingsSection settingsSection, final String keywordName,
            final List<String> args, final String comment) {
        this.settingsSection = settingsSection;
        this.keywordName = keywordName;
        this.args = args;
        this.comment = comment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        newSetting = settingsSection.createSetting(keywordName, comment, args.toArray(new String[0]));

        eventBroker.send(RobotModelEvents.ROBOT_SETTING_ADDED, settingsSection);
    }

    @Override
    public EditorCommand getUndoCommand() {
        return newUndoCommand(new DeleteSettingCommand(newArrayList(newSetting)));
    }
}
