/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;

public interface IExecutableStepsHolder<T extends AModelElement<? extends ARobotSectionTable>> {

    List<RobotExecutableRow<T>> getExecutionContext();

    List<AModelElement<T>> getUnitSettings();

    T getHolder();
}
