/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.layer;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.robotframework.red.nattable.configs.AddingElementConfiguration;

public class AddingElementLabelAccumulatorTest {

    @Test
    public void whenAccumulatorAggregatesLabels_addingLabelIsOnlyAddedForLeftBottomCornerCell() {
        final int rows = 20;
        final int columns = 5;
        final AddingElementLabelAccumulator accumulator = new AddingElementLabelAccumulator(createProvider(columns, rows));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final boolean isInBottomRow = i == rows - 1;
                final boolean isInLeftBottomCorner = isInBottomRow && j == 0;

                final LabelStack configLabels = new LabelStack();
                accumulator.accumulateConfigLabels(configLabels, j, i);

                if (isInLeftBottomCorner) {
                    assertThat(configLabels.getLabels())
                            .containsOnly(AddingElementConfiguration.ELEMENT_ADDER_CONFIG_LABEL,
                                    AddingElementConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
                } else if (isInBottomRow) {
                    assertThat(configLabels.getLabels())
                            .containsOnly(AddingElementConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
                } else {
                    assertThat(configLabels.getLabels()).isEmpty();
                }
            }
        }
    }

    private static IDataProvider createProvider(final int columns, final int rows) {
        return new IDataProvider() {

            @Override
            public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
                // nothing to do
            }

            @Override
            public int getRowCount() {
                return rows;
            }

            @Override
            public Object getDataValue(final int columnIndex, final int rowIndex) {
                return "value";
            }

            @Override
            public int getColumnCount() {
                return columns;
            }
        };
    }
}
