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
public class VariableInsideLabelAccumulatorTest {

    private LabelStack labels;
    private VariableInsideLabelAccumulator labelAccumulator;

    @Before
    public void cleanData() {
        labels = new LabelStack();
        labelAccumulator = new VariableInsideLabelAccumulator();
    }

    @Test
    public void labelIsNotAdded_forZerothColumn() {
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        labelAccumulator.accumulateConfigLabels(labels, 0, 1);
        labelAccumulator.accumulateConfigLabels(labels, 0, 2);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forNonZerothColumn() {
        labelAccumulator.accumulateConfigLabels(labels, 1, 0);
        assertThat(labels.getLabels())
                .containsExactly(VariableInsideLabelAccumulator.POSSIBLE_VARIABLE_INSIDE_CONFIG_LABEL);
    }
}
