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
public class VariablesInElementsLabelAccumulatorTest {

    private LabelStack labels;
    private VariablesInElementsLabelAccumulator labelAccumulator;

    @Before
    public void cleanData() {
        labels = new LabelStack();
        labelAccumulator = new VariablesInElementsLabelAccumulator();
    }

    @Test
    public void labelIsNotAdded_forName() {
        labels.addLabel(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forNonName() {
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels())
                .containsExactly(VariablesInElementsLabelAccumulator.POSSIBLE_VARIABLES_IN_ELEMENTS_CONFIG_LABEL);
    }
}
