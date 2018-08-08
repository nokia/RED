/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.Optional;

import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTask;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetCodeHolderNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsColumnsPropertyAccessor.TableCommandsCollector;

public class CodeTableValuesChangingCommandsCollector implements TableCommandsCollector {

    @Override
    public Optional<? extends EditorCommand> collect(final RobotElement element, final String value, final int column,
            final int numberOfColumns) {

        if (element instanceof RobotCase) {
            final RobotCase testCase = (RobotCase) element;
            return column == 0 ? Optional.of(new SetCodeHolderNameCommand(testCase, value)) : Optional.empty();

        } else if (element instanceof RobotTask) {
            final RobotTask task = (RobotTask) element;
            return column == 0 ? Optional.of(new SetCodeHolderNameCommand(task, value)) : Optional.empty();

        } else if (element instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) element;
            if (column == 0) {
                return Optional.of(new SetCodeHolderNameCommand(keywordDef, value));

            } else if (column > 0 && column <= numberOfColumns - 1) {
                return Optional.of(new SetKeywordDefinitionArgumentCommand(keywordDef, column - 1, value));
            }
            return Optional.empty();
        } else {
            return new KeywordCallsTableValuesChangingCommandsCollector().collect(element, value, column);
        }
    }
}
