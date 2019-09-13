/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.assist;

import java.util.EnumSet;
import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

class ModelRowUtilities {

    static boolean isTemplateLocalSetting(final IRowDataProvider<?> dataProvider, final int row) {
        final Object rowObject = dataProvider.getRowObject(row);
        if (rowObject instanceof RobotKeywordCall) {
            final RobotKeywordCall call = (RobotKeywordCall) rowObject;
            final ModelType modelType = call.getLinkedElement().getModelType();
            return EnumSet.of(ModelType.TEST_CASE_TEMPLATE, ModelType.TASK_TEMPLATE).contains(modelType);
        }
        return false;
    }

    static boolean isKeywordBasedGeneralSetting(final IRowDataProvider<?> dataProvider, final int row) {
        final Entry<?, ?> rowEntry = (Entry<?, ?>) dataProvider.getRowObject(row);
        final String settingName = (String) rowEntry.getKey();
        final RobotTokenType actualType = RobotTokenType.findTypeOfDeclarationForSettingTable(settingName);

        return EnumSet
                .of(RobotTokenType.SETTING_SUITE_SETUP_DECLARATION, RobotTokenType.SETTING_SUITE_TEARDOWN_DECLARATION,
                        RobotTokenType.SETTING_TEST_SETUP_DECLARATION, RobotTokenType.SETTING_TEST_TEARDOWN_DECLARATION,
                        RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION, RobotTokenType.SETTING_TASK_SETUP_DECLARATION,
                        RobotTokenType.SETTING_TASK_TEARDOWN_DECLARATION,
                        RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION)
                .contains(actualType);
    }

    static boolean isTemplateGeneralSetting(final IRowDataProvider<?> dataProvider, final int row) {
        final Entry<?, ?> rowEntry = (Entry<?, ?>) dataProvider.getRowObject(row);
        final String settingName = (String) rowEntry.getKey();
        final RobotTokenType actualType = RobotTokenType.findTypeOfDeclarationForSettingTable(settingName);

        return EnumSet
                .of(RobotTokenType.SETTING_TEST_TEMPLATE_DECLARATION, RobotTokenType.SETTING_TASK_TEMPLATE_DECLARATION)
                .contains(actualType);
    }
}
