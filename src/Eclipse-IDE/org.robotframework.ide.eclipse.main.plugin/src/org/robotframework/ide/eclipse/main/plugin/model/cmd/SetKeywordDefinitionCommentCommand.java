/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordDefinitionCommentCommand extends EditorCommand {

    private final RobotKeywordDefinition keyword;
    private final String newComment;

    public SetKeywordDefinitionCommentCommand(final RobotKeywordDefinition keyword, final String comment) {
        this.keyword = keyword;
        this.newComment = comment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (keyword.getComment().equals(newComment)) {
            return;
        }
        
        //User Keyword has no comment inside
        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_COMMENT_CHANGE, keyword);
    }
}
