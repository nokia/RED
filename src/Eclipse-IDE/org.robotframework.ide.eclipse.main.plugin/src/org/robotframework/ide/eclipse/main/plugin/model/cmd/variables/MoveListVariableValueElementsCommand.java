/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveDirection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.Lists;

/**
 * @author Michal Anglart
 *
 */
public class MoveListVariableValueElementsCommand extends EditorCommand {

    private final RobotVariable variable;

    private final List<RobotToken> elementsToMove;

    private final MoveDirection direction;

    public MoveListVariableValueElementsCommand(final RobotVariable variable,
            final List<RobotToken> elementsToMove, final MoveDirection direction) {
        this.variable = variable;
        this.elementsToMove = elementsToMove;
        this.direction = direction;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        final List<RobotToken> elements = direction == MoveDirection.UP ? elementsToMove
                : Lists.reverse(elementsToMove);

        for (final RobotToken token : elements) {

            if (variable.getType() == VariableType.LIST) {
                final ListVariable var = (ListVariable) variable.getLinkedElement();
                if (direction == MoveDirection.UP) {
                    var.moveLeftItem(token);
                } else {
                    var.moveRightItem(token);
                }

            } else if (variable.getType() == VariableType.SCALAR_AS_LIST) {
                final ScalarVariable var = (ScalarVariable) variable.getLinkedElement();
                if (direction == MoveDirection.UP) {
                    var.moveLeftValue(token);
                } else {
                    var.moveRightValue(token);
                }

            } else if (variable.getType() == VariableType.INVALID) {
                final UnknownVariable var = (UnknownVariable) variable.getLinkedElement();
                if (direction == MoveDirection.UP) {
                    var.moveLeftItem(token);
                } else {
                    var.moveRightItem(token);
                }

            } else {
                throw new CommandExecutionException(
                        "Variables of type " + variable.getType()
                                + " cannot have their value elements moved with this command");
            }
        }

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
}
