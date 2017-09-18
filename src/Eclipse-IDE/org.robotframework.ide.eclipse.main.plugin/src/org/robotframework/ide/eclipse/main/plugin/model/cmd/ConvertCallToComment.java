/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertCallToComment extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    private final String newName;

    private final String oldName;

    public ConvertCallToComment(final IEventBroker eventBroker, final RobotKeywordCall keywordCall, final String name) {
        this.eventBroker = eventBroker;
        this.keywordCall = keywordCall;
        this.newName = name;
        this.oldName = keywordCall.getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws CommandExecutionException {
        if (oldName.equals(newName)) {
            return;
        }

        final boolean isEmptyCall = oldName.isEmpty();
        final boolean hasNoComments = keywordCall.getComment().isEmpty();

        if (isEmptyCall) {
            if (hasNoComments) {
                keywordCall.setComment(newName);
            } else {
                final List<RobotToken> comments = keywordCall.getCommentTokens();
                if (!comments.isEmpty()) {
                    comments.get(0).setText(newName);
                }
            }
        } else {
            final Optional<RobotToken> actionToken = keywordCall.getAction();
            if (actionToken.isPresent()) {
                final RobotToken action = actionToken.get();
                final List<RobotToken> arguments = keywordCall.getArgumentTokens();
                final List<RobotToken> comments = keywordCall.getCommentTokens();
                final Object parentObject = keywordCall.getLinkedElement().getParent();

                RobotExecutableRow<?> newLinked = null;
                if (parentObject instanceof TestCase) {
                    final RobotExecutableRow<TestCase> tempLinked = new RobotExecutableRow<>();
                    tempLinked.getAction().setType(RobotTokenType.TEST_CASE_ACTION_NAME);
                    ((TestCase) parentObject).replaceElement(
                            (RobotExecutableRow<TestCase>) keywordCall.getLinkedElement(), tempLinked);
                    newLinked = tempLinked;
                } else if (parentObject instanceof UserKeyword) {
                    final RobotExecutableRow<UserKeyword> tempLinked = new RobotExecutableRow<>();
                    tempLinked.getAction().setType(RobotTokenType.KEYWORD_ACTION_NAME);
                    ((UserKeyword) parentObject).replaceElement(
                            (RobotExecutableRow<UserKeyword>) keywordCall.getLinkedElement(), tempLinked);
                    newLinked = tempLinked;
                }

                action.setType(RobotTokenType.START_HASH_COMMENT);
                action.setText(newName);

                newLinked.addCommentPart(action);
                for (int i = 0; i < arguments.size(); i++) {
                    final RobotToken argument = arguments.get(i);
                    argument.setType(RobotTokenType.COMMENT_CONTINUE);
                    newLinked.addCommentPart(argument);
                }
                for (int i = 0; i < comments.size(); i++) {
                    final RobotToken comment = comments.get(i);
                    comment.setType(RobotTokenType.COMMENT_CONTINUE);
                    newLinked.addCommentPart(comment);
                }
                keywordCall.setLinkedElement(newLinked);
            }
        }
        keywordCall.resetStored();

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(keywordCall).send(
                RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, keywordCall.getParent());
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ConvertCommentToCall(eventBroker, keywordCall, oldName));
    }

}
