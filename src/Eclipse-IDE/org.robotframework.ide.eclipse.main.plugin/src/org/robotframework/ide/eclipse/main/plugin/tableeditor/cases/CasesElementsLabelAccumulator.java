/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

public class CasesElementsLabelAccumulator implements IConfigLabelAccumulator {

    public static final String CASE_CONFIG_LABEL = "CASE";
    public static final String CASE_WITH_TEMPLATE_CONFIG_LABEL = "TEMPLATED_CASE";

    public static final String CASE_SETTING_CONFIG_LABEL = "CASE_SETTING";
    public static final String CASE_CALL_CONFIG_LABEL = "CASE_CALL";

    static final String KEYWORD_ASSIST_REQUIRED = "KEYWORD_ASSIST_REQUIRED";
    static final String VARIABLES_ASSIST_REQUIRED = "VARIABLES_ASSIST_REQUIRED";

    public static final String CELL_NOT_EDITABLE_LABEL = "CELL_NOT_EDITABLE";

    private final IRowDataProvider<Object> dataProvider;

    public CasesElementsLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        final Object rowObject = dataProvider.getRowObject(rowPosition);

        if (columnPosition == 0) {
            if (rowObject instanceof RobotDefinitionSetting) {
                configLabels.addLabel(CASE_SETTING_CONFIG_LABEL);
            } else if (rowObject instanceof RobotKeywordCall) {
                configLabels.addLabel(CASE_CALL_CONFIG_LABEL);
                configLabels.addLabel(KEYWORD_ASSIST_REQUIRED);
            } else if (rowObject instanceof RobotCase) {
                final RobotCase testCase = (RobotCase) rowObject;
                if (testCase.getTemplateInUse().isPresent()) {
                    configLabels.addLabel(CASE_WITH_TEMPLATE_CONFIG_LABEL);
                } else {
                    configLabels.addLabel(CASE_CONFIG_LABEL);
                }
            }
        } else if (columnPosition > 0 && columnPosition < dataProvider.getColumnCount() - 1) {
            if (rowObject instanceof RobotDefinitionSetting) {
                final RobotDefinitionSetting setting = (RobotDefinitionSetting) rowObject;
                if (columnPosition == 1) {
                    if (setting.isKeywordBased()) {
                        configLabels.addLabel(KEYWORD_ASSIST_REQUIRED);
                    } else {
                        configLabels.addLabel(VARIABLES_ASSIST_REQUIRED);
                    }
                } else {
                    configLabels.addLabel(VARIABLES_ASSIST_REQUIRED);
                    if (setting.isDocumentation()) {
                        configLabels.addLabel(CELL_NOT_EDITABLE_LABEL);
                    }
                }
            } else if (rowObject instanceof RobotKeywordCall) {
                configLabels.addLabel(VARIABLES_ASSIST_REQUIRED);
            } else if (rowObject instanceof RobotCase) {
                configLabels.addLabel(CELL_NOT_EDITABLE_LABEL);
            }
        } else {
            if (rowObject instanceof RobotCase) {
                configLabels.addLabel(CELL_NOT_EDITABLE_LABEL);
            }
        }
    }

}
