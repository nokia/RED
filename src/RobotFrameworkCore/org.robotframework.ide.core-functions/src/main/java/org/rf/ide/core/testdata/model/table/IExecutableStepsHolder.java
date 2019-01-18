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

    T getHolder();

    RobotToken getName();

    void setName(RobotToken name);

    List<AModelElement<T>> getElements();

    List<RobotExecutableRow<T>> getExecutionContext();

    boolean removeElement(final AModelElement<T> element);

    AModelElement<T> addElement(final AModelElement<?> element);

    AModelElement<T> addElement(int index, final AModelElement<?> element);
}
