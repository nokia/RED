/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;


public class TestCaseExecutableRowMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_EXECUTABLE_ROW;
    }

    @Override
    public AModelElement<?> insert(final UserKeyword keyword, final int index, final AModelElement<?> modelElement) {
        @SuppressWarnings("unchecked")
        final RobotExecutableRow<UserKeyword> executableRow = (RobotExecutableRow<UserKeyword>) modelElement;
        executableRow.setParent(keyword);

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

        keyword.addKeywordExecutionRow(executableRow, index);

        return executableRow;
    }

}
