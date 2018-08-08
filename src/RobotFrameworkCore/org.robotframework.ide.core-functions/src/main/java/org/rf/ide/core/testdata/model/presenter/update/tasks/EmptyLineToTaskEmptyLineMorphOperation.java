/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.presenter.update.tasks;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.update.ExecutablesStepsHolderMorphOperation;
import org.rf.ide.core.testdata.model.table.tasks.Task;

public class EmptyLineToTaskEmptyLineMorphOperation extends ExecutablesStepsHolderMorphOperation<Task> {

    @Override
    public boolean isApplicable(final ModelType elementType) {
        return elementType == ModelType.TEST_CASE_EMPTY_LINE || elementType == ModelType.USER_KEYWORD_EMPTY_LINE;
    }

    @Override
    public AModelElement<Task> insert(final Task task, final int index,
            final AModelElement<?> modelElement) {
        return task.addElement(index, modelElement);
    }
}
