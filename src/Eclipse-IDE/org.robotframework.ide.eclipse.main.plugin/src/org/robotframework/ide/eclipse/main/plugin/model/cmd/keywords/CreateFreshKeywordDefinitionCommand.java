/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.NamesGenerator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.services.event.RedEventBroker;

public class CreateFreshKeywordDefinitionCommand extends EditorCommand {

    private static final String DEFAULT_NAME = "Keyword";
    private final RobotKeywordsSection keywordsSection;
    private final int index;
    private RobotKeywordDefinition newKeywordDefinition;

    public CreateFreshKeywordDefinitionCommand(final RobotKeywordsSection keywordsSection) {
        this(keywordsSection, -1);
    }

    public CreateFreshKeywordDefinitionCommand(final RobotKeywordsSection keywordsSection, final int index) {
        this.keywordsSection = keywordsSection;
        this.index = index;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final String name = NamesGenerator.generateUniqueName(keywordsSection, DEFAULT_NAME);

        newKeywordDefinition = keywordsSection.createKeywordDefinition(index, name);

        RedEventBroker.using(eventBroker)
                .additionallyBinding(RobotModelEvents.ADDITIONAL_DATA)
                .to(newKeywordDefinition)
                .send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED, keywordsSection);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new DeleteKeywordDefinitionCommand(newArrayList(newKeywordDefinition)));
    }
}
