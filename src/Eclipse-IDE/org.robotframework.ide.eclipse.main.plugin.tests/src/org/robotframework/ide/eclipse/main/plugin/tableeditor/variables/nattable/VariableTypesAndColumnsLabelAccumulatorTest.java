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

public class VariableTypesAndColumnsLabelAccumulatorTest {

    @Test
    public void variableTypesAreProperlyLabeledOnlyInSecondColumn() {
        final VariablesDataProvider dataProvider = new VariablesDataProvider(new RobotEditorCommandsStack(),
                prepareSection());
        final VariableTypesAndColumnsLabelAccumulator accumulator = new VariableTypesAndColumnsLabelAccumulator(dataProvider);

        assertThat(labelStackAt(accumulator, 0, 0))
                .contains(VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.SCALAR));
        assertThat(labelStackAt(accumulator, 0, 1))
                .contains(VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.SCALAR_AS_LIST));
        assertThat(labelStackAt(accumulator, 0, 2))
                .contains(VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.LIST));
        assertThat(labelStackAt(accumulator, 0, 3))
                .contains(VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.DICTIONARY));
        assertThat(labelStackAt(accumulator, 0, 4))
                .contains(VariableTypesAndColumnsLabelAccumulator.getNameColumnLabel(VariableType.INVALID));
        assertThat(labelStackAt(accumulator, 0, 5)).isEmpty();

        assertThat(labelStackAt(accumulator, 1, 0))
                .contains(VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.SCALAR));
        assertThat(labelStackAt(accumulator, 1, 1))
                .contains(VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.SCALAR_AS_LIST));
        assertThat(labelStackAt(accumulator, 1, 2))
                .contains(VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.LIST));
        assertThat(labelStackAt(accumulator, 1, 3))
                .contains(VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.DICTIONARY));
        assertThat(labelStackAt(accumulator, 1, 4))
                .contains(VariableTypesAndColumnsLabelAccumulator.getValueColumnLabel(VariableType.INVALID));
        assertThat(labelStackAt(accumulator, 1, 5)).isEmpty();

        assertThat(labelStackAt(accumulator, 2, 0))
                .contains(VariableTypesAndColumnsLabelAccumulator.getCommentColumnLabel(VariableType.SCALAR));
        assertThat(labelStackAt(accumulator, 2, 1))
                .contains(VariableTypesAndColumnsLabelAccumulator.getCommentColumnLabel(VariableType.SCALAR_AS_LIST));
        assertThat(labelStackAt(accumulator, 2, 2))
                .contains(VariableTypesAndColumnsLabelAccumulator.getCommentColumnLabel(VariableType.LIST));
        assertThat(labelStackAt(accumulator, 2, 3))
                .contains(VariableTypesAndColumnsLabelAccumulator.getCommentColumnLabel(VariableType.DICTIONARY));
        assertThat(labelStackAt(accumulator, 2, 4))
                .contains(VariableTypesAndColumnsLabelAccumulator.getCommentColumnLabel(VariableType.INVALID));

        assertThat(labelStackAt(accumulator, 2, 5)).isEmpty();
    }

    private static List<String> labelStackAt(final VariableTypesAndColumnsLabelAccumulator accumulator, final int column,
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
