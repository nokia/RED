/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.util.List;

import org.rf.ide.core.testdata.model.table.RobotExecutableRow;

/**
 * @author mmarzec
 *
 */
public interface IRobotExecutableRowFinder {

    public RobotExecutableRow<?> findExecutableRow(final List<KeywordContext> currentKeywords);

}
