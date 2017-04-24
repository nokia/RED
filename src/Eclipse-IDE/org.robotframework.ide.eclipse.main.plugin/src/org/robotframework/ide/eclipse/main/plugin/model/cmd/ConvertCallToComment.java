/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService.ConversionFromCommentCommand;
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
                final Optional<List<RobotToken>> commentTokens = keywordCall.getCommentTokens();
                if (commentTokens.isPresent()) {
                    final List<RobotToken> comments = commentTokens.get();
                    comments.get(0).setText(newName);
                }
            }
        } else {
            final Optional<RobotToken> actionToken = keywordCall.getAction();
            if (actionToken.isPresent()) {
                final RobotToken action = actionToken.get();
                final List<RobotToken> arguments = keywordCall.getArgumentTokens().get();
                final List<RobotToken> comments = keywordCall.getCommentTokens().get();
                final RobotExecutableRow<TestCase> newLinked = new RobotExecutableRow<>();
                newLinked.getAction().setType(RobotTokenType.TEST_CASE_ACTION_NAME);
                newLinked.setParent((TestCase) keywordCall.getLinkedElement().getParent());
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
                RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, keywordCall.getParent());
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        final List<EditorCommand> undoCommands = new ArrayList<>(1);
        undoCommands.add(new ConversionFromCommentCommand(keywordCall, oldName, 0));
        return undoCommands;
    }

}
