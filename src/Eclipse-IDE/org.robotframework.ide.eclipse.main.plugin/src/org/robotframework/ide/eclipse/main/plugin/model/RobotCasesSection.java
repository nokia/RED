/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class RobotCasesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Test Cases";

    RobotCasesSection(final RobotSuiteFile parent, final TestCaseTable testCaseTable) {
        super(parent, SECTION_NAME, testCaseTable);
    }

    @Override
    public void link() {
        final TestCaseTable testCaseTable = getLinkedElement();
        for (final TestCase testCase : testCaseTable.getTestCases()) {
            final RobotCase newTestCase = new RobotCase(this, testCase);
            newTestCase.link();
            elements.add(newTestCase);
        }
    }

    @Override
    public TestCaseTable getLinkedElement() {
        return (TestCaseTable) super.getLinkedElement();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotCase> getChildren() {
        return (List<RobotCase>) super.getChildren();
    }

    public RobotCase createTestCase(final String name) {
        return createTestCase(getChildren().size(), name);
    }

    public RobotCase createTestCase(final int index, final String name) {
        final TestCaseTable casesTable = getLinkedElement();
        final TestCase userTestCase = casesTable.createTestCase(name);
        final RobotCase testCase = new RobotCase(this, userTestCase);
        elements.add(index, testCase);
        return testCase;
    }

    List<RobotCase> getTestCases() {
        final List<RobotCase> testCases = newArrayList();
        for (final RobotElement child : getChildren()) {
            testCases.add((RobotCase) child);
        }
        return testCases;
    }
}
