/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Optional;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

class ModelRowUtilities {

    private static final EnumSet<ModelType> LOCAL_SETTING_TYPES = EnumSet.of(ModelType.TEST_CASE_DOCUMENTATION,
            ModelType.TEST_CASE_SETUP, ModelType.TEST_CASE_TAGS, ModelType.TEST_CASE_TEARDOWN,
            ModelType.TEST_CASE_TEMPLATE, ModelType.TEST_CASE_TIMEOUT, ModelType.TASK_DOCUMENTATION,
            ModelType.TASK_SETUP, ModelType.TASK_TAGS, ModelType.TASK_TEARDOWN, ModelType.TASK_TEMPLATE,
            ModelType.TASK_TIMEOUT, ModelType.USER_KEYWORD_ARGUMENTS, ModelType.USER_KEYWORD_DOCUMENTATION,
            ModelType.USER_KEYWORD_RETURN, ModelType.USER_KEYWORD_TAGS, ModelType.USER_KEYWORD_TEARDOWN,
            ModelType.USER_KEYWORD_TIMEOUT);

    private static final EnumSet<ModelType> KEYWORD_BASED_LOCAL_SETTING_TYPES = EnumSet.of(ModelType.TEST_CASE_SETUP,
            ModelType.TEST_CASE_TEARDOWN, ModelType.TEST_CASE_TEMPLATE, ModelType.TASK_SETUP, ModelType.TASK_TEARDOWN,
            ModelType.TASK_TEMPLATE, ModelType.USER_KEYWORD_TEARDOWN);

    private static final EnumSet<ModelType> TEMPLATE_LOCAL_SETTING_TYPES = EnumSet.of(ModelType.TEST_CASE_TEMPLATE,
            ModelType.TASK_TEMPLATE);

    private static final EnumSet<RobotTokenType> KEYWORD_BASED_GENERAL_SETTING_TYPES = EnumSet.of(
            RobotTokenType.SETTING_SUITE_SETUP_DECLARATION, RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION,
            RobotTokenType.SETTING_TEST_SETUP_DECLARATION, RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION,
            RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION, RobotTokenType.SETTING_TASK_SETUP_DECLARATION,
            RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION, RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION);

    private static final EnumSet<RobotTokenType> TEMPLATE_GENERAL_SETTING_TYPES = EnumSet
            .of(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION, RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION);

    static boolean isLocalSetting(final IRowDataProvider<?> dataProvider, final int row) {
        return isRobotKeywordCallWithType(dataProvider, row, LOCAL_SETTING_TYPES);
    }

    static boolean isKeywordBasedLocalSetting(final IRowDataProvider<?> dataProvider, final int row) {
        return isRobotKeywordCallWithType(dataProvider, row, KEYWORD_BASED_LOCAL_SETTING_TYPES);
    }

    static boolean isTemplateLocalSetting(final IRowDataProvider<?> dataProvider, final int row) {
        return isRobotKeywordCallWithType(dataProvider, row, TEMPLATE_LOCAL_SETTING_TYPES);
    }

    private static boolean isRobotKeywordCallWithType(final IRowDataProvider<?> dataProvider, final int row,
            final EnumSet<ModelType> types) {
        final Object rowObject = dataProvider.getRowObject(row);
        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) rowObject;
            final ModelType modelType = call.getLinkedElement().getModelType();
            return types.contains(modelType);
        }
        return false;
    }

    static boolean isKeywordBasedGeneralSetting(final IRowDataProvider<?> dataProvider, final int row) {
        return isGeneralSettingWithType(dataProvider, row, KEYWORD_BASED_GENERAL_SETTING_TYPES);
    }

    static boolean isTemplateGeneralSetting(final IRowDataProvider<?> dataProvider, final int row) {
        return isGeneralSettingWithType(dataProvider, row, TEMPLATE_GENERAL_SETTING_TYPES);
    }

    private static boolean isGeneralSettingWithType(final IRowDataProvider<?> dataProvider, final int row,
            final EnumSet<RobotTokenType> types) {
        final Object rowObject = dataProvider.getRowObject(row);
        if (rowObject instanceof Entry) {
            final String settingName = (String) ((Entry<?, ?>) rowObject).getKey();
            final RobotTokenType actualType = RobotTokenType.findTypeOfDeclarationForSettingTable(settingName);
            return types.contains(actualType);
        }
        return false;
    }

    static Optional<String> getTemplateInUse(final IRowDataProvider<?> dataProvider, final int row) {
        final Object rowObject = dataProvider.getRowObject(row);
        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) rowObject;
            final Optional<String> template = getTemplateInUse(call.getLinkedElement());
            return template.isPresent() ? template : getTemplateInUse(call.getLinkedElement().getParent());
        }
        return Optional.empty();
    }

    private static Optional<String> getTemplateInUse(final Object element) {
        if (element instanceof TestCase) {
            return ((TestCase) element).getTemplateKeywordName();
        } else if (element instanceof Task) {
            return ((Task) element).getTemplateKeywordName();
        } else {
            return Optional.empty();
        }
    }
}
