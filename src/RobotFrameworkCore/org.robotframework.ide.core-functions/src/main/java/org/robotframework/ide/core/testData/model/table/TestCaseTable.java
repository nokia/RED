package org.robotframework.ide.core.testData.model.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.table.testCases.TestCase;


public class TestCaseTable extends ARobotSectionTable {

    private final List<TestCase> testCases = new LinkedList<>();


    public TestCaseTable(String uuid) {
        super(uuid);
    }


    public void addTest(final TestCase testCase) {
        testCase.setFileUUID(getFileUUID());
        testCases.add(testCase);
    }


    public List<TestCase> getTestCases() {
        return Collections.unmodifiableList(testCases);
    }


    public boolean isEmpty() {
        return (testCases.isEmpty());
    }
}
