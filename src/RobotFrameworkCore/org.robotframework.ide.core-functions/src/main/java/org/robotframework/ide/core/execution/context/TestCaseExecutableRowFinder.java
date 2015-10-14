package org.robotframework.ide.core.execution.context;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.KeywordContext;
import org.robotframework.ide.core.execution.context.RobotDebugExecutionContext.TestCaseExecutionRowCounter;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;

public class TestCaseExecutableRowFinder implements IRobotExecutableRowFinder {

    private TestCase currentTestCase;

    private TestCaseExecutionRowCounter testCaseExecutionRowCounter;

    public TestCaseExecutableRowFinder(final TestCase currentTestCase,
            final TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        this.currentTestCase = currentTestCase;
        this.testCaseExecutionRowCounter = testCaseExecutionRowCounter;
    }

    @Override
    public RobotExecutableRow<?> findExecutableRow(LinkedList<KeywordContext> currentKeywords) {
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
        return null;
    }

    public void setTestCaseExecutionRowCounter(TestCaseExecutionRowCounter testCaseExecutionRowCounter) {
        this.testCaseExecutionRowCounter = testCaseExecutionRowCounter;
    }

    public void setCurrentTestCase(TestCase currentTestCase) {
        this.currentTestCase = currentTestCase;
    }

}
