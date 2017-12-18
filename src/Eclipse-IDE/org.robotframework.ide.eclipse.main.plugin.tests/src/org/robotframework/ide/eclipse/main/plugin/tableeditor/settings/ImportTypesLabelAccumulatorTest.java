package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

public class ImportTypesLabelAccumulatorTest {

    @Test
    public void importTypesAreProperlyLabeledInFirstColumnOnly() {
        final ImportSettingsDataProvider dataProvider = new ImportSettingsDataProvider(new RobotEditorCommandsStack(),
                prepareSection());
        final ImportTypesLabelAccumulator accumulator = new ImportTypesLabelAccumulator(dataProvider);

        for (int i = 0; i < dataProvider.getColumnCount(); i++) {
            for (int j = 0; j < dataProvider.getRowCount(); j++) {
                if (i == 0 && j < 3) {
                    assertThat(labelStackAt(accumulator, i, j)).contains(ImportTypesLabelAccumulator.IMPORT_TYPE_LABEL);
                } else {
                    assertThat(labelStackAt(accumulator, i, j))
                            .doesNotContain(ImportTypesLabelAccumulator.IMPORT_TYPE_LABEL);
                }
            }
        }
    }

    private static List<String> labelStackAt(final ImportTypesLabelAccumulator accumulator, final int column,
            final int row) {
        final LabelStack configLabels = new LabelStack();
        accumulator.accumulateConfigLabels(configLabels, column, row);
        return configLabels.getLabels();
    }

    private RobotSettingsSection prepareSection() {
        return new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine("Library  lib  #comment")
                .appendLine("Variables  vars.py  #comment")
                .appendLine("Resource  res.robot  #comment")
                .build()
                .findSection(RobotSettingsSection.class)
                .get();
    }
}
