/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.Collections;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordCallInCaseUpCommand extends EditorCommand {

    private final RobotKeywordCall keywordCall;

    public MoveKeywordCallInCaseUpCommand(final RobotKeywordCall keywordCall) {
        this.keywordCall = keywordCall;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (!keywordCall.isExecutable()) {
            throw new IllegalStateException("Unable to move non-executable rows");
        }

        final RobotCase robotCase = (RobotCase) keywordCall.getParent();
        final int index = keywordCall.getIndex();

        if (index == 0 || !robotCase.getChildren().get(index - 1).isExecutable()) {
            // no place to move it further up
            return;
        } else {
            Collections.swap(robotCase.getChildren(), index, index - 1);

            final TestCase linkedCase = robotCase.getLinkedElement();
            @SuppressWarnings("unchecked")
            final RobotExecutableRow<TestCase> linkedCall = (RobotExecutableRow<TestCase>) keywordCall
                    .getLinkedElement();
            linkedCase.moveUpExecutableRow(linkedCall);

            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_MOVED, robotCase);
        }
    }
}
