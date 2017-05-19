/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Joiner;

public class ConvertCommentToSetting extends EditorCommand {

    private RobotKeywordCall settingCall;

    private final RobotKeywordCall commentCall;

    private final String newName;

    public ConvertCommentToSetting(final IEventBroker eventBroker, final RobotKeywordCall commentCall,
            final String name) {
        this.eventBroker = eventBroker;
        this.settingCall = null;
        this.commentCall = commentCall;
        this.newName = name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final Optional<List<RobotToken>> commentTokens = commentCall.getCommentTokens();
        if (commentTokens.isPresent()) {
            final List<RobotToken> comments = commentTokens.get();
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
            parent.removeChild(commentCall);
            final RobotDefinitionSetting setting = parent.createSetting(0, newName,
                    newArgs.stream().map(a -> a.getText()).collect(Collectors.toList()),
                    Joiner.on(" | ").join(newComments.stream().map(c -> c.getText()).collect(Collectors.toList())));

            setting.getLinkedElement().getDeclaration().setFilePosition(firstToken.getFilePosition());
            setting.getLinkedElement().getDeclaration().getTypes().remove(RobotTokenType.UNKNOWN);
            setting.getLinkedElement().getDeclaration().markAsDirty();

            settingCall = setting;

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_COMMENT_CHANGE, parent);
        }
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        final List<EditorCommand> undoCommands = new ArrayList<>(1);
        undoCommands.add(new ReplaceRobotKeywordCallCommand(eventBroker, settingCall, commentCall));
        return undoCommands;
    }

    private static boolean looksLikeComment(final String text) {
        return (text != null) && text.trim().startsWith("#");
    }

}
