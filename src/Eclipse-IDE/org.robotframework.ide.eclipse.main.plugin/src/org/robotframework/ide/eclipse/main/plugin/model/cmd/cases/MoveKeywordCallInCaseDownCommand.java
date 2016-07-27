/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.Collections;

import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordCallInCaseDownCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    public MoveKeywordCallInCaseDownCommand(final RobotKeywordCall keywordCall) {
        this.keywordCall = keywordCall;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (keywordCall.getLinkedElement().getModelType() != ModelType.TEST_CASE_EXECUTABLE_ROW) {
            throw new IllegalStateException("Unable to move non-executable rows");
        }

        final RobotCase robotCase = (RobotCase) keywordCall.getParent();
        final int size = robotCase.getChildren().size();
        final int index = keywordCall.getIndex();

        if (index == size - 1) {
            // lets try to move the element down from here
            final int defsSize = robotCase.getParent().getChildren().size();
            final int indexOfElement = robotCase.getIndex();
            if (indexOfElement == defsSize - 1) {
                // no place to move it further down
                return;
            }

            final RobotCase nextTestCase = robotCase.getParent().getChildren().get(indexOfElement + 1);

            robotCase.removeChild(keywordCall);
            nextTestCase.insertKeywordCall(findIndex(nextTestCase), keywordCall);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, nextTestCase);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, robotCase);
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, nextTestCase);

        } else {
            Collections.swap(robotCase.getChildren(), index, index + 1);

            final TestCase linkedCase = robotCase.getLinkedElement();
            @SuppressWarnings("unchecked")
            final RobotExecutableRow<TestCase> linkedCall = (RobotExecutableRow<TestCase>) keywordCall
                    .getLinkedElement();
            linkedCase.moveDownExecutableRow(linkedCall);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, robotCase);
        }
    }

    private int findIndex(final RobotCase nextTestCase) {
        int i = 0;
        for (final RobotKeywordCall call : nextTestCase.getChildren()) {
            if (call.getLinkedElement().getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW) {
                break;
            }
            i++;
        }
        return i;
    }
}
