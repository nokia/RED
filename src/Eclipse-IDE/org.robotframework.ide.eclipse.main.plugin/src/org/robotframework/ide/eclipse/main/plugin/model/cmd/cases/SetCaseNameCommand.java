/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.cases;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
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
        final RobotToken nameToken = RobotToken.create(newName);
        testCase.getLinkedElement().setTestName(nameToken);

        eventBroker.send(RobotModelEvents.ROBOT_CASE_NAME_CHANGE, testCase);
    }
}
