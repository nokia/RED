/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

public class CreateFreshCaseSettingCommandTest {

    @Test
    public void test() {

    }

    private static RobotCase createTestCase() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  Log  10")
                .appendLine("  Log  20")
                .appendLine("  Log  30")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0);
    }
}
