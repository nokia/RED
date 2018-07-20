/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

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
public class ActionNamesLabelAccumulatorTest {

    private IRowDataProvider<Object> dataProvider;
    private LabelStack labels;
    private ActionNamesLabelAccumulator labelAccumulator;
    private final RobotSuiteFile model = createModel();

    @SuppressWarnings("unchecked")
    @Before
    public void cleanData() {
        dataProvider = mock(IRowDataProvider.class);
        labels = new LabelStack();
        labelAccumulator = new ActionNamesLabelAccumulator(dataProvider);
    }

    @Test
    public void labelIsNotAdded_forEmptyLine() {
        final RobotKeywordCall call = new RobotKeywordCall(null, new RobotExecutableRow<>());
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forSimpleKeywordCall_InTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(0);
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forSettingKeywordCall_InTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 1, 1);
        assertThat(labels.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSetting_InTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 1);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSettingArgument_InTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(2);
        when(dataProvider.getRowObject(2)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 1, 2);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forSimpleKeywordCall_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(2);
        when(dataProvider.getRowObject(2)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 2);
        assertThat(labels.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forSettingKeywordCall_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(0);
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 1, 0);
        assertThat(labels.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSetting_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(0);
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSettingArgument_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 1, 1);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forSlashThenCommentCase_InKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(3);
        when(dataProvider.getRowObject(3)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 3);
        assertThat(labels.getLabels()).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelsAreProperlyAdded_forTestSetupContainingNestedKeywords() {
        final RobotKeywordCall call = createModelWithSpecialKeywords().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 2)).isEmpty();
        assertThat(labelsAt(call, 3)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 4)).isEmpty();
        assertThat(labelsAt(call, 5)).isEmpty();
        assertThat(labelsAt(call, 6)).isEmpty();
        assertThat(labelsAt(call, 7)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 8)).isEmpty();
        assertThat(labelsAt(call, 9)).isEmpty();
        assertThat(labelsAt(call, 10)).isEmpty();
        assertThat(labelsAt(call, 11)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 12)).isEmpty();
    }

    @Test
    public void labelsAreProperlyAdded_forExecutableRowContainingNestedKeywords() {
        final RobotKeywordCall call = createModelWithSpecialKeywords().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(call, 0)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 1)).isEmpty();
        assertThat(labelsAt(call, 2)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 3)).isEmpty();
        assertThat(labelsAt(call, 4)).isEmpty();
        assertThat(labelsAt(call, 5)).isEmpty();
        assertThat(labelsAt(call, 6)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 7)).isEmpty();
        assertThat(labelsAt(call, 8)).isEmpty();
        assertThat(labelsAt(call, 9)).isEmpty();
        assertThat(labelsAt(call, 10)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 11)).isEmpty();
    }

    private static List<String> labelsAt(final RobotKeywordCall call, final int column) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(call);

        final LabelStack labels = new LabelStack();
        final ActionNamesLabelAccumulator labelAccumulator = new ActionNamesLabelAccumulator(dataProvider);
        labelAccumulator.accumulateConfigLabels(labels, column, 0);
        return labels.getLabels();
    }

    private static RobotSuiteFile createModel() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  log  10")
                .appendLine("  [Teardown]  t enter")
                .appendLine("  [Tags]  t1  t2")
                .appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  [Teardown]   Log  kw exit")
                .appendLine("  [Tags]   t1  t2")
                .appendLine("  Log  keyword")
                .appendLine("  \\  #cmt")
                .build();
    }

    private static RobotSuiteFile createModelWithSpecialKeywords() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine(
                        "  [Setup]  Run Keyword If  condition1  kw1  arg1  ELSE IF  condition2  kw2  arg2  arg3  ELSE  kw3  arg4")
                .appendLine(
                        "  Run Keyword If  condition1  kw1  arg1  ELSE IF  condition2  kw2  arg2  arg3  ELSE  kw3  arg4")
                .build();
    }
}
