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
public class CommentsLabelAccumulatorTest {

    private IRowDataProvider<Object> dataProvider;
    private LabelStack labels;
    private CommentsLabelAccumulator labelAccumulator;
    private final RobotSuiteFile model = createModel();

    @SuppressWarnings("unchecked")
    @Before
    public void cleanData() {
        dataProvider = mock(IRowDataProvider.class);
        labels = new LabelStack();
        labelAccumulator = new CommentsLabelAccumulator(dataProvider);
    }

    @Test
    public void labelIsNotAdded_forEmptyLine() {
        final RobotKeywordCall call = new RobotKeywordCall(null, new RobotExecutableRow<>());
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forWholeLineComment_inTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(0);
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).containsExactly(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSimpleKeywordCall_inTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 1);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forPartLineCommentStartingToken_inTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 2, 1);
        assertThat(labels.getLabels()).containsExactly(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forPartLineCommentContinueToken_inTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 3, 1);
        assertThat(labels.getLabels()).containsExactly(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSettingKeywordCall_inTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(2);
        when(dataProvider.getRowObject(2)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 1, 2);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSetting_inTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(2);
        when(dataProvider.getRowObject(2)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 2);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSettingArgument_inTestCases() {
        final RobotKeywordCall call = model.findSection(RobotCasesSection.class).get().getChildren()
                .get(0).getChildren().get(3);
        when(dataProvider.getRowObject(3)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 1, 3);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSimpleKeywordCall_inKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(2);
        when(dataProvider.getRowObject(2)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 2);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsAdded_forWholeLineCommentStartingToken_inKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(3);
        when(dataProvider.getRowObject(3)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 3);
        assertThat(labels.getLabels()).containsExactly(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forWholeLineCommentContinueToken_inKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(3);
        when(dataProvider.getRowObject(3)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 1, 3);
        assertThat(labels.getLabels()).containsExactly(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
    }

    @Test
    public void labelIsAdded_forPartLineComment_inKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(1);
        when(dataProvider.getRowObject(1)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 3, 1);
        assertThat(labels.getLabels()).containsExactly(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
    }

    @Test
    public void labelIsNotAdded_forSettingKeywordCall_inKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(0);
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 1, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSetting_inKeywords() {
        final RobotKeywordCall call = model.findSection(RobotKeywordsSection.class).get().getChildren()
                .get(0).getChildren().get(0);
        when(dataProvider.getRowObject(0)).thenReturn(call);
        labelAccumulator.accumulateConfigLabels(labels, 0, 0);
        assertThat(labels.getLabels()).isEmpty();
    }

    @Test
    public void labelIsNotAdded_forSettingArgument_inKeywords() {
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
                .appendLine("  # whole line comment")
                .appendLine("  log  10  # part line  comment")
                .appendLine("  [Teardown]  t enter")
                .appendLine("  [Tags]  t1  t2")
                .appendLine("*** Keywords ***")
                .appendLine("kw")
                .appendLine("  [Teardown]   Log  kw exit")
                .appendLine("  [Tags]   t1  t2  # cmt")
                .appendLine("  Log  keyword")
                .appendLine("  # comment  cmt continue")
                .build();
    }
}
