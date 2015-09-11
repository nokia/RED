/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.model.table;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.model.RobotFile;
import org.robotframework.ide.core.testData.model.table.testCases.TestCase;


public class TestCaseTable extends ARobotSectionTable {

    private final List<TestCase> testCases = new LinkedList<>();


    public TestCaseTable(final RobotFile parent) {
        super(parent);
    }


    public void addTest(final TestCase testCase) {
        testCase.setParent(this);
        testCases.add(testCase);
    }


    public List<TestCase> getTestCases() {
        return Collections.unmodifiableList(testCases);
    }


    public boolean isEmpty() {
        return (testCases.isEmpty());
    }
}
