/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.presenter.update.VariableTableModelUpdater;
import org.rf.ide.core.testdata.model.presenter.update.variables.VariablesValueConverter;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetDictItemsCommand extends EditorCommand {

    private final RobotVariable variable;

    private final List<String> newValue;
    
    private List<String> previousValue = new ArrayList<>();

    public SetDictItemsCommand(final RobotVariable variable, final List<String> newValue) {
        this.variable = variable;
        this.newValue = new ArrayList<>(newValue);
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (variable.getType() != VariableType.DICTIONARY) {
            throw new CommandExecutionException("Invalid type of variable: " + variable.getType());
        }
        
        for(final RobotToken token : VariablesValueConverter.convert(((DictionaryVariable) variable.getLinkedElement()).getItems(),
                RobotToken.class)){
            previousValue.add(token.getText());
        }

        new VariableTableModelUpdater().addOrSet(variable.getLinkedElement(), 0, newValue);
        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
    
    @Override
    public EditorCommand getUndoCommand() {
        return newUndoCommand(new SetDictItemsCommand(variable, previousValue));
    }
}
