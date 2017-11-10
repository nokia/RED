/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.ArrayList;
import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.keywords.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.KeywordCallsTableValuesChangingCommandsCollector;

/**
 * @author Michal Anglart
 */
public class KeywordsTableValuesChangingCommandsCollector {

    public List<? extends EditorCommand> collectForRemoval(final RobotElement element, final int column,
            final int numberOfColumns) {
        return collect(element, null, column, numberOfColumns);
    }

    public List<? extends EditorCommand> collectForChange(final RobotElement element, final String newValue,
            final int column, final int numberOfColumns) {
        return collect(element, newValue, column, numberOfColumns);
    }

    private List<? extends EditorCommand> collect(final RobotElement element, final String value, final int column,
            final int numberOfColumns) {
        final List<EditorCommand> commands = new ArrayList<>();

        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) element;

            if (column == 0) {
                commands.add(new SetKeywordDefinitionNameCommand(keywordDef, value));
            } else if (column > 0 && column <= numberOfColumns - 1) {
                commands.add(new SetKeywordDefinitionArgumentCommand(keywordDef, column - 1, value));
            }
        } else {
            final List<? extends EditorCommand> callCommands = new KeywordCallsTableValuesChangingCommandsCollector()
                    .collect(element, value, column);
            commands.addAll(callCommands);
        }
        return commands;
    }
}
