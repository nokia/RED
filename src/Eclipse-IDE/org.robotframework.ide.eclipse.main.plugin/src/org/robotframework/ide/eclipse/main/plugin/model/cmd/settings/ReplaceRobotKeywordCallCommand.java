/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ReplaceRobotKeywordCallCommand extends EditorCommand {

    private final RobotSetting oldSetting, newSetting;

    public ReplaceRobotKeywordCallCommand(final IEventBroker eventBroker, final RobotSetting oldSetting,
            final RobotSetting newSettting) {
        this.eventBroker = eventBroker;
        this.oldSetting = oldSetting;
        this.newSetting = newSettting;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final IRobotCodeHoldingElement parent = oldSetting.getParent();
        final int index = oldSetting.getIndex();
        if (index > -1) {
            parent.removeChild(oldSetting);
            final RobotSettingsSection settingsSection = (RobotSettingsSection) parent;
            settingsSection.insertSetting(newSetting, index);
            newSetting.resetStored();

            RedEventBroker.using(eventBroker)
                    .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                    .to(newSetting)
                    .send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, newSetting.getParent());
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, newSetting, oldSetting));
    }

}
