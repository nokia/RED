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

    private final String name;
    
    private String previousName;

    public SetCaseNameCommand(final RobotCase testCase, final String name) {
        this.testCase = testCase;
        this.name = name == null || name.isEmpty() ? "\\" : name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        previousName = testCase.getName();
        if (name.equals(previousName)) {
            return;
        }
        final RobotToken nameToken = RobotToken.create(name);
        testCase.getLinkedElement().setTestName(nameToken);

        eventBroker.send(RobotModelEvents.ROBOT_CASE_NAME_CHANGE, testCase);
    }

    @Override
    public EditorCommand getUndoCommand() {
        return newUndoCommand(new SetCaseNameCommand(testCase, previousName));
    }
}
