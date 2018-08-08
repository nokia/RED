/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class ExecRowToTestExecRowMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TASK_EXECUTABLE_ROW || elementType == ModelType.USER_KEYWORD_EXECUTABLE_ROW;
    }

    @Override
    public AModelElement<?> insert(final TestCase testCase, final int index,
            final AModelElement<?> modelElement) {
        final RobotExecutableRow<?> executableRow = (RobotExecutableRow<?>) modelElement;
        testCase.addElement(index, executableRow);

        // executable row could be taken from some keyword originally, so we need to fix types in
        // its tokens
        final RobotToken action = executableRow.getAction();
        action.getTypes().clear();
        executableRow.setAction(action);

        for (int i = 0; i < executableRow.getArguments().size(); i++) {
            final RobotToken arg = executableRow.getArguments().get(i);
            arg.getTypes().clear();
            executableRow.setArgument(i, arg);
        }

        return executableRow;
    }
}
