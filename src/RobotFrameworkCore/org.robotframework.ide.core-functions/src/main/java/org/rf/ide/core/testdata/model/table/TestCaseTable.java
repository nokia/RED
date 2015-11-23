/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.table.testCases.TestCase;


public class TestCaseTable extends ARobotSectionTable {

    private final List<TestCase> testCases = new ArrayList<>();


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
