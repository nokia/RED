/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class CreateArgumentSettingCommand extends EditorCommand {

    private final RobotKeywordDefinition definition;

    private final int index;

    private final String value;

    private boolean executed = false;

    public CreateArgumentSettingCommand(final RobotKeywordDefinition definition, final int index, final String value) {
        this.definition = definition;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (value == null || value.isEmpty()) {
            return;
        }

        final List<String> tokens = IntStream.range(0, index + 1).mapToObj(i -> "\\").collect(toList());
        tokens.set(0, RobotTokenType.KEYWORD_SETTING_ARGUMENTS.getRepresentation().get(0));
        tokens.set(index, value);

        final RobotKeywordCall setting = definition.createSetting(0, tokens);

        executed = true;
        RedEventBroker.using(eventBroker)
            .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA).to(setting)
            .send(RobotModelEvents.ROBOT_KEYWORD_CALL_ADDED, definition);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        // the setting was inserted at 0 index
        return executed ? newUndoCommands(new DeleteKeywordCallCommand(newArrayList(definition.getChildren().get(0))))
                : new ArrayList<>();
    }
}