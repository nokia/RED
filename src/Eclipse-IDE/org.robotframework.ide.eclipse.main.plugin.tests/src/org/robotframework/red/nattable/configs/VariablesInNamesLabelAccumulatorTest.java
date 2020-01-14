/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rf.ide.core.environment.RobotVersion;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesElementsLabelAccumulator;

/**
 * @author lwlodarc
 *
 */
public class VariablesInNamesLabelAccumulatorTest {

    private LabelStack labels;
    private VariablesInNamesLabelAccumulator labelAccumulator;

    @BeforeEach
    public void cleanData() {
        labels = new LabelStack();
        labelAccumulator = new VariablesInNamesLabelAccumulator(() -> new RobotVersion(3, 1));
    }

    @Test
    public void labelIsAdded_forName() {
        labels.addLabel(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forNonName() {
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAddedForCaseName_whenVersionIsOlderThan32() {
        labels.addLabel(CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);

        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);
    }

    @Test
    public void labelIsAddedForCaseName_whenVersionIsNewerThan32() {
        labelAccumulator = new VariablesInNamesLabelAccumulator(() -> new RobotVersion(3, 2));
        labels.addLabel(CasesElementsLabelAccumulator.CASE_CONFIG_LABEL);

        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(CasesElementsLabelAccumulator.CASE_CONFIG_LABEL,
                VariablesInNamesLabelAccumulator.POSSIBLE_VARIABLES_IN_NAMES_CONFIG_LABEL);
    }
}
