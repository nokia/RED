/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.Collections;

import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCaseUpCommand extends EditorCommand {

    private final RobotCase testCase;

    public MoveCaseUpCommand(final RobotCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotCasesSection section = testCase.getParent();
        final int index = section.getChildren().indexOf(testCase);
        if (index == 0) {
            return;
        }
        Collections.swap(section.getChildren(), index, index - 1);

        final TestCaseTable linkedElement = (TestCaseTable) section.getLinkedElement();
        linkedElement.moveUpTest(testCase.getLinkedElement());

        eventBroker.post(RobotModelEvents.ROBOT_CASE_MOVED, section);
    }
}
