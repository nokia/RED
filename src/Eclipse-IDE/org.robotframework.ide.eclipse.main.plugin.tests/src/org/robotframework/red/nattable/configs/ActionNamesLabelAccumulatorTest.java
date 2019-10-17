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
import org.junit.Test;
import org.rf.ide.core.testdata.model.table.RobotEmptyRow;
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

    @Test
    public void labelIsNotAdded_forEmptyLine() {
        final RobotKeywordCall call = new RobotKeywordCall(null, new RobotEmptyRow<>());
        assertThat(labelsAt(call, 0)).isEmpty();
    }

    @Test
    public void labelIsAdded_forSimpleKeywordCall_InTestCases() {
        final RobotKeywordCall call = createModel().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        assertThat(labelsAt(call, 0)).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forSettingKeywordCall_InTestCases() {
        final RobotKeywordCall call = createModel().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);
        assertThat(labelsAt(call, 1, 1)).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSetting_InTestCases() {
        final RobotKeywordCall call = createModel().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);
        assertThat(labelsAt(call, 0, 1)).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSettingArgument_InTestCases() {
        final RobotKeywordCall call = createModel().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(2);
        assertThat(labelsAt(call, 1, 2)).isEmpty();
    }

    @Test
    public void labelIsAdded_forSimpleKeywordCall_InKeywords() {
        final RobotKeywordCall call = createModel().findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(2);
        assertThat(labelsAt(call, 0, 2)).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forSettingKeywordCall_InKeywords() {
        final RobotKeywordCall call = createModel().findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        assertThat(labelsAt(call, 1)).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSetting_InKeywords() {
        final RobotKeywordCall call = createModel().findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        assertThat(labelsAt(call, 0)).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSettingArgument_InKeywords() {
        final RobotKeywordCall call = createModel().findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);
        assertThat(labelsAt(call, 1, 1)).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSlashThenCommentCase_InKeywords() {
        final RobotKeywordCall call = createModel().findSection(RobotKeywordsSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(3);
        assertThat(labelsAt(call, 0, 3)).isEmpty();
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

    @Test
    public void labelsAreProperlyAdded_forExecutableRowContainingForLoopContinuation() {
        final RobotKeywordCall call = createModelWithForLoop().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 2)).isEmpty();
    }

    @Test
    public void labelsAreProperlyAdded_forExecutableRowContainingForLoopWithEndContinuation() {
        final RobotKeywordCall call = createModelWithForLoopWithEnd().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 2)).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forExecutableRowContainingForLoopStart() {
        final RobotKeywordCall call = createModelWithForLoop().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).isEmpty();
        assertThat(labelsAt(call, 2)).isEmpty();
        assertThat(labelsAt(call, 3)).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forExecutableRowContainingForLoopEnd() {
        final RobotKeywordCall call = createModelWithForLoopWithEnd().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(2);
        assertThat(labelsAt(call, 0)).isEmpty();
    }

    @Test
    public void labelIsAdded_forTestSetupContainingTemplateKeywordCall() {
        final RobotKeywordCall call = createModelWithTemplate().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).containsExactly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forTestTemplateArguments() {
        final RobotKeywordCall call = createModelWithTemplate().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);
        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forTestSetupContainingTemplateDisable() {
        final RobotKeywordCall call = createModelWithDisabledTemplate().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);
        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).isEmpty();
    }

    @Test
    public void labelsAreProperlyAdded_forExecutableRowInCaseWithDisabledTemplate() {
        final RobotKeywordCall call = createModelWithDisabledTemplate().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(call, 0)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 1)).isEmpty();
    }

    @Test
    public void labelsAreProperlyAdded_forCallsWithArgumentsSameAsAction() {
        final RobotKeywordCall call = createModelWithArgumentsSameAsAction().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        assertThat(labelsAt(call, 0)).containsOnly(ActionNamesLabelAccumulator.ACTION_NAME_CONFIG_LABEL);
        assertThat(labelsAt(call, 1)).isEmpty();
        assertThat(labelsAt(call, 2)).isEmpty();
        assertThat(labelsAt(call, 3)).isEmpty();
        assertThat(labelsAt(call, 4)).isEmpty();
    }

    private static List<String> labelsAt(final RobotKeywordCall call, final int column) {
        return labelsAt(call, column, 0);
    }

    private static List<String> labelsAt(final RobotKeywordCall call, final int column, final int row) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(row)).thenReturn(call);

        final LabelStack labels = new LabelStack();
        final ActionNamesLabelAccumulator labelAccumulator = new ActionNamesLabelAccumulator(dataProvider);
        labelAccumulator.accumulateConfigLabels(labels, column, row);
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

    private static RobotSuiteFile createModelWithForLoop() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  FOR  ${index}  IN RANGE  3")
                .appendLine("    \\  Log  ${index}")
                .build();
    }

    private static RobotSuiteFile createModelWithForLoopWithEnd() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  FOR  ${index}  IN RANGE  3")
                .appendLine("    Log  ${index}")
                .appendLine("  END")
                .build();
    }

    private static RobotSuiteFile createModelWithTemplate() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Template]  Some Kw")
                .appendLine("    Log  1")
                .build();
    }

    private static RobotSuiteFile createModelWithDisabledTemplate() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Settings ***")
                .appendLine("Test Template  Some Kw")
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("  [Template]  NONE")
                .appendLine("    Log  1")
                .build();
    }

    private static RobotSuiteFile createModelWithArgumentsSameAsAction() {
        return new RobotSuiteFileCreator()
                .appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine("    Call  abc  def  Call  Call")
                .build();
    }
}
