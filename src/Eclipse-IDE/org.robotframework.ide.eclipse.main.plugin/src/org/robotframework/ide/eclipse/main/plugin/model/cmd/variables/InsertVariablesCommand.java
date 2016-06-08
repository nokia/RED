/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertVariablesCommand extends EditorCommand {

    private final RobotVariablesSection variablesSection;
    private final int index;
    private final List<RobotVariable> variablesToInsert;

    public InsertVariablesCommand(final RobotVariablesSection variablesSection, final RobotVariable[] variablesToInsert) {
        this(variablesSection, -1, variablesToInsert);
    }

    public InsertVariablesCommand(final RobotVariablesSection variablesSection, final int index,
            final RobotVariable[] variablesToInsert) {
        this.variablesSection = variablesSection;
        this.index = index;
        this.variablesToInsert = Arrays.asList(variablesToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {
        int shift = 0;
        for (final RobotVariable variable : variablesToInsert) {
            variable.setParent(variablesSection);

            if (index == -1) {
                variablesSection.createVariableFrom(variable);
            } else {
                variablesSection.createVariableFrom(index + shift, variable);
            }
            shift++;
        }
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
    }
}
