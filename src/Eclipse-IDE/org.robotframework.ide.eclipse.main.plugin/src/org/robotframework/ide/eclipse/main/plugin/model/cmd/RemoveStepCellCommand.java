/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.CommonStep;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;


public class RemoveStepCellCommand extends EditorCommand {

    private final RobotKeywordCall call;
    private final int[] columns;
    private CommonStep<?> oldElement;

    public RemoveStepCellCommand(final RobotKeywordCall stepWrapper, final int column) {
        this.call = stepWrapper;
        this.columns = new int[] { column };
    }

    public RemoveStepCellCommand(final RobotKeywordCall stepWrapper, final int[] columns) {
        this.call = stepWrapper;
        this.columns = columns;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final CommonStep<?> linkedElement = (CommonStep<?>) call.getLinkedElement();
        oldElement = RevertTokensCommand.clone(linkedElement);

        final ModelType oldType = linkedElement.getModelType();

        for (int i = columns.length - 1; i >= 0; i--) {
            linkedElement.deleteToken(columns[i]);
        }

        call.resetStored();
        if (linkedElement.getModelType() != oldType) {
            RedEventBroker.using(eventBroker)
                    .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                    .to(call)
                    .send(RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, call.getParent());
        } else {
            RedEventBroker.using(eventBroker).send(RobotModelEvents.ROBOT_KEYWORD_CALL_CELL_CHANGE, call);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new RevertTokensCommand(call, oldElement));
    }
}
