/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lwlodarc
 *
 */
public class SettingsItemsLabelAccumulatorTest {

    private LabelStack labels;
    private SettingsItemsLabelAccumulator labelAccumulator;

    @Before
    public void cleanData() {
        labels = new LabelStack();
        labelAccumulator = new SettingsItemsLabelAccumulator();
    }

    @Test
    public void labelIsNotAdded_forNonZeroColumn() {
        labelAccumulator.accumulateConfigLabels(labels, 1, 0);
        labelAccumulator.accumulateConfigLabels(labels, 1, 1);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forZeroColumn() {
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(SettingsItemsLabelAccumulator.SETTING_CONFIG_LABEL);
    }
}
