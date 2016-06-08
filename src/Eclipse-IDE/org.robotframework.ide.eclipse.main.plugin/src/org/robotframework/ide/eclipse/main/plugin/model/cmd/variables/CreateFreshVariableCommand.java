/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.NamesGenerator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshVariableCommand extends EditorCommand {

    private static final String DEFAULT_NAME = "var";
    private final RobotVariablesSection variablesSection;
    private final int index;

    private final VariableType variableType;

    public CreateFreshVariableCommand(final RobotVariablesSection variablesSection, final VariableType variableType) {
        this(variablesSection, -1, variableType);
    }

    public CreateFreshVariableCommand(final RobotVariablesSection variablesSection, final int index,
            final VariableType variableType) {
        this.variablesSection = variablesSection;
        this.index = index;
        this.variableType = variableType;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final String name = NamesGenerator.generateUniqueName(variablesSection, DEFAULT_NAME, false);

        if (index == -1) {
            variablesSection.createVariable(variableType, name);
        } else {
            variablesSection.createVariable(index, variableType, name);
        }

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_ADDED, variablesSection);
    }
}
