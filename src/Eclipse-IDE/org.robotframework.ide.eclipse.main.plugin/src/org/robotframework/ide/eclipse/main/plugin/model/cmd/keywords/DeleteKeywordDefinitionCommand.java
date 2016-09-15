/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.rf.ide.core.testdata.model.table.ARobotSectionTable;
import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.CompoundEditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class DeleteKeywordDefinitionCommand extends EditorCommand {

    private final List<RobotKeywordDefinition> definitionsToDelete;
    
    private List<Integer> deletedDefinitionsIndexes = newArrayList();

    public DeleteKeywordDefinitionCommand(final List<RobotKeywordDefinition> definitionsToDelete) {
        this.definitionsToDelete = definitionsToDelete;
    }

    @Override
    public void execute() throws CommandExecutionException {
        if (definitionsToDelete.isEmpty()) {
            return;
        }
        for (final RobotKeywordDefinition def : definitionsToDelete) {
            deletedDefinitionsIndexes.add(def.getIndex());
        }
       
        final RobotSuiteFileSection keywordsSection = definitionsToDelete.get(0).getParent();
        keywordsSection.getChildren().removeAll(definitionsToDelete);
        
        removeModelElements(keywordsSection);

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_REMOVED, keywordsSection);
    }

    private void removeModelElements(final RobotSuiteFileSection keywordsSection) {
        final ARobotSectionTable table = keywordsSection.getLinkedElement();
        if(table != null && table instanceof KeywordTable) {
            final KeywordTable keywordsTable = (KeywordTable) table;
            for (final RobotKeywordDefinition robotKeywordDefinition : definitionsToDelete) {
                keywordsTable.removeKeyword(robotKeywordDefinition.getLinkedElement());
            }
        }
    }

    @Override
    public EditorCommand getUndoCommand() {
        return newUndoCompoundCommand(new CompoundEditorCommand(this, setupUndoCommandsForDeletedDefinitions()));
    }

    private List<EditorCommand> setupUndoCommandsForDeletedDefinitions() {
        final List<EditorCommand> commands = newArrayList();
        for (int i = 0; i < definitionsToDelete.size(); i++) {
            final RobotKeywordDefinition def = definitionsToDelete.get(i);
            commands.add(new InsertKeywordDefinitionsCommand(def.getParent(), deletedDefinitionsIndexes.get(i),
                    new RobotKeywordDefinition[] { def }));
        }
        return commands;
    }
}
