/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.junit.Before;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;

public class CasesElementsLabelAccumulatorTest {

    private static final int NUMBER_OF_COLUMNS = 6;

    private IRowDataProvider<Object> dataProvider;

    private CasesElementsLabelAccumulator accumulator;

    @SuppressWarnings("unchecked")
    @Before
    public void beforeTest() {
        dataProvider = mock(IRowDataProvider.class);
        accumulator = new CasesElementsLabelAccumulator(dataProvider);
        when(dataProvider.getColumnCount()).thenReturn(NUMBER_OF_COLUMNS);
    }

    @Test
    public void whenCaseIsNotTemplated_thereIsAProperLabelInCellWithItsName() {
        labelIsAccumulatedAt(createTestCase(), CasesElementsLabelAccumulator.CASE_CONFIG_LABEL, 0);
    }

    @Test
    public void whenCaseIsTemplated_thereIsAProperLabelInCellWithItsName() {
        labelIsAccumulatedAt(createTemplatedCase(), CasesElementsLabelAccumulator.CASE_WITH_TEMPLATE_CONFIG_LABEL, 0);
    }

    @Test
    public void caseShouldBeEditableOnlyInFirstColumn() {
        labelIsAccumulatedAt(createTestCase(), TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL, 1, 2, 3, 4, 5);
        labelIsAccumulatedAt(createTemplatedCase(), TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL, 1, 2, 3, 4,
                5);
    }

    @Test
    public void callNameShouldBeLabeledInFirstColumn() {
        for (final RobotKeywordCall call : createTestCase().getChildren()) {
            if (call.getClass() == RobotKeywordCall.class) {
                labelIsAccumulatedAt(call, CasesElementsLabelAccumulator.CASE_CALL_CONFIG_LABEL, 0);
            } else {
                thereIsNoSuchLabel(call, CasesElementsLabelAccumulator.CASE_CALL_CONFIG_LABEL);
            }
        }
    }

    @Test
    public void settingNameShouldBeLabeledInFirstColumn() {
        for (final RobotKeywordCall call : Iterables.concat(createTestCase().getChildren(),
                createTemplatedCase().getChildren())) {

            if (call instanceof RobotDefinitionSetting) {
                labelIsAccumulatedAt(call, CasesElementsLabelAccumulator.CASE_SETTING_CONFIG_LABEL, 0);
            }
        }
    }

    @Test
    public void documentationIsNotEditableForArgumentsColumnsOtherThatFirstOne() {
        for (final RobotKeywordCall call : createTestCase().getChildren()) {
            if (call instanceof RobotDefinitionSetting && ((RobotDefinitionSetting) call).isDocumentation()) {
                labelIsAccumulatedAt(call, TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL, 2, 3, 4, 5);
            } else {
                thereIsNoSuchLabel(call, TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL);
            }
        }
    }

    private void thereIsNoSuchLabel(final Object objectToLabel, final String labelToCheck) {
        labelIsAccumulatedAt(objectToLabel, labelToCheck);
    }

    private void labelIsAccumulatedAt(final Object objectToLabel, final String labelToCheck,
            final int... columnsWhereLabelShouldExist) {
        when(dataProvider.getRowObject(0)).thenReturn(objectToLabel);

        for (int column = 0; column < NUMBER_OF_COLUMNS; column++) {
            final LabelStack configLabels = new LabelStack();
            accumulator.accumulateConfigLabels(configLabels, column, 0);

            final List<String> accumulatedLabels = configLabels.getLabels();

            if (Ints.asList(columnsWhereLabelShouldExist).contains(column)) {
                assertThat(accumulatedLabels).contains(labelToCheck);
            } else {
                assertThat(accumulatedLabels).doesNotContain(labelToCheck);
            }
        }
    }

    private static RobotCase createTestCase() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Tags]  a  b")
                .appendLine("  Log  10")
                .appendLine("  [Setup]  Log  xxx")
                .appendLine("  [Teardown]  Log  yyy")
                .appendLine("  [Timeout]  10  x  y  z")
                .appendLine("  Log  10")
                .appendLine("  [Documentation]  abc    def")
                .appendLine("  [unknown]  abc    def")
                .appendLine("  Log  10")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0);
    }

    private static RobotCase createTemplatedCase() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Test Cases ***")
                .appendLine("case 1")
                .appendLine("  [Template]  a")
                .appendLine("  1    2    3")
                .appendLine("  5    6    7")
                .appendLine("  8    9    0")
                .build();
        final RobotCasesSection section = model.findSection(RobotCasesSection.class).get();
        return section.getChildren().get(0);
    }
}
