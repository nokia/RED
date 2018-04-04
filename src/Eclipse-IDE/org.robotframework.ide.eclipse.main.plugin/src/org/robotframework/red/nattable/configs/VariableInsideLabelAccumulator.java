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
public class VariableInsideLabelAccumulator implements IConfigLabelAccumulator {

    public static final String POSSIBLE_VARIABLE_INSIDE_CONFIG_LABEL = "POSSIBLE_VARIABLE_INSIDE";

    @Override
    public void accumulateConfigLabels(LabelStack configLabels, int columnPosition, int rowPosition) {
        if (columnPosition > 0) {
            configLabels.addLabel(POSSIBLE_VARIABLE_INSIDE_CONFIG_LABEL);
        }
    }
}