/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.red.nattable.AddingElementLabelAccumulator;

public class GeneralSettingsTableEditableRuleTest {

    private final IEditableRule editableRule = GeneralSettingsTableEditableRule
            .createEditableRule(new RobotSuiteFileCreator().build());

    @Test
    public void testIsEditable() {
        final ConfigRegistry configRegistry = mock(ConfigRegistry.class);
        assertThat(editableRule.isEditable(createCell(0, false), configRegistry)).isFalse();
        assertThat(editableRule.isEditable(createCell(0, true), configRegistry)).isTrue();
        assertThat(editableRule.isEditable(createCell(1, true), configRegistry)).isTrue();
        assertThat(editableRule.isEditable(createCell(1, false), configRegistry)).isTrue();
    }

    private ILayerCell createCell(final int columnIndex, final boolean hasElementAdderLabel) {
        final LabelStack labelStack = mock(LabelStack.class);
        when(labelStack.hasLabel(AddingElementLabelAccumulator.ELEMENT_ADDER_CONFIG_LABEL))
                .thenReturn(hasElementAdderLabel);
        final ILayerCell cell = mock(ILayerCell.class);
        when(cell.getColumnIndex()).thenReturn(columnIndex);
        when(cell.getConfigLabels()).thenReturn(labelStack);
        return cell;
    }
}
