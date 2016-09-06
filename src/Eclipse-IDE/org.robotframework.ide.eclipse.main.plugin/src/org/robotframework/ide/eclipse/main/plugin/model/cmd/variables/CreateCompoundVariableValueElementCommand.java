/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import static com.google.common.collect.Lists.newArrayList;

import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;

import java.util.List;

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
public class CreateCompoundVariableValueElementCommand extends EditorCommand {

    private final RobotVariable variable;

    private final String newElementContent;
    
    private final int index;
    
    private RobotToken newToken;
    
    private DictionaryKeyValuePair newKeyValuePair;

    public CreateCompoundVariableValueElementCommand(final RobotVariable variable, final String newElementContent) {
        this(variable, newElementContent, -1);
    }
    
    public CreateCompoundVariableValueElementCommand(final RobotVariable variable, final String newElementContent, final int index) {
        this.variable = variable;
        this.newElementContent = newElementContent;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {

        if (variable.getType() == VariableType.SCALAR_AS_LIST) {
            final ScalarVariable var = (ScalarVariable) variable.getLinkedElement();
            newToken = RobotToken.create(newElementContent);
            var.addNewValue(index < 0 ? var.getValues().size() : index, newToken);

        } else if (variable.getType() == VariableType.LIST) {
            final ListVariable var = (ListVariable) variable.getLinkedElement();
            newToken = RobotToken.create(newElementContent);
            var.addNewItem(index < 0 ? var.getItems().size() : index, newToken);

        } else if (variable.getType() == VariableType.INVALID) {
            final UnknownVariable var = (UnknownVariable) variable.getLinkedElement();
            newToken = RobotToken.create(newElementContent);
            var.addNewItem(index < 0 ? var.getItems().size() : index, newToken);

        } else if (variable.getType() == VariableType.DICTIONARY) {
            final DictionaryVariable var = (DictionaryVariable) variable.getLinkedElement();
            newKeyValuePair = DictionaryKeyValuePair.createFromRaw(newElementContent);
            var.addKeyValuePair(index < 0 ? var.getItems().size() : index, newKeyValuePair);

        } else {
            throw new CommandExecutionException("Variables of type " + variable.getType()
                    + " cannot have new value element added with this command");
        }
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
    
    @Override
    public List<EditorCommand> getUndoCommands() {
        EditorCommand command = null;
        if (variable.getType() == VariableType.DICTIONARY) {
            command = new RemoveDictVariableValueElementsCommand(variable, newArrayList(newKeyValuePair));
        } else {
            command = new RemoveListVariableValueElementsCommand(variable, newArrayList(newToken));
        }
        return newUndoCommands(command);
    }
}
