package org.robotframework.ide.core.execution.context;

import java.util.LinkedList;

import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;

public interface IRobotExecutableRowFinder {

    public RobotExecutableRow<?> findExecutableRow(final LinkedList<KeywordContext> currentKeywords);

}
