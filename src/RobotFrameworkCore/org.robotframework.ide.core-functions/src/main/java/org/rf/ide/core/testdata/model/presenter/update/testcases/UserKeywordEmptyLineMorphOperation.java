/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class UserKeywordEmptyLineMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.USER_KEYWORD_EMPTY_LINE;
    }

    @Override
    public RobotEmptyRow<TestCase> insert(final TestCase testCase, final int index,
            final AModelElement<?> modelElement) {
        @SuppressWarnings("unchecked")
        final RobotEmptyRow<TestCase> emptyLine = (RobotEmptyRow<TestCase>) modelElement;
        emptyLine.setParent(testCase);
        testCase.addElement(emptyLine, index);

        return emptyLine;
    }
}
