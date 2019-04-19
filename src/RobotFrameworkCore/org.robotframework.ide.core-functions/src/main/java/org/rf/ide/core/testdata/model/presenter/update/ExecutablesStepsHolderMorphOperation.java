/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update;

import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

public abstract class ExecutablesStepsHolderMorphOperation<T extends IExecutableStepsHolder<?>>
        implements IExecutablesStepsHolderElementOperation<T> {

    @Override
    public final boolean isApplicable(final IRobotTokenType elementType) {
        return false;
    }
}
