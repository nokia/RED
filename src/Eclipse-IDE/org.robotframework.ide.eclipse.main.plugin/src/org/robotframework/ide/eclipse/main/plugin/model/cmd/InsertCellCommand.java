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

    private final RobotSetting oldSetting;

    private RobotKeywordCall newSetting;
    private final int position;

    public InsertCellCommand(final RobotSetting newSetting, final int position) {
        this.oldSetting = newSetting;
        this.position = position;
    }

    @Override
    public void execute() throws CommandExecutionException {
        newSetting = oldSetting.insertEmptyCellAt(position);

        RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(newSetting)
                .send(RobotModelEvents.ROBOT_SETTING_CHANGED, newSetting.getParent());
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(oldSetting.equals(newSetting) ? new DeleteCellCommand(newSetting, position)
                : new ReplaceRobotKeywordCallCommand(eventBroker, newSetting, oldSetting));
    }

}
