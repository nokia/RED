/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import java.util.ArrayList;
import java.util.List;

import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordDefinitionCommand extends EditorCommand {

    private final List<RobotKeywordDefinition> keywordsToDelete;

    private final List<Integer> deletedKeywordsIndexes = new ArrayList<>();

    public DeleteKeywordDefinitionCommand(final List<RobotKeywordDefinition> keywordsToDelete) {
        this.keywordsToDelete = keywordsToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (keywordsToDelete.isEmpty()) {
            return;
        }
        deletedKeywordsIndexes.clear();
        for (final RobotKeywordDefinition def : keywordsToDelete) {
            deletedKeywordsIndexes.add(def.getIndex());
        }

        final RobotSuiteFileSection keywordsSection = keywordsToDelete.get(0).getParent();
        keywordsSection.getChildren().removeAll(keywordsToDelete);

        final KeywordTable keywordsTable = (KeywordTable) keywordsSection.getLinkedElement();
        for (final RobotKeywordDefinition keywordToDelete : keywordsToDelete) {
            keywordsTable.removeKeyword(keywordToDelete.getLinkedElement());
        }

        eventBroker.send(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, keywordsSection);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(setupUndoCommandsForDeletedDefinitions());
    }

    private List<EditorCommand> setupUndoCommandsForDeletedDefinitions() {
        final List<EditorCommand> commands = new ArrayList<>();
        if (keywordsToDelete.size() == deletedKeywordsIndexes.size()) {
            for (int i = 0; i < keywordsToDelete.size(); i++) {
                final RobotKeywordDefinition def = keywordsToDelete.get(i);
                commands.add(new InsertKeywordDefinitionsCommand(def.getParent(), deletedKeywordsIndexes.get(i),
                        new RobotKeywordDefinition[] { def }));
            }
        }
        return commands;
    }
}
