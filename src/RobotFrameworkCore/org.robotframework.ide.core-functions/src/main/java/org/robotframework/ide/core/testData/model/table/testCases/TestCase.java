package org.robotframework.ide.core.testData.model.table.testCases;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.AModelElement;
import org.robotframework.ide.core.testData.model.table.RobotExecutableRow;
import org.robotframework.ide.core.testData.text.read.recognizer.RobotToken;


public class TestCase extends AModelElement {

    private RobotToken testName;
    private final List<RobotExecutableRow> testContext = new LinkedList<>();


    public TestCase(final RobotToken testName) {
        this.testName = testName;
    }


    public RobotToken getTestName() {
        return testName;
    }


    public void setTestName(final RobotToken testName) {
        this.testName = testName;
    }


    public void addTestExecutionRow(final RobotExecutableRow executionRow) {
        this.testContext.add(executionRow);
    }


    public List<RobotExecutableRow> getTestExecutionRows() {
        return testContext;
    }


    @Override
    public boolean isPresent() {
        return true;
    }
}
