/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordDefinitionCommand extends EditorCommand {

    private final List<RobotKeywordDefinition> definitionsToDelete;

    public DeleteKeywordDefinitionCommand(final List<RobotKeywordDefinition> definitionsToDelete) {
        this.definitionsToDelete = definitionsToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (definitionsToDelete.isEmpty()) {
            return;
        }
        final RobotSuiteFileSection keywordsSection = definitionsToDelete.get(0).getParent();
        keywordsSection.getChildren().removeAll(definitionsToDelete);

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, keywordsSection);
    }
}
