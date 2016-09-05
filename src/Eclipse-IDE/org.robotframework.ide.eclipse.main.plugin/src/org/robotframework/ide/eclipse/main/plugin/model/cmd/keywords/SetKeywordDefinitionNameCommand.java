/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import java.util.List;

import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class SetKeywordDefinitionNameCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;
    private final String name;
    private String previousName;

    public SetKeywordDefinitionNameCommand(final RobotKeywordDefinition definition, final String name) {
        this.definition = definition;
        this.name = name == null || name.isEmpty() ? "\\" : name;
    }

    @Override
    public void execute() throws CommandExecutionException {
        previousName = definition.getName();
        if (previousName.equals(name)) {
            return;
        }

        final RobotToken nameToken = RobotToken.create(name);
        definition.getLinkedElement().setKeywordName(nameToken);

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_NAME_CHANGE, definition);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new SetKeywordDefinitionNameCommand(definition, previousName));
    }
}
