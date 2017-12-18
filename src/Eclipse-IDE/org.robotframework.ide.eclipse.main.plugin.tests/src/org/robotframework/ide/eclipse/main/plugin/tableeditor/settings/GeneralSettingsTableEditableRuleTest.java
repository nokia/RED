package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.red.nattable.AddingElementLabelAccumulator;

public class GeneralSettingsTableEditableRuleTest {

    private final IEditableRule editableRule = GeneralSettingsTableEditableRule
            .createEditableRule(new RobotSuiteFileCreator().build());

    @Test
    public void testIsEditable() {
        final ConfigRegistry configRegistry = mock(ConfigRegistry.class);
        assertFalse(editableRule.isEditable(createCell(0, false), configRegistry));
        assertTrue(editableRule.isEditable(createCell(0, true), configRegistry));
        assertTrue(editableRule.isEditable(createCell(1, true), configRegistry));
        assertTrue(editableRule.isEditable(createCell(1, false), configRegistry));
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
