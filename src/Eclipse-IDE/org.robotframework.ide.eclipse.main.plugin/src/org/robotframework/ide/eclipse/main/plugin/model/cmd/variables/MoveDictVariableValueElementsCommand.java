/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.MoveDirection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.collect.Lists;

/**
 * @author Michal Anglart
 *
 */
public class MoveDictVariableValueElementsCommand extends EditorCommand {

    private final RobotVariable variable;

    private final List<DictionaryKeyValuePair> elementsToMove;

    private final MoveDirection direction;

    public MoveDictVariableValueElementsCommand(final RobotVariable variable,
            final List<DictionaryKeyValuePair> elementsToMove, final MoveDirection direction) {
        this.variable = variable;
        this.elementsToMove = elementsToMove;
        this.direction = direction;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        if (variable.getType() != VariableType.DICTIONARY) {
            throw new CommandExecutionException("Variables of type " + variable.getType()
                    + " cannot have their value elements moved with this command");
        }
        final List<DictionaryKeyValuePair> elements = direction == MoveDirection.UP ? elementsToMove
                : Lists.reverse(elementsToMove);

        final DictionaryVariable dictVariable = (DictionaryVariable) variable.getLinkedElement();
        for (final DictionaryKeyValuePair detailToMove : elements) {
            if (direction == MoveDirection.UP) {
                dictVariable.moveLeftKeyValuePair(detailToMove);
            } else {
                dictVariable.moveRightKeyValuePair(detailToMove);
            }
        }

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
}
