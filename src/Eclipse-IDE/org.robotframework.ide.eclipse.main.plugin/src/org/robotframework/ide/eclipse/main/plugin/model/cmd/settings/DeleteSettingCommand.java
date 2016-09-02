/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteKeywordCallCommand;

public class DeleteSettingCommand extends DeleteKeywordCallCommand {

    public DeleteSettingCommand(final List<RobotSetting> settingsToRemove) {
        super(settingsToRemove, RobotModelEvents.ROBOT_SETTING_REMOVED);
    }
}
