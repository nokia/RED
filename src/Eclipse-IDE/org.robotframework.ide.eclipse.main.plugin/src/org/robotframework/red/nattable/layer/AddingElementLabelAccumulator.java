/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.layer;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.red.nattable.configs.AddingElementConfiguration;

/**
 * @author Michal Anglart
 *
 */
public class AddingElementLabelAccumulator implements IConfigLabelAccumulator {

    private final IDataProvider dataProvider;

    public AddingElementLabelAccumulator(final IDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (columnPosition == 0 && dataProvider.getRowCount() - 1 == rowPosition) {
            configLabels.addLabel(AddingElementConfiguration.ELEMENT_ADDER_CONFIG_LABEL);
        }
        if (dataProvider.getRowCount() - 1 == rowPosition) {
            configLabels.addLabel(AddingElementConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
        }
    }
}
