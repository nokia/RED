/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetVariableCommentCommand extends EditorCommand {

    private final RobotVariable variable;
    private final String newComment;

    public SetVariableCommentCommand(final RobotVariable variable, final String newComment) {
        this.variable = variable;
        this.newComment = newComment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variable.getComment().equals(newComment)) {
            return;
        }
        // variable.setComment(newComment);

        // it has to be send, not posted
        // otherwise it is not possible to traverse between cells, because the cell
        // is traversed and then main thread has to handle incoming posted event which
        // closes currently active cell editor
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_COMMENT_CHANGE, variable);
    }
}
