/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.keywords;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;

public class EmptyLineToKeywordEmptyLineMorphOperation extends ExecutablesStepsHolderMorphOperation<UserKeyword> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_EMPTY_LINE || elementType == ModelType.TASK_EMPTY_LINE;
    }

    @Override
    public AModelElement<UserKeyword> insert(final UserKeyword userKeyword, final int index,
            final AModelElement<?> modelElement) {
        return userKeyword.addElement(index, modelElement);
    }
}
