/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.EmptyCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveCaseDownCommand extends EditorCommand {

    private final RobotCase testCase;
    private boolean wasMoved = true;

    public MoveCaseDownCommand(final RobotCase testCase) {
        this.testCase = testCase;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotCasesSection section = testCase.getParent();
        final int size = section.getChildren().size();
        final int index = section.getChildren().indexOf(testCase);
        if (index == size - 1) {
            wasMoved = false;
            return;
        }
        Collections.swap(section.getChildren(), index, index + 1);

        final TestCaseTable linkedElement = section.getLinkedElement();
        linkedElement.moveDownTest(testCase.getLinkedElement());

        eventBroker.send(RobotModelEvents.ROBOT_CASE_MOVED, section);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(wasMoved ? new MoveCaseUpCommand(testCase) : new EmptyCommand());
    }
}
