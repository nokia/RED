/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.RobotFile;
import org.rf.ide.core.testdata.model.presenter.MoveElementHelper;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;

public class TestCaseTable extends ARobotSectionTable {

    private final List<TestCase> testCases = new ArrayList<>();

    public TestCaseTable(final RobotFile parent) {
        super(parent);
    }

    public TestCase createTestCase(final String testName) {
        return createTestCase(testName, testCases.size());
    }

    public TestCase createTestCase(final String testName, final int position) {
        final TestCase test = new TestCase(RobotToken.create(testName));
        addTest(test, position);
        return test;
    }

    public void addTest(final TestCase testCase) {
        testCase.setParent(this);
        testCases.add(testCase);
    }

    public void addTest(final TestCase testCase, final int position) {
        testCase.setParent(this);
        testCases.add(position, testCase);
    }

    public void removeTest(final TestCase testCase) {
        testCases.remove(testCase);
    }

    @Override
    public boolean moveUpElement(final AModelElement<? extends ARobotSectionTable> element) {
        return MoveElementHelper.moveUp(testCases, (TestCase) element);
    }

    @Override
    public boolean moveDownElement(final AModelElement<? extends ARobotSectionTable> element) {
        return MoveElementHelper.moveDown(testCases, (TestCase) element);
    }

    public List<TestCase> getTestCases() {
        return Collections.unmodifiableList(testCases);
    }

    public boolean isEmpty() {
        return (testCases.isEmpty());
    }
}
