/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class InsertCellCommand extends EditorCommand {

    private final RobotKeywordCall oldCall;
    private RobotKeywordCall newCall;
    private final int position;
    private final String newValue;

    public InsertCellCommand(final RobotKeywordCall call, final int position,
            final String newValue) {
        oldCall = call;
        this.position = position;
        this.newValue = newValue == null ? "" : newValue;
    }

    @Override
    public void execute() throws CommandExecutionException {

        newCall = oldCall.insertCellAt(position, newValue);

        final String topic = (newCall instanceof RobotSetting) ? RobotModelEvents.ROBOT_SETTING_CHANGED
                : RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED;

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newCall).send(
                topic, newCall.getParent());
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(oldCall.equals(newCall) ? new DeleteCellCommand(newCall, position)
                : new ReplaceRobotKeywordCallCommand(eventBroker, newCall, oldCall));
    }

}
