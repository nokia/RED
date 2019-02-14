/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotEmptyLine;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertCommentToCall extends EditorCommand {

    private final RobotEmptyLine emptyLine;

    private final String name;

    private RobotKeywordCall newCall;

    public ConvertCommentToCall(final IEventBroker eventBroker, final RobotEmptyLine emptyLine, final String name) {
        this.eventBroker = eventBroker;
        this.emptyLine = emptyLine;
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws CommandExecutionException {
        final List<RobotToken> comments = emptyLine.getCommentTokens();
        if (!comments.isEmpty()) {
            final IRobotCodeHoldingElement parent = emptyLine.getParent();
            final Object parentObject = emptyLine.getLinkedElement().getParent();

            final RobotToken firstToken = comments.get(0).copy();
            RobotExecutableRow<?> newLinked = null;
            IRobotTokenType typeToUse = null;
            if (parentObject instanceof TestCase) {
                final RobotExecutableRow<TestCase> tempLinked = new RobotExecutableRow<>();
                ((TestCase) parentObject).replaceElement((RobotEmptyRow<TestCase>) emptyLine.getLinkedElement(),
                        tempLinked);
                typeToUse = RobotTokenType.TEST_CASE_ACTION_ARGUMENT;
                firstToken.setType(RobotTokenType.TEST_CASE_ACTION_NAME);
                newLinked = tempLinked;

            } else if (parentObject instanceof Task) {
                final RobotExecutableRow<Task> tempLinked = new RobotExecutableRow<>();
                ((Task) parentObject).replaceElement((RobotEmptyRow<Task>) emptyLine.getLinkedElement(), tempLinked);
                typeToUse = RobotTokenType.TASK_ACTION_ARGUMENT;
                firstToken.setType(RobotTokenType.TASK_ACTION_NAME);
                newLinked = tempLinked;

            } else if (parentObject instanceof UserKeyword) {
                final RobotExecutableRow<UserKeyword> tempLinked = new RobotExecutableRow<>();
                ((UserKeyword) parentObject).replaceElement((RobotEmptyRow<UserKeyword>) emptyLine.getLinkedElement(),
                        tempLinked);
                typeToUse = RobotTokenType.KEYWORD_ACTION_ARGUMENT;
                firstToken.setType(RobotTokenType.KEYWORD_ACTION_NAME);
                newLinked = tempLinked;
            }
            newCall = new RobotKeywordCall(parent, newLinked);

            final int index = parent.getChildren().indexOf(emptyLine);
            parent.removeChild(emptyLine);
            parent.getChildren().add(index, newCall);

            newLinked.setAction(firstToken);
            firstToken.setText(name);

            boolean foundComment = false;
            for (int i = 1; i < comments.size(); i++) {
                final RobotToken token = comments.get(i);
                if (foundComment) {
                    token.setType(RobotTokenType.COMMENT_CONTINUE);
                    newLinked.addCommentPart(token.copy());
                } else {
                    if (looksLikeComment(token.getText())) {
                        token.setType(RobotTokenType.START_HASH_COMMENT);
                        newLinked.addCommentPart(token.copy());
                        foundComment = true;
                    } else {
                        token.setType(typeToUse);
                        newLinked.addArgument(token.copy());
                    }
                }
            }

            RedEventBroker.using(eventBroker)
                    .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                    .to(newCall)
                    .send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, parent);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, newCall, emptyLine));
    }

    private static boolean looksLikeComment(final String text) {
        return text != null && text.trim().startsWith("#");
    }

}
