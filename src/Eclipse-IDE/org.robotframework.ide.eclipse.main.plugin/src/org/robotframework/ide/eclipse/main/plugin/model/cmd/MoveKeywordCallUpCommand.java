/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;

import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


public class MoveKeywordCallUpCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    public MoveKeywordCallUpCommand(final RobotKeywordCall keywordCall) {
        this.keywordCall = keywordCall;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final IRobotCodeHoldingElement codeElement = keywordCall.getParent();
        final int index = codeElement.getChildren().indexOf(keywordCall);
        if (index == 0) {
            // lets try to the element up from here
            final int indexOfElement = codeElement.getParent().getChildren().indexOf(codeElement);
            if (indexOfElement == 0) {
                return;
            }
            final IRobotCodeHoldingElement targetElement = (IRobotCodeHoldingElement) codeElement.getParent()
                    .getChildren().get(indexOfElement - 1);

            codeElement.getChildren().remove(index);
            targetElement.getChildren().add(keywordCall);
            keywordCall.setParent(targetElement);

            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, targetElement);
            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, codeElement);

            return;
        }
        Collections.swap(codeElement.getChildren(), index, index - 1);

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, codeElement);
    }
}
