/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.testdata.model.presenter.update;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.IExecutableStepsHolder;
import org.rf.ide.core.testdata.text.read.IRobotTokenType;

public interface IExecutablesStepsHolderElementOperation<T extends IExecutableStepsHolder<?>> {

    boolean isApplicable(ModelType elementType);

    boolean isApplicable(IRobotTokenType elementType);

    AModelElement<?> insert(T executablesHolder, int index, AModelElement<?> modelElement);
}
