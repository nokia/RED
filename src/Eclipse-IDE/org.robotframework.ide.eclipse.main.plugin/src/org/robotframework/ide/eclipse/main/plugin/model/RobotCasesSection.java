/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import static java.util.stream.Collectors.toList;

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

    @Override
    public String getDefaultChildName() {
        return "case";
    }

    @Override
    public RobotCase createChild(final int index, final String name) {
        final RobotCase testCase;

        final TestCaseTable casesTable = getLinkedElement();
        if (index >= 0 && index < elements.size()) {
            testCase = new RobotCase(this, casesTable.createTestCase(name, index));
            elements.add(index, testCase);
        } else {
            testCase = new RobotCase(this, casesTable.createTestCase(name));
            elements.add(testCase);
        }
        return testCase;
    }

    @Override
    public void insertChild(final int index, final RobotFileInternalElement element) {
        final RobotCase testCase = (RobotCase) element;
        testCase.setParent(this);

        final TestCaseTable casesTable = getLinkedElement();
        if (index >= 0 && index < elements.size()) {
            getChildren().add(index, testCase);
            casesTable.addTest(testCase.getLinkedElement(), index);
        } else {
            getChildren().add(testCase);
            casesTable.addTest(testCase.getLinkedElement());
        }
    }

    @Override
    public void removeChildren(final List<? extends RobotFileInternalElement> elementsToRemove) {
        getChildren().removeAll(elementsToRemove);

        final TestCaseTable linkedElement = getLinkedElement();
        for (final RobotFileInternalElement elementToRemove : elementsToRemove) {
            linkedElement.removeTest((TestCase) elementToRemove.getLinkedElement());
        }
    }

    List<RobotCase> getTestCases() {
        return getChildren().stream().map(RobotCase.class::cast).collect(toList());
    }
}
