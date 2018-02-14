/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.ListVariable;
import org.rf.ide.core.testdata.model.table.variables.ScalarVariable;
import org.rf.ide.core.testdata.model.table.variables.UnknownVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


/**
 * @author Michal Anglart
 *
 */
public class RemoveListVariableValueElementsCommand extends EditorCommand {

    private final RobotVariable variable;

    private final Collection<RobotToken> elements;

    private final List<Integer> removedElementsIndexes = new ArrayList<>();

    public RemoveListVariableValueElementsCommand(final RobotVariable variable, final Collection<RobotToken> elements) {
        this.variable = variable;
        this.elements = elements;
    }

    @Override
    public void execute() throws CommandExecutionException {

        if (variable.getType() == VariableType.SCALAR_AS_LIST) {
            final ScalarVariable var = (ScalarVariable) variable.getLinkedElement();
            for (final RobotToken token : elements) {
                removedElementsIndexes.add(var.getValues().indexOf(token));
            }
            for (final RobotToken token : elements) {
                var.removeValue(token);
            }
        } else if (variable.getType() == VariableType.LIST) {
            final ListVariable var = (ListVariable) variable.getLinkedElement();
            for (final RobotToken token : elements) {
                removedElementsIndexes.add(var.getItems().indexOf(token));
            }
            for (final RobotToken token : elements) {
                var.removeItem(token);
            }

        } else if (variable.getType() == VariableType.INVALID) {
            final UnknownVariable var = (UnknownVariable) variable.getLinkedElement();
            for (final RobotToken token : elements) {
                removedElementsIndexes.add(var.getItems().indexOf(token));
            }
            for (final RobotToken token : elements) {
                var.removeItem(token);
            }

        } else {
            throw new CommandExecutionException("Variables of type " + variable.getType()
                    + " cannot have value elements removed with this command");
        }

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(setupUndoCommandsForRemovedElements());
    }

    private List<EditorCommand> setupUndoCommandsForRemovedElements() {
        final List<EditorCommand> commands = new ArrayList<>();
        if (elements.size() == removedElementsIndexes.size()) {
            int indexesCounter = 0;
            for (final RobotToken token : elements) {
                commands.add(new CreateCompoundVariableValueElementCommand(variable, token.getText(),
                        removedElementsIndexes.get(indexesCounter)));
                indexesCounter++;
            }
        }
        return commands;
    }
}
