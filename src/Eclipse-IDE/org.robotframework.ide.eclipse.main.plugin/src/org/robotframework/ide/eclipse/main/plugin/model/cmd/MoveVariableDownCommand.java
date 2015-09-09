/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collections;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveVariableDownCommand extends EditorCommand {

    private final RobotVariable variable;

    public MoveVariableDownCommand(final RobotVariable variable) {
        this.variable = variable;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotVariablesSection variablesSection = variable.getParent();
        final int index = variablesSection.getChildren().indexOf(variable);

        final List<RobotVariable> children = variablesSection.getChildren();
        if (index == children.size() - 1) {
            return;
        }
        Collections.swap(children, index, index + 1);

        eventBroker.post(RobotModelEvents.ROBOT_VARIABLE_MOVED, variablesSection);
    }
}
