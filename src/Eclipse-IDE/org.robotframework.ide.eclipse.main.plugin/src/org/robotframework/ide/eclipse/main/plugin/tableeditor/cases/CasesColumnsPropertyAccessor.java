/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.CreateFreshKeywordCallCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetKeywordCallNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.CreateFreshCaseSettingCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseNameCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseSettingArgumentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseSettingCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.cases.SetCaseSettingNameCommand;
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

            if (isExecutable(call)) {
                if (columnIndex == 0 && looksLikeSetting(value)) {
                    changeToSetting(value, call);
                } else if (columnIndex == 0) {
                    commandsStack.execute(new SetKeywordCallNameCommand(call, value));
                } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                    commandsStack.execute(new SetKeywordCallArgumentCommand(call, columnIndex - 1, value));
                } else if (columnIndex == (numberOfColumns - 1)) {
                    commandsStack.execute(new SetKeywordCallCommentCommand(call, value));
                }
            } else {
                final RobotDefinitionSetting setting = (RobotDefinitionSetting) call;
                if (columnIndex == 0 && !looksLikeSetting(value)) {
                    changeToCall(value, setting);
                } else if (columnIndex == 0) {
                    commandsStack.execute(new SetCaseSettingNameCommand(setting, value));
                } else if (columnIndex > 0 && columnIndex < (numberOfColumns - 1)) {
                    commandsStack.execute(new SetCaseSettingArgumentCommand(setting, columnIndex - 1, value));
                } else if (columnIndex == (numberOfColumns - 1)) {
                    commandsStack.execute(new SetCaseSettingCommentCommand(setting, value));
                }
            }
        }
    }

    private boolean isExecutable(final RobotKeywordCall call) {
        return call.getLinkedElement().getModelType() == ModelType.TEST_CASE_EXECUTABLE_ROW;
    }

    private boolean looksLikeSetting(final String value) {
        return value.startsWith("[") && value.endsWith("]");
    }

    @SuppressWarnings("unchecked")
    private void changeToSetting(final String value, final RobotKeywordCall call) {
        RobotTokenType tokenType = RobotTokenType.findTypeOfDeclarationForTestCaseSettingTable(value);
        tokenType = tokenType == RobotTokenType.UNKNOWN ? RobotTokenType.TEST_CASE_SETTING_UNKNOWN_DECLARATION
                : tokenType;

        final RobotCase testCase = (RobotCase) call.getParent();
        testCase.getLinkedElement().removeExecutableRow((RobotExecutableRow<TestCase>) call.getLinkedElement());

        final List<RobotKeywordCall> children = testCase.getChildren();
        final int index = children.indexOf(call);
        children.remove(call);

        commandsStack.execute(new CreateFreshCaseSettingCommand(testCase, index, value, call.getArguments()));
    }

    @SuppressWarnings("unchecked")
    private void changeToCall(final String value, final RobotDefinitionSetting setting) {
        final RobotCase testCase = (RobotCase) setting.getParent();

        final List<RobotKeywordCall> children = testCase.getChildren();
        final int index = children.indexOf(setting);
        children.remove(setting);

        final List<RobotKeywordCall> executablesBeforeSetting = testCase.getExecutableRows(0, index);
        int modelIndex;
        if (executablesBeforeSetting.isEmpty()) {
            modelIndex = 0;
        } else {
            final AModelElement<?> lastExecutable = executablesBeforeSetting.get(executablesBeforeSetting.size() - 1)
                    .getLinkedElement();
            modelIndex = testCase.getLinkedElement().getExecutionContext().indexOf(lastExecutable) + 1;
        }
        final TestCase modelCase = (TestCase) setting.getLinkedElement().getParent();
        modelCase.removeUnitSettings((AModelElement<TestCase>) setting.getLinkedElement());

        commandsStack.execute(new CreateFreshKeywordCallCommand(testCase, value, modelIndex, index));
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
