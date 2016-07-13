/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;

public class RobotCasesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Test Cases";

    RobotCasesSection(final RobotSuiteFile parent) {
        super(parent, SECTION_NAME);
    }

    public RobotCase createTestCase(final String name, final String comment) {
        return createTestCase(getChildren().size(), name, comment);
    }

    public RobotCase createTestCase(final int index, final String name, final String comment) {
        final RobotCase testCase = new RobotCase(this, name, comment);

        final TestCaseTable casesTable = (TestCaseTable) this.getLinkedElement();
        testCase.link(casesTable.createTestCase(name));

        elements.add(index, testCase);
        return testCase;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotCase> getChildren() {
        return (List<RobotCase>) super.getChildren();
    }
    
    @Override
    public void link(final ARobotSectionTable table) {
        super.link(table);

        final TestCaseTable testCaseTable = (TestCaseTable) sectionTable;
        for (final TestCase testCase : testCaseTable.getTestCases()) {
            final RobotCase newTestCase = new RobotCase(this, testCase.getTestName().getText().toString(), "");
            newTestCase.link(testCase);
            elements.add(newTestCase);
        }
    }
}
