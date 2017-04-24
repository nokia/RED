/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService.ConversionFromCommentCommand;
import org.robotframework.services.event.RedEventBroker;

public class ConvertSettingToComment extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    private RobotKeywordCall settingCall;

    private final String newName;

    private final String oldName;

    public ConvertSettingToComment(final IEventBroker eventBroker, final RobotKeywordCall keywordCall,
            final String name) {
        this.eventBroker = eventBroker;
        this.keywordCall = keywordCall;
        this.newName = name;
        this.oldName = keywordCall.getLinkedElement().getDeclaration().getRaw();
        this.settingCall = null;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (oldName.equals(newName)) {
            return;
        }

        final List<RobotToken> tokens = keywordCall.getLinkedElement().getElementTokens();

        if (tokens.isEmpty()) {
            return;
        }

        final RobotExecutableRow<TestCase> newLinked = new RobotExecutableRow<>();
        newLinked.getAction().setType(RobotTokenType.TEST_CASE_ACTION_NAME);

        final RobotToken first = tokens.get(0);
        first.setType(RobotTokenType.START_HASH_COMMENT);
        first.setText(newName);
        newLinked.addCommentPart(first);

        for (int i = 1; i < tokens.size(); i++) {
            final RobotToken token = tokens.get(i);
            token.setType(RobotTokenType.COMMENT_CONTINUE);
            newLinked.addCommentPart(token);
        }

        final RobotCodeHoldingElement<?> parent = (RobotCodeHoldingElement<?>) keywordCall.getParent();

        final int index = keywordCall.getIndex();
        parent.removeChild(keywordCall);
        final RobotKeywordCall newCall = new RobotKeywordCall(parent, newLinked);
        parent.insertKeywordCall(index, newCall);

        settingCall = newCall;

        RedEventBroker.using(eventBroker).additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(newCall).send(
                RobotModelEvents.ROBOT_KEYWORD_CALL_CONVERTED, parent);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        final List<EditorCommand> undoCommands = new ArrayList<>(1);
        undoCommands.add(new ConversionFromCommentCommand(settingCall, oldName, 0));
        return undoCommands;
    }

}
