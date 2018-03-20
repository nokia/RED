/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lwlodarc
 *
 */
public class WithNameLabelAccumulatorTest {

    private IRowDataProvider<Object> dataProvider;
    private LabelStack labels;
    private WithNameLabelAccumulator labelAccumulator;

    @SuppressWarnings("unchecked")
    @Before
    public void cleanData() {
        dataProvider = mock(IRowDataProvider.class);
        labels = new LabelStack();
        labelAccumulator = new WithNameLabelAccumulator(dataProvider);
    }

    @Test
    public void labelIsNotAdded_forEmptyCell() {
        when(dataProvider.getDataValue(2, 0)).thenReturn("");
        labelAccumulator.accumulateConfigLabels(labels, 2, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forFirstTwoColumns() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("WITH NAME");
        when(dataProvider.getDataValue(1, 0)).thenReturn("WITH NAME");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        labelAccumulator.accumulateConfigLabels(labels, 1, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forWithNameCell_inRightColumn() {
        when(dataProvider.getDataValue(2, 0)).thenReturn("WITH NAME");
        labelAccumulator.accumulateConfigLabels(labels, 2, 0);
        assertThat(labels.getLabels()).containsExactly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forWithNameCell_withTabSeparator() {
        when(dataProvider.getDataValue(2, 0)).thenReturn("WITH\tNAME");
        labelAccumulator.accumulateConfigLabels(labels, 2, 0);
        assertThat(labels.getLabels()).containsExactly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forWithNameCell_pOkEmOnCase() {
        when(dataProvider.getDataValue(2, 0)).thenReturn("WiTh nAmE");
        labelAccumulator.accumulateConfigLabels(labels, 2, 0);
        assertThat(labels.getLabels()).containsExactly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forWithNameCell_lowerCase() {
        when(dataProvider.getDataValue(2, 0)).thenReturn("with name");
        labelAccumulator.accumulateConfigLabels(labels, 2, 0);
        assertThat(labels.getLabels()).containsExactly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
    }
}
