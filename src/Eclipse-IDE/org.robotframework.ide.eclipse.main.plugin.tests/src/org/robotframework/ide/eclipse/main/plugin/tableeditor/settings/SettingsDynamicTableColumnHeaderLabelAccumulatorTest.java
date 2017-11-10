package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class SettingsDynamicTableColumnHeaderLabelAccumulatorTest {

    @Test
    public void variableTypesAreProperlyLabeledOnlyInSecondColumn() {
        final ImportSettingsDataProvider dataProvider = new ImportSettingsDataProvider(new RobotEditorCommandsStack(),
                prepareSection());
        final SettingsDynamicTableColumnHeaderLabelAccumulator accumulator = new SettingsDynamicTableColumnHeaderLabelAccumulator(
                dataProvider);

        final int columnCount = dataProvider.getColumnCount();

        for (int i = 0; i < columnCount - 2; i++) {
            assertThat(labelStackAt(accumulator, i, 0)).contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + i);
            assertThat(labelStackAt(accumulator, i, 1)).contains(ColumnLabelAccumulator.COLUMN_LABEL_PREFIX + i);
        }
        assertThat(labelStackAt(accumulator, columnCount - 1, 0))
                .contains(SettingsDynamicTableColumnHeaderLabelAccumulator.SETTING_COMMENT_LABEL);
        assertThat(labelStackAt(accumulator, columnCount - 1, 1))
                .contains(SettingsDynamicTableColumnHeaderLabelAccumulator.SETTING_COMMENT_LABEL);
    }

    private static List<String> labelStackAt(final SettingsDynamicTableColumnHeaderLabelAccumulator accumulator,
            final int column, final int row) {
        final LabelStack configLabels = new LabelStack();
        accumulator.accumulateConfigLabels(configLabels, column, row);
        return configLabels.getLabels();
    }

    private RobotSettingsSection prepareSection() {
        return new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Library  lib  #comment")
                .appendLine("Variables  vars.py  1  2  3  5  6  7  8  9  #comment")
                .build()
                .findSection(RobotSettingsSection.class)
                .get();
    }
}
