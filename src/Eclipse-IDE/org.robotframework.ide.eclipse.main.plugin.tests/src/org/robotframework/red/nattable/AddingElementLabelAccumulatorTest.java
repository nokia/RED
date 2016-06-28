/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.robotframework.red.nattable.configs.AddingElementStyleConfiguration;

public class AddingElementLabelAccumulatorTest {

    @Test
    public void whenDataProviderIsFilteringAndFilterIsDisabled_addingLabelsAreAdded() {
        final int rows = 20;
        final int columns = 5;
        final AddingElementLabelAccumulator accumulator = new AddingElementLabelAccumulator(
                createFilteringProvider(columns, rows, false));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final boolean isInBottomRow = i == rows - 1;
                final boolean isInLeftBottomCorner = isInBottomRow && j == 0;

                final LabelStack configLabels = new LabelStack();
                accumulator.accumulateConfigLabels(configLabels, j, i);

                if (isInLeftBottomCorner) {
                    assertThat(configLabels.getLabels()).containsOnly(
                            AddingElementStyleConfiguration.ELEMENT_ADDER_CONFIG_LABEL,
                            AddingElementStyleConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
                } else if (isInBottomRow) {
                    assertThat(configLabels.getLabels())
                            .containsOnly(AddingElementStyleConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
                } else {
                    assertThat(configLabels.getLabels()).isEmpty();
                }
            }
        }
    }

    @Test
    public void whenDataProviderIsFilteringAndFilterIsEnabled_addingLabelsAreNotAdded() {
        final int rows = 20;
        final int columns = 5;
        final AddingElementLabelAccumulator accumulator = new AddingElementLabelAccumulator(
                createFilteringProvider(columns, rows, true));

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final LabelStack configLabels = new LabelStack();
                accumulator.accumulateConfigLabels(configLabels, j, i);

                assertThat(configLabels.getLabels()).isEmpty();
            }
        }
    }

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
                            .containsOnly(AddingElementStyleConfiguration.ELEMENT_ADDER_CONFIG_LABEL,
                                    AddingElementStyleConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
                } else if (isInBottomRow) {
                    assertThat(configLabels.getLabels())
                            .containsOnly(AddingElementStyleConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
                } else {
                    assertThat(configLabels.getLabels()).isEmpty();
                }
            }
        }
    }

    @Test
    public void whenAccumulatorAggregatesLabels_multistateAddingLabelIsOnlyAddedForLeftBottomCornerCell() {
        final int rows = 20;
        final int columns = 5;
        final AddingElementLabelAccumulator accumulator = new AddingElementLabelAccumulator(
                createProvider(columns, rows), true);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final boolean isInBottomRow = i == rows - 1;
                final boolean isInLeftBottomCorner = isInBottomRow && j == 0;

                final LabelStack configLabels = new LabelStack();
                accumulator.accumulateConfigLabels(configLabels, j, i);

                if (isInLeftBottomCorner) {
                    assertThat(configLabels.getLabels()).containsOnly(
                            AddingElementStyleConfiguration.ELEMENT_MULTISTATE_ADDER_CONFIG_LABEL,
                            AddingElementStyleConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
                } else if (isInBottomRow) {
                    assertThat(configLabels.getLabels())
                            .containsOnly(AddingElementStyleConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
                } else {
                    assertThat(configLabels.getLabels()).isEmpty();
                }
            }
        }
    }

    private static IDataProvider createProvider(final int columns, final int rows) {
        return new DataProviderMock(rows, columns);
    }

    private static IDataProvider createFilteringProvider(final int columns, final int rows,
            final boolean filterIsEnabled) {
        return new FilteringDataProviderMock(rows, columns, filterIsEnabled);
    }

    private static class DataProviderMock implements IDataProvider {

        private final int rows;

        private final int columns;

        private DataProviderMock(final int rows, final int columns) {
            this.rows = rows;
            this.columns = columns;
        }

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
    }

    private static final class FilteringDataProviderMock extends DataProviderMock implements IFilteringDataProvider {

        private final boolean filterIsEnabled;

        public FilteringDataProviderMock(final int rows, final int columns, final boolean filterIsEnabled) {
            super(rows, columns);
            this.filterIsEnabled = filterIsEnabled;
        }

        @Override
        public boolean isFilterSet() {
            return filterIsEnabled;
        }
    }
}
