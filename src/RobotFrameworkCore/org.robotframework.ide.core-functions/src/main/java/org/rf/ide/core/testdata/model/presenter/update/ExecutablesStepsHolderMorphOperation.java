/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

public abstract class ExecutablesStepsHolderMorphOperation<T extends IExecutableStepsHolder<?>>
        implements IExecutablesStepsHolderElementOperation<T> {

    @Override
    public final boolean isApplicable(final IRobotTokenType elementType) {
        return false;
    }

    @Override
    public final AModelElement<T> create(final T executablesHolder, final int index, final String action,
            final List<String> args, final String comment) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void update(final AModelElement<?> modelElement, final int index, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void update(final AModelElement<?> modelElement, final List<String> newArguments) {
        throw new UnsupportedOperationException();
    }
}
