/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.execution.context;

import java.util.LinkedList;

import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;

/**
 * @author mmarzec
 *
 */
public interface IRobotExecutableRowFinder {

    public RobotExecutableRow<?> findExecutableRow(final LinkedList<KeywordContext> currentKeywords);

}
