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
import org.rf.ide.core.testdata.model.table.RobotExecutableRow;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;

/**
 * @author lwlodarc
 *
 */
public class VariablesLabelAccumulatorTest {

    private IRowDataProvider<Object> dataProvider;
    private LabelStack labels;
    private VariablesLabelAccumulator labelAccumulator;
    private final RobotSuiteFile model = createModel();

    @SuppressWarnings("unchecked")
    @Before
    public void cleanData() {
        dataProvider = mock(IRowDataProvider.class);
        labels = new LabelStack();
        labelAccumulator = new VariablesLabelAccumulator(dataProvider);
    }

    @Test
    public void labelIsNotAdded_forEmptyLine() {
        final RobotKeywordCall call = new RobotKeywordCall(null, new RobotExecutableRow<>());
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forNonZerothColumn() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(2);
        when(dataProvider.getRowObject(2)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 2, 2);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSimpleKeywordCall_InTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(0);
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forScalarVariableAssignment_withEqualSign_InTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 1);
        assertThat(labels.getLabels()).containsExactly(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forScalarVariableAssignment_withuotEqualSign_InTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(2);
        when(dataProvider.getRowObject(2)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 2);
        assertThat(labels.getLabels()).containsExactly(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSimpleKeywordCall_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(2);
        when(dataProvider.getRowObject(2)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 2);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forDictionaryVariableAssignment_withEqualSign_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(0);
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forListVariableAssignment_withuotEqualSign_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 1);
        assertThat(labels.getLabels()).containsExactly(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forScalarVariableAssignment_withEqualSignAndSpaces_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(3);
        when(dataProvider.getRowObject(3)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 3);
        assertThat(labels.getLabels()).containsExactly(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
    }

    private static RobotSuiteFile createModel() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  log many  10  ${x}  @{y}  &{z}")
                .appendLine("  ${var1}=  Set Value  something important")
                .appendLine("  ${var2}  Set Value  something else")
                .appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  &{dict}=  Create Dictionary  key=value")
                .appendLine("  @{list}  Create List  qwop")
                .appendLine("  log many  10  &{a}  @{b}  ${c}")
                .appendLine("  ${ var with spaces } =")
                .build();
    }
}
