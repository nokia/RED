/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertKeywordDefinitionArgumentCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;
    private final int index;
    private final String argument;

    public InsertKeywordDefinitionArgumentCommand(final RobotKeywordDefinition definition, final int index,
            final String argument) {
        this.definition = definition;
        this.index = index;
        this.argument = argument;
    }

    @Override
    public void execute() throws CommandExecutionException {
        RobotDefinitionSetting argumentsSetting = definition.getArgumentsSetting();
        if (argumentsSetting == null) {
            argumentsSetting = definition.createDefinitionSetting(0, RobotKeywordDefinition.ARGUMENTS,
                    new ArrayList<String>(), "");
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, definition);
        }

        final List<String> arguments = argumentsSetting.getArguments();

        for (int i = arguments.size(); i < index; i++) {
            arguments.add("");
        }
        arguments.add(index, argument);
        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ARGUMENT_CHANGE, definition);
    }
}
