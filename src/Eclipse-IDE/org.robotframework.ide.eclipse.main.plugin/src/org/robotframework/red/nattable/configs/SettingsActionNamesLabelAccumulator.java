/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;

/**
 * @author lwlodarc
 *
 */
public class SettingsActionNamesLabelAccumulator implements IConfigLabelAccumulator {

    private final IRowDataProvider<?> dataProvider;

    public SettingsActionNamesLabelAccumulator(final IRowDataProvider<?> dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        if (columnPosition == 1) {
            final RobotSetting setting = ((Entry<String, RobotSetting>) dataProvider
                    .getRowObject(rowPosition)).getValue();
            if (setting != null && setting.isKeywordBased()) {
                // don't worry about variable here - this case would be served by
                // SettingsVariablesLabelAccumulator
                configLabels.addLabel(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
            }
        }
    }
}