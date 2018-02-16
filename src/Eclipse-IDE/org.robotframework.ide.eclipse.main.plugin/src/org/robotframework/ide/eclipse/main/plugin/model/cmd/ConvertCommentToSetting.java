/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertCommentToSetting extends EditorCommand {

    private final RobotKeywordCall commentCall;

    private final String newName;

    private RobotDefinitionSetting settingCall;

    public ConvertCommentToSetting(final IEventBroker eventBroker, final RobotKeywordCall commentCall,
            final String name) {
        this.eventBroker = eventBroker;
        this.commentCall = commentCall;
        this.newName = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<RobotToken> comments = commentCall.getCommentTokens();
        if (!comments.isEmpty()) {
            final RobotToken firstToken = comments.get(0);

            final List<RobotToken> newArgs = new ArrayList<>();
            final List<RobotToken> newComments = new ArrayList<>();

            boolean foundComment = false;
            for (int i = 1; i < comments.size(); i++) {
                final RobotToken token = comments.get(i);
                if (foundComment) {
                    newComments.add(token);
                } else {
                    if (looksLikeComment(token.getText())) {
                        newComments.add(token);
                        foundComment = true;
                    } else {
                        newArgs.add(token);
                    }
                }
            }

            final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) commentCall.getParent();
            final int index = commentCall.getIndex();
            parent.removeChild(commentCall);
            settingCall = parent.createSetting(index, newName,
                    newArgs.stream().map(RobotToken::getText).collect(Collectors.toList()),
                    newComments.stream().map(RobotToken::getText).collect(Collectors.joining(" | ")));

            settingCall.getLinkedElement().getDeclaration().setFilePosition(firstToken.getFilePosition());
            settingCall.getLinkedElement().getDeclaration().getTypes().remove(RobotTokenType.UNKNOWN);
            settingCall.getLinkedElement().getDeclaration().markAsDirty();

            RedEventBroker.using(eventBroker)
                    .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                    .to(settingCall)
                    .send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, parent);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new ReplaceRobotKeywordCallCommand(eventBroker, settingCall, commentCall));
    }

    private static boolean looksLikeComment(final String text) {
        return (text != null) && text.trim().startsWith("#");
    }

}
