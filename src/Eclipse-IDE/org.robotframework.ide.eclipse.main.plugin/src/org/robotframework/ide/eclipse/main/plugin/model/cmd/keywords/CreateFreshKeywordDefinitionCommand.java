/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.NamesGenerator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class CreateFreshKeywordDefinitionCommand extends EditorCommand {

    private static final String DEFAULT_NAME = "Keyword";
    private final RobotKeywordsSection keywordsSection;
    private final int index;
    private final boolean notifySync;

    public CreateFreshKeywordDefinitionCommand(final RobotKeywordsSection keywordsSection,
            final boolean notifySynchronously) {
        this(keywordsSection, -1, notifySynchronously);
    }

    public CreateFreshKeywordDefinitionCommand(final RobotKeywordsSection keywordsSection, final int index) {
        this(keywordsSection, index, false);
    }

    private CreateFreshKeywordDefinitionCommand(final RobotKeywordsSection keywordsSection, final int index,
            final boolean notifySynchronously) {
        this.keywordsSection = keywordsSection;
        this.index = index;
        this.notifySync = notifySynchronously;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final String name = NamesGenerator.generateUniqueName(keywordsSection, DEFAULT_NAME);

        keywordsSection.createKeywordDefinition(index, name);

        if (notifySync) {
            eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED, keywordsSection);
        } else {
            eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED, keywordsSection);
        }
    }
}
