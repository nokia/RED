/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class VariableValuesTypeLabelAccumulatorTest {

    @Test
    public void variableTypesAreProperlyLabeledOnlyInSecondColumn() {
        final VariablesDataProvider dataProvider = new VariablesDataProvider(new RobotEditorCommandsStack(),
                prepareSection());
        final VariableValuesTypeLabelAccumulator accumulator = new VariableValuesTypeLabelAccumulator(dataProvider);

        assertThat(labelStackAt(accumulator, 0, 0)).isEmpty();
        assertThat(labelStackAt(accumulator, 0, 1)).isEmpty();
        assertThat(labelStackAt(accumulator, 0, 2)).isEmpty();
        assertThat(labelStackAt(accumulator, 0, 3)).isEmpty();
        assertThat(labelStackAt(accumulator, 0, 4)).isEmpty();
        assertThat(labelStackAt(accumulator, 0, 5)).isEmpty();

        assertThat(labelStackAt(accumulator, 1, 0)).contains(VariableType.SCALAR.name());
        assertThat(labelStackAt(accumulator, 1, 1)).contains(VariableType.SCALAR_AS_LIST.name());
        assertThat(labelStackAt(accumulator, 1, 2)).contains(VariableType.LIST.name());
        assertThat(labelStackAt(accumulator, 1, 3)).contains(VariableType.DICTIONARY.name());
        assertThat(labelStackAt(accumulator, 1, 4)).contains(VariableType.INVALID.name());
        assertThat(labelStackAt(accumulator, 1, 5)).isEmpty();

        assertThat(labelStackAt(accumulator, 2, 0)).isEmpty();
        assertThat(labelStackAt(accumulator, 2, 1)).isEmpty();
        assertThat(labelStackAt(accumulator, 2, 2)).isEmpty();
        assertThat(labelStackAt(accumulator, 2, 3)).isEmpty();
        assertThat(labelStackAt(accumulator, 2, 4)).isEmpty();
        assertThat(labelStackAt(accumulator, 2, 5)).isEmpty();
    }

    private static List<String> labelStackAt(final VariableValuesTypeLabelAccumulator accumulator, final int column,
            final int row) {
        final LabelStack configLabels = new LabelStack();
        accumulator.accumulateConfigLabels(configLabels, column, row);
        return configLabels.getLabels();
    }

    private RobotVariablesSection prepareSection() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Variables ***")
                .appendLine("${scalar}  1")
                .appendLine("${scalar_as_list}  1  2  3")
                .appendLine("@{list}  a  b  c")
                .appendLine("&{dict}  k1=a  k2=b")
                .appendLine("{invalid}  1  2")
                .build()
                .findSection(RobotVariablesSection.class)
                .get();
    }

}
