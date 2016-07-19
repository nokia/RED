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
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordCallDownCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    public MoveKeywordCallDownCommand(final RobotKeywordCall keywordCall) {
        this.keywordCall = keywordCall;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
            final IRobotCodeHoldingElement codeElement = keywordCall.getParent();
            final int size = codeElement.getChildren().size();
            final int index = codeElement.getChildren().indexOf(keywordCall);
            if (index == size - 1) {
                // lets try to move the element down from here
                final int defsSize = codeElement.getParent().getChildren().size();
                final int indexOfElement = codeElement.getParent().getChildren().indexOf(codeElement);
                if (indexOfElement == defsSize - 1) {
                    return;
                }
                final IRobotCodeHoldingElement targetElement = (IRobotCodeHoldingElement) codeElement.getParent()
                        .getChildren()
                        .get(indexOfElement + 1);

                codeElement.getChildren().remove(index);
                targetElement.getChildren().add(0, keywordCall);
                keywordCall.setParent(targetElement);

                eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, targetElement);
                eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, codeElement);

                return;
            }
            Collections.swap(codeElement.getChildren(), index, index + 1);

            final Object linkedElement = codeElement.getLinkedElement();
            if (linkedElement != null && linkedElement instanceof UserKeyword) {
                ((UserKeyword) linkedElement)
                        .moveDownExecutableRow((RobotExecutableRow<UserKeyword>) keywordCall.getLinkedElement());
            }

            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, codeElement);
        }
    }

}
