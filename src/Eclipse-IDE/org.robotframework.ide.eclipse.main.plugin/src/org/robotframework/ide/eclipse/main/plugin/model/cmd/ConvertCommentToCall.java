/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertCommentToCall extends EditorCommand {

    private final RobotKeywordCall commentCall;

    private final String newName;

    private final String oldName;

    public ConvertCommentToCall(final IEventBroker eventBroker, final RobotKeywordCall commentCall, final String name) {
        this.eventBroker = eventBroker;
        this.commentCall = commentCall;
        this.newName = name;
        this.oldName = commentCall.getCommentTokens().get(0).getText();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws CommandExecutionException {
        final List<RobotToken> comments = commentCall.getCommentTokens();
        if (!comments.isEmpty()) {
            final RobotToken firstToken = comments.get(0);
            final Object parentObject = commentCall.getLinkedElement().getParent();

            RobotExecutableRow<?> newLinked = null;
            IRobotTokenType typeToUse = null;
            if (parentObject instanceof TestCase) {
                final RobotExecutableRow<TestCase> tempLinked = new RobotExecutableRow<>();
                ((TestCase) parentObject).replaceElement(
                        (RobotExecutableRow<TestCase>) commentCall.getLinkedElement(), tempLinked);
                typeToUse = RobotTokenType.TEST_CASE_ACTION_ARGUMENT;
                firstToken.setType(RobotTokenType.TEST_CASE_ACTION_NAME);
                newLinked = tempLinked;
            } else if (parentObject instanceof UserKeyword) {
                final RobotExecutableRow<UserKeyword> tempLinked = new RobotExecutableRow<>();
                ((UserKeyword) parentObject)
                        .replaceElement((RobotExecutableRow<UserKeyword>) commentCall.getLinkedElement(), tempLinked);
                typeToUse = RobotTokenType.KEYWORD_ACTION_ARGUMENT;
                firstToken.setType(RobotTokenType.KEYWORD_ACTION_NAME);
                newLinked = tempLinked;
            }

            newLinked.setAction(firstToken);
            firstToken.setText(newName);

            boolean foundComment = false;
            for (int i = 1; i < comments.size(); i++) {
                final RobotToken token = comments.get(i);
                if (foundComment) {
                    token.setType(RobotTokenType.COMMENT_CONTINUE);
                    newLinked.addCommentPart(token);
                } else {
                    if (looksLikeComment(token.getText())) {
                        token.setType(RobotTokenType.START_HASH_COMMENT);
                        newLinked.addCommentPart(token);
                        foundComment = true;
                    } else {
                        token.setType(typeToUse);
                        newLinked.addArgument(token);
                    }
                }
            }

            commentCall.setLinkedElement(newLinked);
            commentCall.resetStored();

            RedEventBroker.using(eventBroker)
                    .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                    .to(commentCall)
                    .send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, commentCall.getParent());
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ConvertCallToComment(eventBroker, commentCall, oldName));
    }

    private static boolean looksLikeComment(final String text) {
        return (text != null) && text.trim().startsWith("#");
    }

}
