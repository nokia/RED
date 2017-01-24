/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.context;

import java.util.List;

import org.rf.ide.core.execution.context.RobotDebugExecutionContext.TestCaseExecutionRowCounter;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

/**
 * @author mmarzec
 *
 */
public class TestCaseExecutableRowFinder implements IRobotExecutableRowFinder {

    private TestCase currentTestCase;

    private TestCaseExecutionRowCounter testCaseExecutionRowCounter;

    public TestCaseExecutableRowFinder(final TestCase currentTestCase,
            final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        this.currentTestCase = currentTestCase;
        this.testCaseExecutionRowCounter = testCaseExecutionRowCounter;
    }

    @Override
    public RobotExecutableRow<?> findExecutableRow(final List<KeywordContext> currentKeywords) {
        if (currentTestCase != null) {
            final List<RobotExecutableRow<TestCase>> executionRows = currentTestCase.getTestExecutionRows();
            if (testCaseExecutionRowCounter.getCounter() < executionRows.size()) {
                final RobotExecutableRow<TestCase> executionRow = executionRows.get(testCaseExecutionRowCounter.getCounter());
                testCaseExecutionRowCounter.increment();

                if (executionRow.isExecutable()) {
                    return executionRow;
                } else {
                    return findExecutableRow(currentKeywords);
                }
            }
        }
        return null;
    }

    public void setTestCaseExecutionRowCounter(final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        this.testCaseExecutionRowCounter = testCaseExecutionRowCounter;
    }

    public void setCurrentTestCase(final TestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
    }

}
