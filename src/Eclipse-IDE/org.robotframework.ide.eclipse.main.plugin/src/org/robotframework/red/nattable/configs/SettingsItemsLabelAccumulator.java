/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;

/**
 * @author lwlodarc
 *
 */
public class SettingsItemsLabelAccumulator implements IConfigLabelAccumulator {

    public static final String SETTING_CONFIG_LABEL = "SETTING";

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        if (columnPosition == 0) {
            // don't worry about inactive setting - this case would be served by another
            // method
            configLabels.addLabel(SETTING_CONFIG_LABEL);
        }
    }
}