/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class RemoveKeywordDefinitionArgumentCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;
    private final int index;

    public RemoveKeywordDefinitionArgumentCommand(final RobotKeywordDefinition definition, final int index) {
        this.definition = definition;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotDefinitionSetting argumentsSetting = definition.getArgumentsSetting();
        if (argumentsSetting != null) {
            final List<String> arguments = argumentsSetting.getArguments();
            if (index < arguments.size()) {
                arguments.remove(index);
            }
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ARGUMENT_CHANGE, definition);
        }
    }
}
