/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetScalarValueCommand extends EditorCommand {

    private final RobotVariable variable;

    private final String newValue;

    public SetScalarValueCommand(final RobotVariable variable, final String newValue) {
        this.variable = variable;
        this.newValue = newValue == null ? "" : newValue;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variable.getType() != VariableType.SCALAR) {
            throw new CommandExecutionException("Invalid type of variable: " + variable.getType());
        }
        final RobotToken token = RobotToken.create(newValue);

        final ScalarVariable scalar = (ScalarVariable) variable.getLinkedElement();
        if (scalar.getValues().isEmpty()) {
            scalar.addValue(token);
        } else {
            scalar.addValue(token, 0);
        }

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
}
