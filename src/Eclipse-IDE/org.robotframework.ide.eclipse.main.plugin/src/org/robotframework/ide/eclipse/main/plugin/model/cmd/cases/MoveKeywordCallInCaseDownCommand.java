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
            // no place to move it further down
            return;
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
}
