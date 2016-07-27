/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.SettingTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
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
        if (keywordCall.getComment() != null && keywordCall.getComment().equals(newComment)) {
            return;
        }
        keywordCall.resetStored();
        
        updateModelElement();
        
        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, keywordCall);
    }
    
    protected void updateModelElement() {
        final AModelElement<?> linkedElement = keywordCall.getLinkedElement();
        final ModelType modelType = linkedElement.getModelType();
        if (modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW || modelType == ModelType.TEST_CASE_EXECUTABLE_ROW) {
            if (newComment != null) {
                ((RobotExecutableRow<?>) linkedElement).setComment(newComment);
            } else {
                ((RobotExecutableRow<?>) linkedElement).clearComment();
            }
        } else {
            new SettingTableModelUpdater().updateComment(keywordCall.getLinkedElement(), newComment);
        }
    }

    protected RobotKeywordCall getKeywordCall() {
        return keywordCall;
    }

    protected String getNewComment() {
        return newComment;
    }

}
