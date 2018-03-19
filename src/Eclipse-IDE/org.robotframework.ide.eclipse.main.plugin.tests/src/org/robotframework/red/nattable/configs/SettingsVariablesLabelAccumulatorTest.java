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
public class SettingsVariablesLabelAccumulatorTest {

    private IRowDataProvider<Object> dataProvider;
    private LabelStack labels;
    private SettingsVariablesLabelAccumulator labelAccumulator;

    @SuppressWarnings("unchecked")
    @Before
    public void cleanData() {
        dataProvider = mock(IRowDataProvider.class);
        labels = new LabelStack();
        labelAccumulator = new SettingsVariablesLabelAccumulator(dataProvider);
    }

    @Test
    public void labelIsNotAdded_forEmptyCell() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forNonVariableCell() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("Not a variable");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forScalarVariableCell() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("${scalar}");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forListVariableCell() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("@{list}");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forDictionaryVariableCell() {
        when(dataProvider.getDataValue(0, 0)).thenReturn("&{dict}");
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forIncorrectVariableCell() {
        final String[] cases = { "$var}", "{var}", "{$var}", "${var", "@&{var}" };
        for (final String c : cases) {
            when(dataProvider.getDataValue(0, 0)).thenReturn(c);
            labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        }
        assertThat(labels.getLabels()).isEmpty();
    }
}
