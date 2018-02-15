/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertSettingToSetting extends EditorCommand {

    private final RobotDefinitionSetting setting;

    private final String settingName;

    private RobotDefinitionSetting newSetting;

    public ConvertSettingToSetting(final IEventBroker eventBroker, final RobotDefinitionSetting setting,
            final String settingName) {
        this.eventBroker = eventBroker;
        this.setting = setting;
        this.settingName = settingName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) setting.getParent();

        final int index = setting.getIndex();
        parent.removeChild(setting);
        newSetting = parent.createSetting(index, settingName, setting.getArguments(), setting.getComment());

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newSetting).send(
                RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, newSetting, setting));
    }
}
