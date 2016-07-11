/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordSettingCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;

    private final String settingName;

    private final List<String> args;

    private final int index;

    public CreateFreshKeywordSettingCommand(final RobotKeywordDefinition definition, final int index,
            final String settingName, final List<String> args) {
        this.definition = definition;
        this.index = index;
        this.settingName = settingName;
        this.args = args;
    }

    @Override
    protected void execute() throws CommandExecutionException {
        definition.createKeywordDefinitionSetting(index, settingName, args, "");

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, definition);
    }
}
