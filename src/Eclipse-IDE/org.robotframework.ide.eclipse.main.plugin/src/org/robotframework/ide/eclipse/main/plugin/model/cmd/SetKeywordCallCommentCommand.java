/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Objects;

import org.rf.ide.core.testdata.model.ICommentHolder;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler;
import org.rf.ide.core.testdata.model.presenter.CommentServiceHandler.ETokenSeparator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordCallCommentCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;
    private final String newComment;

    public SetKeywordCallCommentCommand(final RobotKeywordCall keywordCall, final String comment) {
        this.keywordCall = keywordCall;
        this.newComment = comment;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (Objects.equals(keywordCall.getComment(), newComment)) {
            return;
        }
        CommentServiceHandler.update((ICommentHolder) keywordCall.getLinkedElement(),
                ETokenSeparator.PIPE_WRAPPED_WITH_SPACE, newComment);
        keywordCall.resetStored();
        
        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, keywordCall);
    }
}
