/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

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
public class CreateCompoundVariableValueElementCommand extends EditorCommand {

    private final RobotVariable variable;

    private final String newElementContent;

    public CreateCompoundVariableValueElementCommand(final RobotVariable variable, final String newElementContent) {
        this.variable = variable;
        this.newElementContent = newElementContent;
    }

    @Override
    protected void execute() throws CommandExecutionException {

        if (variable.getType() == VariableType.SCALAR_AS_LIST) {
            final ScalarVariable var = (ScalarVariable) variable.getLinkedElement();
            var.addValue(RobotToken.create(newElementContent));

        } else if (variable.getType() == VariableType.LIST) {
            final ListVariable var = (ListVariable) variable.getLinkedElement();
            var.addItem(RobotToken.create(newElementContent));

        } else if (variable.getType() == VariableType.INVALID) {
            final UnknownVariable var = (UnknownVariable) variable.getLinkedElement();
            var.addItem(RobotToken.create(newElementContent));

        } else if (variable.getType() == VariableType.DICTIONARY) {
            final DictionaryVariable var = (DictionaryVariable) variable.getLinkedElement();
            final DictionaryKeyValuePair keyValuePair = DictionaryKeyValuePair.createFromRaw(newElementContent);
            var.put(keyValuePair.getRaw(), keyValuePair.getKey(), keyValuePair.getValue());

        } else {
            throw new CommandExecutionException("Variables of type " + variable.getType()
                    + " cannot have new value element added with this command");
        }
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }

}
