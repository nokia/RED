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
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService;

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

            if (columnIndex > 0 && modelType == ModelType.USER_KEYWORD_DOCUMENTATION) {
                if (columnIndex == 1) {
                    return getDocumentationText(keywordCall).replaceAll("\\s+", " ").trim();
                } else {
                    return "";
                }
            }

            final List<RobotToken> execRowView = ExecutablesRowHolderCommentService.execRowView(keywordCall);
            if (columnIndex < execRowView.size()) {
                return execRowView.get(columnIndex).getText();
            }
        } else if (rowObject instanceof RobotKeywordDefinition) {
            final RobotKeywordDefinition keywordDef = (RobotKeywordDefinition) rowObject;
            if (columnIndex == 0) {
                return keywordDef.getName();
            } else if (columnIndex > 0 && columnIndex <= (numberOfColumns - 1)) {
                final RobotDefinitionSetting argumentsSetting = keywordDef.getArgumentsSetting();
                if (argumentsSetting != null) {
                    final List<String> arguments = argumentsSetting.getArguments();
                    if ((columnIndex - 1) < arguments.size()) {
                        return arguments.get(columnIndex - 1);
                    }
                }
            }
        }

        return "";
    }

    @Override
    public void setDataValue(final Object rowObject, final int columnIndex, final Object newValue) {
        if (rowObject instanceof RobotElement) {
            new KeywordsTableValuesChangingCommandsCollector()
                    .collectForChange((RobotElement) rowObject, (String) newValue, columnIndex, numberOfColumns)
                    .ifPresent(commandsStack::execute);
        }
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

    private String getDocumentationText(final RobotKeywordCall keywordCall) {
        return DocumentationServiceHandler.toEditConsolidated((IDocumentationHolder) keywordCall.getLinkedElement());
    }
}
