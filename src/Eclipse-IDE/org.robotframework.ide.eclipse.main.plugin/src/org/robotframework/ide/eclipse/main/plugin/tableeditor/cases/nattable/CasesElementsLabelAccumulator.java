/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.nattable;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;

public class CasesElementsLabelAccumulator implements IConfigLabelAccumulator {

    public static final String CASE_CONFIG_LABEL = "CASE";
    public static final String CASE_SETTING_CONFIG_LABEL = "CASE_SETTING";
    public static final String CASE_CALL_CONFIG_LABEL = "CASE_CALL";

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
            } else if (rowObject instanceof RobotCase) {
                configLabels.addLabel(CASE_CONFIG_LABEL);
            }
        }
    }
}
