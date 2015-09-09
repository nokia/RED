/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetCaseNameCommand extends EditorCommand {

    private final RobotCase testCase;
    private final String newName;

    public SetCaseNameCommand(final RobotCase testCase, final String newName) {
        this.testCase = testCase;
        this.newName = newName;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (newName.equals(testCase.getName())) {
            return;
        }
        
        testCase.setName(newName);
        eventBroker.send(RobotModelEvents.ROBOT_CASE_NAME_CHANGE, testCase);
    }
}
