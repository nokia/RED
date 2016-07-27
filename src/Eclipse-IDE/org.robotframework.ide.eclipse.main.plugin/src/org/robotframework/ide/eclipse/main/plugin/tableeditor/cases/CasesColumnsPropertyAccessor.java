/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseNameCommand;
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
                return keywordCall.getLinkedElement().getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW
                        ? keywordCall.getName() : "[" + keywordCall.getName() + "]";
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
        final String value = newValue != null ? (String) newValue : "";

        if (rowObject instanceof RobotCase && !value.isEmpty()) {
            final RobotCase testCase = (RobotCase) rowObject;
            if (columnIndex == 0) {
                commandsStack.execute(new SetCaseNameCommand(testCase, value));
            }
        } else if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) rowObject;

            if (columnIndex == 0) {
                commandsStack.execute(new SetCaseKeywordCallNameCommand(call, value));
            } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                commandsStack.execute(new SetCaseKeywordCallArgumentCommand(call, columnIndex - 1, value));
            } else {
                commandsStack.execute(new SetCaseKeywordCallCommentCommand(call, value));
            }
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
}
