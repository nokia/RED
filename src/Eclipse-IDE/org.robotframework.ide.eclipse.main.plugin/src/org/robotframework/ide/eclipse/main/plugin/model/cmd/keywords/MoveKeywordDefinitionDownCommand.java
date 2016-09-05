/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import java.util.Collections;
import java.util.List;

import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

public class MoveKeywordDefinitionDownCommand extends EditorCommand {

    private final RobotKeywordDefinition keywordDef;

    public MoveKeywordDefinitionDownCommand(final RobotKeywordDefinition keywordDef) {
        this.keywordDef = keywordDef;
    }

    @Override
    public void execute() throws CommandExecutionException {
        final RobotElement section = keywordDef.getParent();
        final int size = section.getChildren().size();
        final int index = section.getChildren().indexOf(keywordDef);
        if (index == size - 1) {
            return;
        }
        Collections.swap(section.getChildren(), index, index + 1);
        
        final Object linkedElement = ((RobotKeywordsSection)section).getLinkedElement();
        if(linkedElement != null && linkedElement instanceof KeywordTable) {
            ((KeywordTable)linkedElement).moveDownKeyword(keywordDef.getLinkedElement());
        }

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_MOVED, section);
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new MoveKeywordDefinitionUpCommand(keywordDef));
    }
}
