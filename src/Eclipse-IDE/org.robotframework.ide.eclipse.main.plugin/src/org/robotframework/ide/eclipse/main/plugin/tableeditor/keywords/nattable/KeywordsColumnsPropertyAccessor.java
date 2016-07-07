/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsDataProvider.RobotKeywordCallAdder;

import com.google.common.collect.ImmutableBiMap;

public class KeywordsColumnsPropertyAccessor implements IColumnPropertyAccessor<Object> {

    private static ImmutableBiMap<Integer, String> properties = ImmutableBiMap.of(0, "name");

    private final RobotEditorCommandsStack commandsStack;

    private int numberOfColumns;

    public KeywordsColumnsPropertyAccessor(final RobotEditorCommandsStack commandsStack) {
        this(commandsStack, 0);
    }

    public KeywordsColumnsPropertyAccessor(final RobotEditorCommandsStack commandsStack, final int numberOfColumns) {
        this.commandsStack = commandsStack;
        this.numberOfColumns = numberOfColumns;
    }

    @Override
    public Object getDataValue(final Object rowObject, final int columnIndex) {

        if (rowObject instanceof RobotKeywordCall) {
            RobotKeywordCall keywordCall = (RobotKeywordCall) rowObject;
            if (columnIndex == 0) {
                return keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW
                        ? keywordCall.getName() : "[" + keywordCall.getName() + "]";
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                List<String> arguments = keywordCall.getArguments();
                if (columnIndex - 1 < arguments.size()) {
                    return arguments.get(columnIndex - 1);
                }
            } else if (columnIndex == (numberOfColumns - 1)) {
                return keywordCall.getComment();
            }
        } else if (rowObject instanceof RobotKeywordDefinition) {
            RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) rowObject;
            if (columnIndex == 0) {
                return keywordDef.getName();
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
                if(argumentsSetting != null) {
                    List<String> arguments = argumentsSetting.getArguments();
                    if((columnIndex-1) < arguments.size()) {
                        return arguments.get(columnIndex-1);
                    }
                }
            } else if (columnIndex == (numberOfColumns - 1)) {
                return keywordDef.getComment();
            }
        } else if (columnIndex == 0 && rowObject instanceof RobotKeywordCallAdder) {
            return "...";
        }

        return "";
    }

    @Override
    public void setDataValue(Object rowObject, int columnIndex, Object newValue) {

        final String value = newValue != null ? (String) newValue : "";

        if (rowObject instanceof RobotKeywordCall) {
            RobotKeywordCall keywordCall = (RobotKeywordCall) rowObject;
            if (columnIndex == 0) {
                commandsStack.execute(new SetKeywordCallNameCommand(keywordCall, value));
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                commandsStack.execute(new SetKeywordCallArgumentCommand(keywordCall, columnIndex - 1, value));
            } else if (columnIndex == (numberOfColumns - 1)) {
                commandsStack.execute(new SetKeywordCallCommentCommand(keywordCall, value));
            }
        } else if (rowObject instanceof RobotKeywordDefinition) {
            RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) rowObject;
            if (columnIndex == 0) {
                if (!value.isEmpty()) {
                    commandsStack.execute(new SetKeywordDefinitionNameCommand(keywordDef, value));
                }
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                commandsStack.execute(new SetKeywordDefinitionArgumentCommand(keywordDef, columnIndex - 1, value));
            }
        }

    }

    @Override
    public int getColumnCount() {
        return numberOfColumns;
    }

    @Override
    public String getColumnProperty(int columnIndex) {
        String property = properties.get(columnIndex);
        return property;
    }

    @Override
    public int getColumnIndex(String propertyName) {
        return properties.inverse().get(propertyName);
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

}
