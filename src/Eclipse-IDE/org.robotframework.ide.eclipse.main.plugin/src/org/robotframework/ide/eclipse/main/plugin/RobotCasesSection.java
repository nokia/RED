package org.robotframework.ide.eclipse.main.plugin;


public class RobotCasesSection extends RobotSuiteFileSection {

    public static final String SECTION_NAME = "Test Cases";

    public RobotCasesSection(final RobotSuiteFile parent, final boolean readOnly) {
        super(parent, SECTION_NAME, readOnly);
    }
}
