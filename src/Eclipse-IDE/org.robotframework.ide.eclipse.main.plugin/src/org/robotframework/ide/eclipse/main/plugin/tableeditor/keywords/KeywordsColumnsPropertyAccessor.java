/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshCodeHolderSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordDefinitionNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordSettingCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

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
            final RobotKeywordCall keywordCall = (RobotKeywordCall) rowObject;
            final ModelType modelType = keywordCall.getLinkedElement().getModelType();
            if (columnIndex == 0) {
                return modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW || modelType == ModelType.UNKNOWN
                        ? keywordCall.getName() : "[" + keywordCall.getName() + "]";
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                final List<String> arguments = keywordCall.getArguments();
                if (modelType == ModelType.USER_KEYWORD_DOCUMENTATION) {
                    if (columnIndex == 1) {
                        return getDocumentationText(keywordCall);
                    }
                } else if (columnIndex - 1 < arguments.size()) {
                    return arguments.get(columnIndex - 1);
                }
            } else if (columnIndex == (numberOfColumns - 1)) {
                return keywordCall.getComment();
            }
        } else if (rowObject instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) rowObject;
            if (columnIndex == 0) {
                return keywordDef.getName();
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
                if (argumentsSetting != null) {
                    final List<String> arguments = argumentsSetting.getArguments();
                    if ((columnIndex - 1) < arguments.size()) {
                        return arguments.get(columnIndex - 1);
                    }
                }
            } else if (columnIndex == (numberOfColumns - 1)) {
                return keywordDef.getComment();
            }
        }

        return "";
    }

    @Override
    public void setDataValue(final Object rowObject, final int columnIndex, final Object newValue) {

        String value = newValue != null ? (String) newValue : "";

        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) rowObject;

            if (isCandidateForNewKeywordSetting(keywordCall, value, columnIndex)) {
                final boolean isKeywordSettingCreated = createNewKeywordSettingAndRemoveOldExeRow(value, keywordCall);
                if (isKeywordSettingCreated) {
                    return;
                }
            }

            final ModelType modelType = keywordCall.getLinkedElement().getModelType();
            if (modelType == ModelType.USER_KEYWORD_EXECUTABLE_ROW || modelType == ModelType.UNKNOWN) {
                if (columnIndex == 0) {
                    commandsStack.execute(new SetKeywordCallNameCommand(keywordCall, value));
                } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                    commandsStack.execute(new SetKeywordCallArgumentCommand(keywordCall, columnIndex - 1, value));
                } else if (columnIndex == (numberOfColumns - 1)) {
                    commandsStack.execute(new SetKeywordCallCommentCommand(keywordCall, value));
                }
            } else {
                if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                    commandsStack.execute(new SetKeywordSettingArgumentCommand(keywordCall, columnIndex - 1, value));
                } else if (columnIndex == (numberOfColumns - 1)) {
                    commandsStack.execute(new SetKeywordSettingCommentCommand(keywordCall, value));
                }
            }
        } else if (rowObject instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) rowObject;
            if (columnIndex == 0) {
                commandsStack.execute(new SetKeywordDefinitionNameCommand(keywordDef, value.isEmpty() ? "\\" : value));
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                commandsStack.execute(new SetKeywordDefinitionArgumentCommand(keywordDef, columnIndex - 1, value));
            }
        }

    }

    private boolean isCandidateForNewKeywordSetting(final RobotKeywordCall keywordCall, final String value,
            final int columnIndex) {
        return value.startsWith("[") && value.endsWith("]") && columnIndex == 0
                && keywordCall.getLinkedElement().getModelType() == ModelType.USER_KEYWORD_EXECUTABLE_ROW;
    }

    @Override
    public int getColumnCount() {
        return numberOfColumns;
    }

    @Override
    public String getColumnProperty(final int columnIndex) {
        final String property = properties.get(columnIndex);
        return property;
    }

    @Override
    public int getColumnIndex(final String propertyName) {
        return properties.inverse().get(propertyName);
    }

    public void setNumberOfColumns(final int numberOfColumns) {
        this.numberOfColumns = numberOfColumns;
    }

    private boolean createNewKeywordSettingAndRemoveOldExeRow(final String value, final RobotKeywordCall keywordCall) {
        final RobotTokenType tokenType = RobotTokenType.findTypeOfDeclarationForKeywordSettingTable(value);
        if (tokenType != RobotTokenType.UNKNOWN && tokenType != RobotTokenType.KEYWORD_SETTING_ARGUMENTS) {
            final RobotKeywordDefinition keywordDefinition = (RobotKeywordDefinition) keywordCall.getParent();
            keywordDefinition.getLinkedElement()
                    .removeExecutableRow((RobotExecutableRow<UserKeyword>) keywordCall.getLinkedElement());

            final List<RobotKeywordCall> children = keywordDefinition.getChildren();
            final int index = children.indexOf(keywordCall);
            children.remove(keywordCall);

            commandsStack.execute(new CreateFreshCodeHolderSettingCommand(keywordDefinition, index, value,
                    keywordCall.getArguments()));
            return true;
        }
        return false;
    }

    private String getDocumentationText(final RobotKeywordCall keywordCall) {
        return DocumentationServiceHandler.toEditConsolidated((IDocumentationHolder) keywordCall.getLinkedElement());
    }
}
