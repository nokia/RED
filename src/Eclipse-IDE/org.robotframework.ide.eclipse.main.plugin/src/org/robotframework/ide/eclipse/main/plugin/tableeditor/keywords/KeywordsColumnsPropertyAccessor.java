/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.List;

import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsColumnsPropertyAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeTableValuesChangingCommandsCollector;

class KeywordsColumnsPropertyAccessor extends CodeElementsColumnsPropertyAccessor {

    KeywordsColumnsPropertyAccessor(final RobotEditorCommandsStack commandsStack,
            final CodeTableValuesChangingCommandsCollector commandsCollector) {
        super(commandsStack, commandsCollector);
    }

    @Override
    public String getDataValue(final Object rowObject, final int columnIndex) {
        if (rowObject instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) rowObject;
            if (columnIndex == 0) {
                return keywordDef.getName();

            } else if (columnIndex > 0 && columnIndex <= (numberOfColumns - 1)) {
                final RobotKeywordCall argumentsSetting = keywordDef.getArgumentsSetting();
                if (argumentsSetting != null) {
                    final List<String> arguments = argumentsSetting.getArguments();
                    if ((columnIndex - 1) < arguments.size()) {
                        return arguments.get(columnIndex - 1);
                    }
                }
            }
            return "";
        } else {
            return super.getDataValue(rowObject, columnIndex);
        }
    }
}
