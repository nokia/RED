package org.robotframework.ide.core.testData.model.table;

import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.testCases.TestCase;


public class TestCaseTable extends ARobotSectionTable {

    private final List<TestCase> testCases = new LinkedList<>();


    public void addTest(final TestCase testCase) {
        testCases.add(testCase);
    }


    public List<TestCase> getTestCases() {
        return testCases;
    }
}
