/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.model.table.CommonStep;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;


public class UpdateStepCellCommand extends EditorCommand {

    private final RobotKeywordCall call;
    private final String value;
    private final int column;

    private CommonStep<?> oldElement;

    public UpdateStepCellCommand(final RobotKeywordCall stepWrapper, final String value, final int column) {
        this.call = stepWrapper;
        this.value = value;
        this.column = column;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final CommonStep<?> linkedElement = (CommonStep<?>) call.getLinkedElement();
        oldElement = RevertTokensCommand.clone(linkedElement);

        if (value == null) {
            linkedElement.deleteToken(column);
        } else {
            linkedElement.updateToken(column, value);
        }
        call.resetStored();

        RedEventBroker.using(eventBroker).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new RevertTokensCommand(call, oldElement));
    }
}
