/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.variables;

import java.util.List;

import org.rf.ide.core.testdata.model.table.variables.DictionaryVariable.DictionaryKeyValuePair;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 *
 */
public class SetDictVariableValueElementCommand extends EditorCommand {

    private final RobotVariable variable;

    private final DictionaryKeyValuePair oldElement;

    private final String newValue;

    private String previousValue;

    public SetDictVariableValueElementCommand(final RobotVariable variable,
            final DictionaryKeyValuePair oldElement, final String newValue) {
        this.variable = variable;
        this.oldElement = oldElement;
        this.newValue = newValue;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final List<String> splittedContent = Splitter.on('=').splitToList(newValue);
        final String key = splittedContent.get(0);
        final String value = Joiner.on('=').join(splittedContent.subList(1, splittedContent.size()));

        previousValue = oldElement.getRaw().getText();
        
        boolean thereIsAChange = false;
        if (!previousValue.equals(newValue)) {
            oldElement.setRaw(RobotToken.create(newValue));

            thereIsAChange = true;
        }
        if (!oldElement.getKey().getText().equals(key)) {
            oldElement.setKey(RobotToken.create(key));

            thereIsAChange = true;
        }
        if (!oldElement.getValue().getText().equals(value)) {
            oldElement.setValue(RobotToken.create(value));

            thereIsAChange = true;
        }

        if (thereIsAChange) {
            eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
        }
    }
    
    @Override
    public EditorCommand getUndoCommand() {
        return newUndoCommand(new SetDictVariableValueElementCommand(variable, oldElement, previousValue));
    }
}
