/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

public interface IExecutablesStepsHolderElementOperation<T extends IExecutableStepsHolder<?>> {

    boolean isApplicable(ModelType elementType);

    boolean isApplicable(IRobotTokenType elementType);

    AModelElement<T> create(T executablesHolder, int index, String action, List<String> args, final String comment);

    AModelElement<?> insert(T executablesHolder, int index, AModelElement<?> modelElement);

    void update(final AModelElement<?> modelElement, final int index, final String value);

    void update(final AModelElement<?> modelElement, final List<String> newArguments);

    void remove(T executablesHolder, AModelElement<?> modelElement);
}
