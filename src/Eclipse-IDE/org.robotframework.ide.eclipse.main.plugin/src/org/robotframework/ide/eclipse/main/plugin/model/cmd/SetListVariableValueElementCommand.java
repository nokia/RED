/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

/**
 * @author Michal Anglart
 *
 */
public class SetListVariableValueElementCommand extends EditorCommand {

    private final RobotVariable variable;

    private final RobotToken oldElement;

    private final String newValue;


    public SetListVariableValueElementCommand(final RobotVariable variable,
            final RobotToken oldElement, final String newValue) {
        this.variable = variable;
        this.oldElement = oldElement;
        this.newValue = newValue;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        if (oldElement.getText().equals(newValue)) {
            return;
        }
        oldElement.setText(newValue);
        oldElement.setRaw(newValue);

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
}
