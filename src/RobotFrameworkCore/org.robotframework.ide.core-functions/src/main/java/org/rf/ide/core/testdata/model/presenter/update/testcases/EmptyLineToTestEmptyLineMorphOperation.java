/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.testcases;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class EmptyLineToTestEmptyLineMorphOperation extends ExecutablesStepsHolderMorphOperation<TestCase> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TASK_EMPTY_LINE || elementType == ModelType.USER_KEYWORD_EMPTY_LINE;
    }

    @Override
    public AModelElement<TestCase> insert(final TestCase testCase, final int index,
            final AModelElement<?> modelElement) {
        return testCase.addElement(index, modelElement);
    }
}
