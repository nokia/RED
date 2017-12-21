/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.Optional;

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

    public Optional<? extends EditorCommand> collectForRemoval(final RobotElement element, final int column,
            final int numberOfColumns) {
        return collect(element, null, column, numberOfColumns);
    }

    public Optional<? extends EditorCommand> collectForChange(final RobotElement element, final String newValue,
            final int column, final int numberOfColumns) {
        return collect(element, newValue, column, numberOfColumns);
    }

    private Optional<? extends EditorCommand> collect(final RobotElement element, final String value, final int column,
            final int numberOfColumns) {

        if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) element;
            if (column == 0) {
                return Optional.of(new SetKeywordDefinitionNameCommand(keywordDef, value));

            } else if (column > 0 && column <= numberOfColumns - 1) {
                return Optional.of(new SetKeywordDefinitionArgumentCommand(keywordDef, column - 1, value));
            }
            return Optional.empty();
        } else {
            return new KeywordCallsTableValuesChangingCommandsCollector().collect(element, value, column);
        }
    }
}
