/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords;

import static com.google.common.collect.Iterables.any;

import java.util.Arrays;
import java.util.List;

import org.rf.ide.core.testdata.model.table.KeywordTable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.NamesGenerator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;

import com.google.common.base.Predicate;

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
        final KeywordTable keywordTable = keywordsSection.getLinkedElement();

        int counter = index;
        for (final RobotKeywordDefinition keyword : definitionsToInsert) {
            keyword.setParent(keywordsSection);
            keyword.getLinkedElement().setParent(keywordTable);

            if (nameChangeIsRequired(keyword)) {
                final String newName = NamesGenerator.generateUniqueName(keywordsSection, keyword.getName(), true);
                keyword.getLinkedElement().getKeywordName().setText(newName);
            }

            if (counter == -1) {
                keywordsSection.getChildren().add(keyword);
                keywordTable.addKeyword(keyword.getLinkedElement());
            } else {
                keywordsSection.getChildren().add(counter, keyword);
                keywordTable.addKeyword(keyword.getLinkedElement(), counter);
                counter++;
            }
        }

        eventBroker.post(RobotModelEvents.ROBOT_KEYWORD_DEFINITION_ADDED, keywordsSection);
    }

    private boolean nameChangeIsRequired(final RobotKeywordDefinition definition) {
        return any(keywordsSection.getChildren(),
                new Predicate<RobotKeywordDefinition>() {

                    @Override
                    public boolean apply(final RobotKeywordDefinition def) {
                        return def.getName().equalsIgnoreCase(definition.getName());
                    }
                });
    }

    @Override
    public List<EditorCommand> getUndoCommands() {
        return newUndoCommands(new DeleteKeywordDefinitionCommand(definitionsToInsert));
    }
}
