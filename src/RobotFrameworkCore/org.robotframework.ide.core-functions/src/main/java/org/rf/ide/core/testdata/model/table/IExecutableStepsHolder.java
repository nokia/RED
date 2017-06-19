/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public interface IExecutableStepsHolder<T extends AModelElement<? extends ARobotSectionTable>> {

    List<AModelElement<T>> getElements();

    List<RobotExecutableRow<T>> getExecutionContext();

    boolean removeElement(final AModelElement<T> element);

    T getHolder();

    RobotToken getName();
}
