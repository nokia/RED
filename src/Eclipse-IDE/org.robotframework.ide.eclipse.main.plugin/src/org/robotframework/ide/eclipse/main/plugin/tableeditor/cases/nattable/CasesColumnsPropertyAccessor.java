/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.nattable;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

import com.google.common.collect.ImmutableBiMap;

public class CasesColumnsPropertyAccessor implements IColumnPropertyAccessor<Object> {

    private static ImmutableBiMap<Integer, String> properties = ImmutableBiMap.of(0, "name");

    private final RobotEditorCommandsStack commandsStack;

    private int numberOfColumns;

    public CasesColumnsPropertyAccessor(final RobotEditorCommandsStack commandsStack, final int numberOfColumns) {
        this.commandsStack = commandsStack;
        this.numberOfColumns = numberOfColumns;
    }

    @Override
    public Object getDataValue(final Object rowObject, final int columnIndex) {
        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) rowObject;

            if (columnIndex == 0) {
                return keywordCall.getLabel();
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                final List<String> arguments = keywordCall.getArguments();
                if (columnIndex - 1 < arguments.size()) {
                    return arguments.get(columnIndex - 1);
                }
            } else if (columnIndex == (numberOfColumns - 1)) {
                return keywordCall.getComment();
            }
        } else if (rowObject instanceof RobotCase) {
            final RobotCase robotCase = (RobotCase) rowObject;

            if (columnIndex == 0) {
                return robotCase.getName();
            } else if (columnIndex == (numberOfColumns - 1)) {
                return robotCase.getComment();
            }
        }
        return "";
    }

    @Override
    public void setDataValue(final Object rowObject, final int columnIndex, final Object newValue) {
        // final String value = newValue != null ? (String) newValue : "";
        //
        // if (rowObject instanceof RobotKeywordCall) {
        // final RobotKeywordCall keywordCall = (RobotKeywordCall) rowObject;
        //
        // if (value.startsWith("[") && value.endsWith("]")
        // && keywordCall.getLinkedElement().getModelType() ==
        // ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
        // createNewKeywordSettingAndRemoveOldExeRow(value, keywordCall);
        // return;
        // }
        //
        // if (keywordCall.getLinkedElement().getModelType() ==
        // ModelType.USER_KEYWORD_EXECUTABLE_ROW) {
        // if (columnIndex == 0) {
        // commandsStack.execute(new SetKeywordCallNameCommand(keywordCall, value));
        // } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
        // commandsStack.execute(new SetKeywordCallArgumentCommand(keywordCall, columnIndex - 1,
        // value));
        // } else if (columnIndex == (numberOfColumns - 1)) {
        // commandsStack.execute(new SetKeywordCallCommentCommand(keywordCall, value));
        // }
        // } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
        // commandsStack.execute(new SetKeywordSettingArgumentCommand(keywordCall, columnIndex - 1,
        // value));
        // }
        // } else if (rowObject instanceof RobotKeywordDefinition && !value.isEmpty()) {
        // final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) rowObject;
        // if (columnIndex == 0) {
        // commandsStack.execute(new SetKeywordDefinitionNameCommand(keywordDef, value));
        // } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
        // commandsStack.execute(new SetKeywordDefinitionArgumentCommand(keywordDef, columnIndex -
        // 1, value));
        // }
        // }
    }

    // private void createNewKeywordSettingAndRemoveOldExeRow(final String value, final
    // RobotKeywordCall keywordCall) {
    // final RobotTokenType tokenType =
    // RobotTokenType.findTypeOfDeclarationForKeywordSettingTable(value);
    // if (tokenType != RobotTokenType.UNKNOWN && tokenType !=
    // RobotTokenType.KEYWORD_SETTING_ARGUMENTS
    // && tokenType != RobotTokenType.KEYWORD_SETTING_DOCUMENTATION) {
    // final RobotKeywordDefinition keywordDefinition = (RobotKeywordDefinition)
    // keywordCall.getParent();
    // keywordDefinition.getLinkedElement()
    // .removeExecutableRow((RobotExecutableRow<UserKeyword>) keywordCall.getLinkedElement());
    //
    // final List<RobotKeywordCall> children = keywordDefinition.getChildren();
    // final int index = children.indexOf(keywordCall);
    // children.remove(keywordCall);
    //
    // commandsStack.execute(
    // new CreateFreshKeywordSettingCommand(keywordDefinition, index, value,
    // keywordCall.getArguments()));
    // }
    // }

    @Override
    public int getColumnCount() {
        return numberOfColumns;
    }

    public void setColumnCount(final int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    @Override
    public String getColumnProperty(final int columnIndex) {
        return properties.get(columnIndex);
    }

    @Override
    public int getColumnIndex(final String propertyName) {
        return properties.inverse().get(propertyName);
    }
}
