/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;

import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordCallDownCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    public MoveKeywordCallDownCommand(final RobotKeywordCall keywordCall) {
        this.keywordCall = keywordCall;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() throws CommandExecutionException {

        if (!keywordCall.isExecutable()) {
            throw new IllegalStateException("Unable to move non-executable rows");
        }

        final RobotKeywordDefinition keywordDefinition = (RobotKeywordDefinition) keywordCall.getParent();
        final int size = keywordDefinition.getChildren().size();
        final int index = keywordCall.getIndex();

        if (index == size - 1) {
            // lets try to move the element down from here
            final int defsSize = keywordDefinition.getParent().getChildren().size();
            final int indexOfElement = keywordDefinition.getIndex();
            if (indexOfElement == defsSize - 1) {
                // no place to move it further down
                return;
            }
            final RobotKeywordDefinition nextKeywordDefinition = keywordDefinition.getParent()
                    .getChildren()
                    .get(indexOfElement + 1);

            keywordDefinition.getChildren().remove(keywordCall);
            keywordDefinition.getLinkedElement()
                    .removeExecutableRow((RobotExecutableRow<UserKeyword>) keywordCall.getLinkedElement());

            nextKeywordDefinition.insertKeywordCall(findIndex(nextKeywordDefinition), keywordCall);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, nextKeywordDefinition);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, keywordDefinition);
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, nextKeywordDefinition);

        } else {
            Collections.swap(keywordDefinition.getChildren(), index, index + 1);

            final UserKeyword linkedElement = keywordDefinition.getLinkedElement();
            final RobotExecutableRow<UserKeyword> linkedCall = (RobotExecutableRow<UserKeyword>) keywordCall
                    .getLinkedElement();
            linkedElement.moveDownExecutableRow(linkedCall);

            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, keywordDefinition);
        }
    }

    private int findIndex(final RobotKeywordDefinition nextKeywordDefinition) {
        int i = 0;
        for (final RobotKeywordCall call : nextKeywordDefinition.getChildren()) {
            if (call.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
                break;
            }
            i++;
        }
        return i;
    }

}
