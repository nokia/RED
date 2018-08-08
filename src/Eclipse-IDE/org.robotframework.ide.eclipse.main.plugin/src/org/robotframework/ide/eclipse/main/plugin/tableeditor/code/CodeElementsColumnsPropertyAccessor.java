/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.List;
import java.util.Optional;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.IDocumentationHolder;
import org.rf.ide.core.testdata.model.presenter.DocumentationServiceHandler;
import org.rf.ide.core.testdata.model.table.LocalSetting;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.EditorCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

import com.google.common.collect.ImmutableBiMap;

public class CodeElementsColumnsPropertyAccessor implements IColumnPropertyAccessor<Object> {

    private static ImmutableBiMap<Integer, String> properties = ImmutableBiMap.of(0, "name");

    private final RobotEditorCommandsStack commandsStack;

    private final TableCommandsCollector commandsCollector;

    protected int numberOfColumns;

    public CodeElementsColumnsPropertyAccessor(final RobotEditorCommandsStack commandsStack,
            final TableCommandsCollector commandsCollector) {
        this.commandsStack = commandsStack;
        this.commandsCollector = commandsCollector;
    }

    @Override
    public String getDataValue(final Object rowObject, final int columnIndex) {
        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall keywordCall = (RobotKeywordCall) rowObject;

            if (columnIndex > 0 && keywordCall instanceof RobotDefinitionSetting
                    && ((RobotDefinitionSetting) keywordCall).isDocumentation()) {
                return columnIndex == 1 ? getDocumentationText(keywordCall).replaceAll("\\s+", " ").trim() : "";
            }

            final List<RobotToken> execRowView = ExecutablesRowHolderCommentService.execRowView(keywordCall);
            if (columnIndex < execRowView.size()) {
                return execRowView.get(columnIndex).getText();
            }
        } else if (rowObject instanceof RobotCodeHoldingElement<?>) {
            final RobotCodeHoldingElement<?> holder = (RobotCodeHoldingElement<?>) rowObject;
            return columnIndex == 0 ? holder.getName() : "";
        }
        return "";
    }

    private String getDocumentationText(final RobotKeywordCall keywordCall) {
        return DocumentationServiceHandler.toEditConsolidated(getHolder(keywordCall.getLinkedElement()));
    }

    private IDocumentationHolder getHolder(final AModelElement<?> element) {
        if (element instanceof IDocumentationHolder) {
            return (IDocumentationHolder) element;
        } else {
            final LocalSetting<?> setting = (LocalSetting<?>) element;
            return setting.adaptTo(IDocumentationHolder.class);
        }
    }

    @Override
    public void setDataValue(final Object rowObject, final int columnIndex, final Object newValue) {
        if (rowObject instanceof RobotElement) {
            commandsCollector
                    .collectForChange((RobotElement) rowObject, (String) newValue, columnIndex, numberOfColumns)
                    .ifPresent(commandsStack::execute);
        }
    }

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

    public static interface TableCommandsCollector {

        public default Optional<? extends EditorCommand> collectForRemoval(final RobotElement element,
                final int column, final int numberOfColumns) {
            return collect(element, null, column, numberOfColumns);
        }

        public default Optional<? extends EditorCommand> collectForChange(final RobotElement element,
                final String newValue, final int column, final int numberOfColumns) {
            return collect(element, newValue, column, numberOfColumns);
        }

        public Optional<? extends EditorCommand> collect(final RobotElement element, final String value,
                final int column, final int numberOfColumns);
    }
}
