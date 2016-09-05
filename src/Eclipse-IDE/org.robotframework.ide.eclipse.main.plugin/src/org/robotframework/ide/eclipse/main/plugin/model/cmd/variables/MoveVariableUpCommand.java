/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.VariableTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveVariableUpCommand extends EditorCommand {

    private final RobotVariable variable;

    public MoveVariableUpCommand(final RobotVariable variable) {
        this.variable = variable;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotVariablesSection variablesSection = variable.getParent();
        final int index = variablesSection.getChildren().indexOf(variable);
        if (index == 0) {
            return;
        }
        Collections.swap(variablesSection.getChildren(), index, index - 1);

        final VariableTable table = variablesSection.getLinkedElement();
        table.moveUpVariable(variable.getLinkedElement());

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_MOVED, variablesSection);
    }
    
    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new MoveVariableDownCommand(variable));
    }
}
