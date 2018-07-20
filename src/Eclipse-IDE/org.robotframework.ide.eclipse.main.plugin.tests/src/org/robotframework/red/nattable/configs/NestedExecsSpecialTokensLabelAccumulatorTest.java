package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;


public class NestedExecsSpecialTokensLabelAccumulatorTest {

    @Test
    public void labelsAreOnlyAccumulated_forElseElseIfWordsInRunKeywordIf_inExecutableRow() {
        final RobotKeywordCall call = createModelWithSpecialKeywords().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(1);

        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).isEmpty();
        assertThat(labelsAt(call, 2)).isEmpty();
        assertThat(labelsAt(call, 3)).isEmpty();
        assertThat(labelsAt(call, 4)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(call, 5)).isEmpty();
        assertThat(labelsAt(call, 6)).isEmpty();
        assertThat(labelsAt(call, 7)).isEmpty();
        assertThat(labelsAt(call, 8)).isEmpty();
        assertThat(labelsAt(call, 9)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(call, 10)).isEmpty();
        assertThat(labelsAt(call, 11)).isEmpty();
    }

    @Test
    public void labelsAreOnlyAccumulated_forAndWordsInRunKeywords_inExecutableRow() {
        final RobotKeywordCall call = createModelWithSpecialKeywords().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(2);

        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).isEmpty();
        assertThat(labelsAt(call, 2)).isEmpty();
        assertThat(labelsAt(call, 3)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(call, 4)).isEmpty();
        assertThat(labelsAt(call, 5)).isEmpty();
        assertThat(labelsAt(call, 6)).isEmpty();
        assertThat(labelsAt(call, 7)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(call, 8)).isEmpty();
        assertThat(labelsAt(call, 9)).isEmpty();
        assertThat(labelsAt(call, 10)).isEmpty();
        assertThat(labelsAt(call, 11)).isEmpty();
    }

    @Test
    public void labelsAreOnlyAccumulated_forElseElseIfWordsInRunKeywordIf_inSetup() {
        final RobotKeywordCall call = createModelWithSpecialKeywords().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(0);

        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).isEmpty();
        assertThat(labelsAt(call, 2)).isEmpty();
        assertThat(labelsAt(call, 3)).isEmpty();
        assertThat(labelsAt(call, 4)).isEmpty();
        assertThat(labelsAt(call, 5)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(call, 6)).isEmpty();
        assertThat(labelsAt(call, 7)).isEmpty();
        assertThat(labelsAt(call, 8)).isEmpty();
        assertThat(labelsAt(call, 9)).isEmpty();
        assertThat(labelsAt(call, 10)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(call, 11)).isEmpty();
        assertThat(labelsAt(call, 12)).isEmpty();
    }

    @Test
    public void labelsAreOnlyAccumulated_forAndWordsInRunKeywords_inTeardown() {
        final RobotKeywordCall call = createModelWithSpecialKeywords().findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren()
                .get(3);

        assertThat(labelsAt(call, 0)).isEmpty();
        assertThat(labelsAt(call, 1)).isEmpty();
        assertThat(labelsAt(call, 2)).isEmpty();
        assertThat(labelsAt(call, 3)).isEmpty();
        assertThat(labelsAt(call, 4)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(call, 5)).isEmpty();
        assertThat(labelsAt(call, 6)).isEmpty();
        assertThat(labelsAt(call, 7)).isEmpty();
        assertThat(labelsAt(call, 8)).containsOnly(SpecialItemsLabelAccumulator.SPECIAL_ITEM_CONFIG_LABEL);
        assertThat(labelsAt(call, 9)).isEmpty();
        assertThat(labelsAt(call, 10)).isEmpty();
        assertThat(labelsAt(call, 11)).isEmpty();
        assertThat(labelsAt(call, 12)).isEmpty();
    }

    private static List<String> labelsAt(final RobotKeywordCall call, final int column) {
        @SuppressWarnings("unchecked")
        final IRowDataProvider<Object> dataProvider = mock(IRowDataProvider.class);
        when(dataProvider.getRowObject(0)).thenReturn(call);

        final LabelStack labels = new LabelStack();
        final NestedExecsSpecialTokensLabelAccumulator labelAccumulator = new NestedExecsSpecialTokensLabelAccumulator(
                dataProvider);
        labelAccumulator.accumulateConfigLabels(labels, column, 0);
        return labels.getLabels();
    }

    private static RobotSuiteFile createModelWithSpecialKeywords() {
        return new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case")
                .appendLine(
                        "  [Setup]  Run Keyword If  condition1  kw1  arg1  ELSE IF  condition2  kw2  arg2  arg3  ELSE  kw3  arg4")
                .appendLine(
                        "  Run Keyword If  condition1  kw1  arg1  ELSE IF  condition2  kw2  arg2  arg3  ELSE  kw3  arg4")
                .appendLine("  Run Keywords  kw 1  arg1  AND  kw2  arg2  arg3  AND  kw3  arg4")
                .appendLine("  [Teardown]  Run Keywords  kw 1  arg1  AND  kw2  arg2  arg3  AND  kw3  arg4")
                .build();
    }
}
