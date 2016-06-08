/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import java.util.List;

import org.rf.ide.core.testdata.model.table.VariableTable;
import org.rf.ide.core.testdata.model.table.variables.AVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class RemoveVariableCommand extends EditorCommand {

    private final List<RobotVariable> variablesToDelete;

    public RemoveVariableCommand(final List<RobotVariable> variablesToDelete) {
        this.variablesToDelete = variablesToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variablesToDelete.isEmpty()) {
            return;
        }
        final RobotSuiteFileSection variablesSection = variablesToDelete.get(0).getParent();
        variablesSection.getChildren().removeAll(variablesToDelete);

        final VariableTable table = (VariableTable) variablesSection.getLinkedElement();
        for (final RobotVariable var : variablesToDelete) {
            table.removeVariable((AVariable) var.getLinkedElement());

        }
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_REMOVED, variablesSection);
    }
}
