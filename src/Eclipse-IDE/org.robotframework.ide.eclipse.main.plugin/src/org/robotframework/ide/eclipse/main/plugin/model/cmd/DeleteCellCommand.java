/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;
import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.settings.SetSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.KeywordCallsTableValuesChangingCommandsCollector;
import org.robotframework.services.event.RedEventBroker;

public class DeleteCellCommand extends EditorCommand {

    private final RobotKeywordCall call;
    private final int position;
    private EditorCommand executed;

    public DeleteCellCommand(final RobotKeywordCall call, final int position) {
        this.call = call;
        this.position = position;
    }

    @Override
    public void execute() throws CommandExecutionException {

        final int callIndex = call.getIndex();
        final IRobotCodeHoldingElement parent = call.getParent();
        final Optional<? extends EditorCommand> command;
        final String topic;
        if (call instanceof RobotSetting) {
            final RobotSetting selectedSetting = (RobotSetting) call;
            topic = RobotModelEvents.ROBOT_SETTING_CHANGED;

            command = position > 0 ? Optional.of(new SetSettingArgumentCommand(selectedSetting, position - 1, null))
                    : Optional.empty();
        } else {
            topic = RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED;
            command = new KeywordCallsTableValuesChangingCommandsCollector().collect(call, null, position);
        }

        if (command.isPresent()) {
            executed = command.get();
            executed.setEventBroker(eventBroker);
            executed.execute();
        }

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(parent.getChildren().get(callIndex)).send(topic, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return executed.getUndoCommands();
    }

}
