package org.robotframework.ide.eclipse.main.plugin.model;


public class RobotCasesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Test Cases";

    public RobotCasesSection(final RobotSuiteFile parent, final boolean readOnly) {
        super(parent, SECTION_NAME, readOnly);
    }

    public RobotCase createTestCase(final String name, final String comment) {
        final RobotCase testCase = new RobotCase(this, name, comment);
        elements.add(testCase);
        return testCase;
    }
}
