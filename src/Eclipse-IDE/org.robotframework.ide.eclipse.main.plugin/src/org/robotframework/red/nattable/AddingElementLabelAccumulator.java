/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;

/**
 * @author Michal Anglart
 *
 */
public class AddingElementLabelAccumulator implements IConfigLabelAccumulator {

    public static final String ELEMENT_ADDER_CONFIG_LABEL = "ELEMENT_ADDER";
    public static final String ELEMENT_MULTISTATE_ADDER_CONFIG_LABEL = "ELEMENT_ADDER_MULTISTATE";
    public static final String ELEMENT_ADDER_NESTED_CONFIG_LABEL = "ELEMENT_ADDER_NESTED";

    public static final String ELEMENT_ADDER_ROW_CONFIG_LABEL = "ELEMENT_ADDER_ROW";
    public static final String ELEMENT_ADDER_ROW_NESTED_CONFIG_LABEL = "ELEMENT_ADDER_ROW_NESTED";

    private final IDataProvider dataProvider;

    private final boolean multistate;

    public AddingElementLabelAccumulator(final IDataProvider dataProvider) {
        this(dataProvider, false);
    }

    public AddingElementLabelAccumulator(final IDataProvider dataProvider, final boolean multistate) {
        this.dataProvider = dataProvider;
        this.multistate = multistate;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (dataProvider instanceof IFilteringDataProvider && ((IFilteringDataProvider) dataProvider).isFilterSet()) {
            return;
        }
        if (dataProvider instanceof IRowDataProvider<?>) {
            final IRowDataProvider<?> rowDataProvider = (IRowDataProvider<?>) dataProvider;
            final Object rowObject = rowDataProvider.getRowObject(rowPosition);
            if (rowObject instanceof AddingToken && ((AddingToken) rowObject).isNested()) {
                if (columnPosition == 0) {
                    configLabels.addLabel(ELEMENT_ADDER_NESTED_CONFIG_LABEL);
                }
                configLabels.addLabel(ELEMENT_ADDER_ROW_NESTED_CONFIG_LABEL);
            }
        }

        if (columnPosition == 0 && dataProvider.getRowCount() - 1 == rowPosition) {
            configLabels.addLabel(multistate ? ELEMENT_MULTISTATE_ADDER_CONFIG_LABEL : ELEMENT_ADDER_CONFIG_LABEL);
        }
        if (dataProvider.getRowCount() - 1 == rowPosition) {
            configLabels.addLabel(ELEMENT_ADDER_ROW_CONFIG_LABEL);
        }
    }
}
