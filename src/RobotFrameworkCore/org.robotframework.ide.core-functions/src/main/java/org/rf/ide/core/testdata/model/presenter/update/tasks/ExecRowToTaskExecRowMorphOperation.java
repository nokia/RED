/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.tasks;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class ExecRowToTaskExecRowMorphOperation extends ExecutablesStepsHolderMorphOperation<Task> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_EXECUTABLE_ROW
                || elementType == ModelType.USER_KEYWORD_EXECUTABLE_ROW;
    }

    @Override
    public AModelElement<?> insert(final Task task, final int index, final AModelElement<?> modelElement) {
        final RobotExecutableRow<?> executableRow = (RobotExecutableRow<?>) modelElement;
        task.addElement(index, executableRow);

        // executable row could be taken from some test case originally, so we need to fix types in
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
