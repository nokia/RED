/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetCaseCommentCommand extends EditorCommand {

    private final RobotCase testCase;
    private final String newComment;

    public SetCaseCommentCommand(final RobotCase testCase, final String comment) {
        this.testCase = testCase;
        this.newComment = comment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (testCase.getComment().equals(newComment)) {
            return;
        }
        // testCase.setComment(newComment);

        // FIXME: don't need this command at all, since Test Case has no comment inside
        eventBroker.send(RobotModelEvents.ROBOT_CASE_COMMENT_CHANGE, testCase);
    }
}
