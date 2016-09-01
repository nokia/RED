/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetVariableCommentCommand extends EditorCommand {

    private final RobotVariable variable;
    private final String newComment;

    public SetVariableCommentCommand(final RobotVariable variable, final String newComment) {
        this.variable = variable;
        this.newComment = newComment == null ? "" : newComment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variable.getComment().equals(newComment)) {
            return;
        }
        CommentServiceHandler.update(variable.getLinkedElement(), ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, newComment);

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_COMMENT_CHANGE, variable);
    }
}
