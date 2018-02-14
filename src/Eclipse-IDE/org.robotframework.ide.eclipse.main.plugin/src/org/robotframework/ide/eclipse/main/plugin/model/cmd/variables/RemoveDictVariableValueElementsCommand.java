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
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;


/**
 * @author Michal Anglart
 *
 */
public class RemoveDictVariableValueElementsCommand extends EditorCommand {

    private final RobotVariable variable;

    private final Collection<DictionaryKeyValuePair> elements;

    private final List<Integer> removedElementsIndexes = new ArrayList<>();

    public RemoveDictVariableValueElementsCommand(final RobotVariable variable,
            final Collection<DictionaryKeyValuePair> elements) {
        this.variable = variable;
        this.elements = elements;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variable.getType() != VariableType.DICTIONARY) {
            throw new CommandExecutionException("Variables of type " + variable.getType()
                    + " cannot have value elements removed with this command");
        }

        final DictionaryVariable dictVariable = (DictionaryVariable) variable.getLinkedElement();
        for (final DictionaryKeyValuePair pair : elements) {
            removedElementsIndexes.add(dictVariable.getItems().indexOf(pair));
        }

        for (final DictionaryKeyValuePair pair : elements) {
            dictVariable.removeKeyValuePair(pair);
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
            for (final DictionaryKeyValuePair pair : elements) {
                commands.add(new CreateCompoundVariableValueElementCommand(variable, pair.getRaw().getText(),
                        removedElementsIndexes.get(indexesCounter)));
                indexesCounter++;
            }
        }
        return commands;
    }
}
