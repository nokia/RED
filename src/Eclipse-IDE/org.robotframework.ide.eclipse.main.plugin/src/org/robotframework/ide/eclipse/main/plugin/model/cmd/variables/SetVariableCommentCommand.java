/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import org.rf.ide.core.testdata.model.table.variables.AVariable;
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
        if (newComment.isEmpty()) {
            final AVariable holder = (AVariable) variable.getLinkedElement();
            holder.clearComment();
        }
        
        // variable.setComment(newComment);

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_COMMENT_CHANGE, variable);
    }
}
