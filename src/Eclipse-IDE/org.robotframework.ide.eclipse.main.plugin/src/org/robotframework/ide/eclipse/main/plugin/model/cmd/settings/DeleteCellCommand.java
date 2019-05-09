/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd.settings;

import java.util.List;
import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class DeleteCellCommand extends EditorCommand {

    private final RobotSetting setting;
    private final int position;
    private EditorCommand executed;

    public DeleteCellCommand(final RobotSetting setting, final int position) {
        this.setting = setting;
        this.position = position;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final int callIndex = setting.getIndex();
        final IRobotCodeHoldingElement parent = setting.getParent();
        final Optional<? extends EditorCommand> command;

        final RobotSetting selectedSetting = setting;
        command = position > 0 ? Optional.of(new SetSettingArgumentCommand(selectedSetting, position - 1, null))
                : Optional.empty();

        if (command.isPresent()) {
            executed = command.get();
            executed.setEventBroker(eventBroker);
            executed.execute();
        }

        RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(parent.getChildren().get(callIndex))
                .send(RobotModelEvents.ROBOT_SETTING_CHANGED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return executed.getUndoCommands();
    }

}
