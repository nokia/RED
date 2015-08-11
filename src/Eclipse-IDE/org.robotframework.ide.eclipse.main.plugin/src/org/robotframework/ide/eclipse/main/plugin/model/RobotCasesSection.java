package org.robotframework.ide.eclipse.main.plugin.model;

import java.util.List;

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
        elements.add(index, testCase);
        return testCase;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<RobotCase> getChildren() {
        return (List<RobotCase>) super.getChildren();
    }
}
