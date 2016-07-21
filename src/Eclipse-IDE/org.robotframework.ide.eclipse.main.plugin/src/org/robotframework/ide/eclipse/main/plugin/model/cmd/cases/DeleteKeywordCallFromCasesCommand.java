/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.TestCaseTableModelUpdater;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.model.IRobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordCallFromCasesCommand extends EditorCommand {

    private final List<? extends RobotKeywordCall> callsToDelete;

    public DeleteKeywordCallFromCasesCommand(final List<? extends RobotKeywordCall> callsToDelete) {
        this.callsToDelete = callsToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (callsToDelete.isEmpty()) {
            return;
        }
        final IRobotCodeHoldingElement parent = callsToDelete.get(0).getParent();
        parent.getChildren().removeAll(callsToDelete);

        final TestCase testCase = (TestCase) parent.getLinkedElement();
        for (final RobotKeywordCall robotKeywordCall : callsToDelete) {
            @SuppressWarnings("unchecked")
            final AModelElement<TestCase> modelElement = (AModelElement<TestCase>) robotKeywordCall.getLinkedElement();
            if (modelElement.getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
                testCase.removeExecutableRow((RobotExecutableRow<TestCase>) modelElement);
            } else {
                new TestCaseTableModelUpdater().remove(testCase, modelElement);
            }
        }

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_CALL_REMOVED, parent);
    }
}
