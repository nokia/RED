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
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertCallToComment extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    private final String name;

    private RobotEmptyLine newEmptyLine;

    public ConvertCallToComment(final IEventBroker eventBroker, final RobotKeywordCall keywordCall, final String name) {
        this.eventBroker = eventBroker;
        this.keywordCall = keywordCall;
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws CommandExecutionException {
        final String oldName = keywordCall.getName();
        if (oldName.equals(name)) {
            return;
        }
        final IRobotCodeHoldingElement parent = keywordCall.getParent();

        final boolean isEmptyCall = oldName.isEmpty();
        final boolean hasNoComments = keywordCall.getComment().isEmpty();

        if (isEmptyCall) {
            if (hasNoComments) {
                keywordCall.setComment(name);
            } else {
                final List<RobotToken> comments = keywordCall.getCommentTokens();
                if (!comments.isEmpty()) {
                    comments.get(0).setText(name);
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

                } else if (parentObject instanceof Task) {
                    final RobotExecutableRow<Task> tempLinked = new RobotExecutableRow<>();
                    tempLinked.getAction().setType(RobotTokenType.TASK_ACTION_NAME);
                    ((Task) parentObject)
                            .replaceElement((RobotExecutableRow<Task>) keywordCall.getLinkedElement(), tempLinked);
                    newLinked = tempLinked;

                } else if (parentObject instanceof UserKeyword) {
                    final RobotExecutableRow<UserKeyword> tempLinked = new RobotExecutableRow<>();
                    tempLinked.getAction().setType(RobotTokenType.KEYWORD_ACTION_NAME);
                    ((UserKeyword) parentObject).replaceElement(
                            (RobotExecutableRow<UserKeyword>) keywordCall.getLinkedElement(), tempLinked);
                    newLinked = tempLinked;
                }
                newEmptyLine = new RobotEmptyLine(parent, newLinked);

                final int index = parent.getChildren().indexOf(keywordCall);
                parent.removeChild(keywordCall);
                parent.getChildren().add(index, newEmptyLine);

                action.setType(RobotTokenType.START_HASH_COMMENT);
                action.setText(name);

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

        RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(newEmptyLine)
                .send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, newEmptyLine, keywordCall));
    }

}
