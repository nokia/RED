/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Collection;

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

    public RemoveDictVariableValueElementsCommand(final RobotVariable variable,
            final Collection<DictionaryKeyValuePair> elements) {
        this.variable = variable;
        this.elements = elements;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        if (variable.getType() != VariableType.DICTIONARY) {
            throw new CommandExecutionException("Variables of type " + variable.getType()
                    + " cannot have value elements removed with this command");
        }

        final DictionaryVariable dictVariable = (DictionaryVariable) variable.getLinkedElement();
        for (final DictionaryKeyValuePair pair : elements) {
            dictVariable.removeKeyValuePair(pair);
        }

        eventBroker.send(RobotModelEvents.ROBOT_VARIABLE_VALUE_CHANGE, variable);
    }
}
