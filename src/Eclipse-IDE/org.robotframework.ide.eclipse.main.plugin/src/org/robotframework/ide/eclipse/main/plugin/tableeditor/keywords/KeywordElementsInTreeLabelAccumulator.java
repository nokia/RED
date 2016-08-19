/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;

public class KeywordElementsInTreeLabelAccumulator implements IConfigLabelAccumulator {

    public static final String KEYWORD_DEFINITION_CONFIG_LABEL = "KEYWORD_DEFINITION";

    public static final String KEYWORD_DEFINITION_ARGUMENT_CONFIG_LABEL = "KEYWORD_DEFINITION_ARGUMENT";

    public static final String KEYWORD_DEFINITION_SETTING_CONFIG_LABEL = "KEYWORD_DEFINITION_SETTING";

    public static final String KEYWORD_DEFINITION_SETTING_DOCUMENTATION_NOT_EDITABLE_LABEL = "KEYWORD_SETTING_DOCUMENTATION_NOT_EDITABLE";

    static final String KEYWORD_ASSIST_REQUIRED = "KEYWORD_ASSIST_REQUIRED";

    static final String VARIABLES_ASSIST_REQUIRED = "VARIABLES_ASSIST_REQUIRED";

    private final IRowDataProvider<Object> dataProvider;

    public KeywordElementsInTreeLabelAccumulator(final IRowDataProvider<Object> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        final Object rowObject = dataProvider.getRowObject(rowPosition);

        if (columnPosition == 0) {
            if (rowObject instanceof RobotDefinitionSetting) {
                configLabels.addLabel(KEYWORD_DEFINITION_SETTING_CONFIG_LABEL);
            } else if (rowObject instanceof RobotKeywordDefinition) {
                configLabels.addLabel(KEYWORD_DEFINITION_CONFIG_LABEL);
            } else {
                configLabels.addLabel(KEYWORD_ASSIST_REQUIRED);
            }
        } else if (columnPosition > 0 && columnPosition < dataProvider.getColumnCount() - 1) {
            if (rowObject instanceof RobotKeywordDefinition) {
                configLabels.addLabel(KEYWORD_DEFINITION_ARGUMENT_CONFIG_LABEL);
                configLabels.addLabel(VARIABLES_ASSIST_REQUIRED);
            } else if (rowObject instanceof RobotDefinitionSetting) {
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
                        configLabels.addLabel(KEYWORD_DEFINITION_SETTING_DOCUMENTATION_NOT_EDITABLE_LABEL);
                    }
                }
            } else {
                configLabels.addLabel(VARIABLES_ASSIST_REQUIRED);
            }
        }
    }

}
