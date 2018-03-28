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
                .build();
    }
}
