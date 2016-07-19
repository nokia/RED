/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.Arrays;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class InsertKeywordDefinitionsCommand extends EditorCommand {

    private final RobotKeywordsSection keywordsSection;
    private final int index;
    private final List<RobotKeywordDefinition> definitionsToInsert;

    public InsertKeywordDefinitionsCommand(final RobotKeywordsSection keywordsSection,
            final RobotKeywordDefinition[] definitionsToInsert) {
        this(keywordsSection, -1, definitionsToInsert);
    }

    public InsertKeywordDefinitionsCommand(final RobotKeywordsSection keywordsSection, final int index,
            final RobotKeywordDefinition[] definitionsToInsert) {
        this.keywordsSection = keywordsSection;
        this.index = index;
        this.definitionsToInsert = Arrays.asList(definitionsToInsert);
    }

    @Override
    public void execute() throws CommandExecutionException {

        int shift = 0;
        for (final RobotKeywordDefinition robotKeywordDefinition : definitionsToInsert) {
            keywordsSection.insertKeywordDefinitionCopy(index < 0 ? -1 : index + shift, robotKeywordDefinition);
            shift++;
        }

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED, keywordsSection);
    }
}
