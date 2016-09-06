/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
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
public class CleanVariableValueCommand extends EditorCommand {

    private final RobotVariable variable;
    
    private List<RobotToken> previousValues = newArrayList();

    public CleanVariableValueCommand(final RobotVariable variable) {
        this.variable = variable;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        boolean modified = false;
        switch (variable.getType()) {
            case SCALAR:
            case SCALAR_AS_LIST:
                final ScalarVariable scalar = (ScalarVariable) variable.getLinkedElement();
                if (!scalar.getValues().isEmpty()) {
                    modified = true;
                    for (final RobotToken value : newArrayList(scalar.getValues())) {
                        previousValues.add(value);
                        scalar.removeValue(value);
                    }
                }
                break;
            case LIST:
                final ListVariable list = (ListVariable) variable.getLinkedElement();
                if (!list.getItems().isEmpty()) {
                    modified = true;
                    for (final RobotToken value : newArrayList(list.getItems())) {
                        previousValues.add(value);
                        list.removeItem(value);
                    }
                }
                break;
            case DICTIONARY:
                final DictionaryVariable dict = (DictionaryVariable) variable.getLinkedElement();
                if (!dict.getItems().isEmpty()) {
                    modified = true;
                    for (final DictionaryKeyValuePair pair : newArrayList(dict.getItems())) {
                        previousValues.add(pair.getRaw());
                        dict.removeKeyValuePair(pair);
                    }
                }
                break;
            case INVALID:
                final UnknownVariable unknown = (UnknownVariable) variable.getLinkedElement();
                if (!unknown.getItems().isEmpty()) {
                    modified = true;
                    for (final RobotToken value : newArrayList(unknown.getItems())) {
                        previousValues.add(value);
                        unknown.removeItem(value);
                    }
                }
                break;
            default:
                throw new IllegalStateException(
                        "Unable to remove value of variable of type " + variable.getType().name());
        }

        if (modified) {
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
        }
    }
    
    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(setupUndoCommandsForRemovedValues());
    }

    private List<EditorCommand> setupUndoCommandsForRemovedValues() {
        final List<EditorCommand> commands = newArrayList();
        if (variable.getType() == VariableType.SCALAR) {
            if (!previousValues.isEmpty()) {
                commands.add(new SetScalarValueCommand(variable, previousValues.get(0).getText()));
            }
        } else {
            for (final RobotToken value : previousValues) {
                commands.add(new CreateCompoundVariableValueElementCommand(variable, value.getText()));
            }
        }
        return commands;
    }
}
