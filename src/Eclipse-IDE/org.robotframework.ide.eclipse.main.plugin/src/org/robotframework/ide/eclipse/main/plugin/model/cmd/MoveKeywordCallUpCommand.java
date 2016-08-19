/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordCallUpCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    public MoveKeywordCallUpCommand(final RobotKeywordCall keywordCall) {
        this.keywordCall = keywordCall;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws CommandExecutionException {

        if (!keywordCall.isExecutable()) {
            throw new IllegalStateException("Unable to move non-executable rows");
        }

        final RobotKeywordDefinition keywordDefinition = (RobotKeywordDefinition) keywordCall.getParent();
        final int index = keywordCall.getIndex();

        if (index == 0 || !keywordDefinition.getChildren().get(index - 1).isExecutable()) {
            // lets try to the element up from here
            final int indexOfElement = keywordDefinition.getIndex();
            if (indexOfElement == 0) {
                // no place to move it further up
                return;
            }
            final RobotKeywordDefinition prevKeywordDefinition = keywordDefinition.getParent()
                    .getChildren()
                    .get(indexOfElement - 1);

            keywordDefinition.getChildren().remove(keywordCall);
            keywordDefinition.getLinkedElement()
                    .removeExecutableRow((RobotExecutableRow<UserKeyword>) keywordCall.getLinkedElement());

            prevKeywordDefinition.insertKeywordCall(prevKeywordDefinition.getChildren().size(), keywordCall);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, prevKeywordDefinition);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, keywordDefinition);
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, prevKeywordDefinition);
        } else {
            Collections.swap(keywordDefinition.getChildren(), index, index - 1);

            final UserKeyword linkedElement = keywordDefinition.getLinkedElement();
            final RobotExecutableRow<UserKeyword> linkedCall = (RobotExecutableRow<UserKeyword>) keywordCall
                    .getLinkedElement();
            linkedElement.moveUpExecutableRow(linkedCall);

            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, keywordDefinition);
        }
    }
}
