/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.nattable.AddingElementLabelAccumulator;

public class SuiteModelEditableRuleTest {

    @Test(expected = IllegalStateException.class)
    public void ruleCannotBeAskedForEditabilityUsingIndexes() {
        final RobotSuiteFile model = new RobotSuiteFileCreator().build();
        final IEditableRule rule = SuiteModelEditableRule.createEditableRule(model);
        rule.isEditable(1, 1);
    }

    @Test
    public void cellIsNotEditable_whenModelIsNotEditable() {
        final RobotSuiteFile model = spy(new RobotSuiteFileCreator().build());
        when(model.isEditable()).thenReturn(false);

        final ILayerCell cell1 = mock(ILayerCell.class);
        final ILayerCell cell2 = mock(ILayerCell.class);
        final ILayerCell cell3 = mock(ILayerCell.class);

        when(cell1.getConfigLabels()).thenReturn(new LabelStack("BODY"));
        when(cell2.getConfigLabels())
                .thenReturn(new LabelStack("BODY", AddingElementLabelAccumulator.ELEMENT_ADDER_ROW_CONFIG_LABEL));
        when(cell3.getConfigLabels())
                .thenReturn(new LabelStack("BODY", AddingElementLabelAccumulator.ELEMENT_ADDER_ROW_CONFIG_LABEL,
                        AddingElementLabelAccumulator.ELEMENT_ADDER_CONFIG_LABEL));

        final IEditableRule rule = SuiteModelEditableRule.createEditableRule(model);

        assertThat(rule.isEditable(cell1, mock(IConfigRegistry.class))).isFalse();
        assertThat(rule.isEditable(cell2, mock(IConfigRegistry.class))).isFalse();
        assertThat(rule.isEditable(cell3, mock(IConfigRegistry.class))).isFalse();
    }

    @Test
    public void cellIsEditable_whenModelIsEditable() {
        final RobotSuiteFile model = spy(new RobotSuiteFileCreator().build());
        when(model.isEditable()).thenReturn(true);

        final ILayerCell cell1 = mock(ILayerCell.class);
        final ILayerCell cell2 = mock(ILayerCell.class);
        final ILayerCell cell3 = mock(ILayerCell.class);

        when(cell1.getConfigLabels()).thenReturn(new LabelStack("BODY"));
        when(cell2.getConfigLabels())
                .thenReturn(new LabelStack("BODY", AddingElementLabelAccumulator.ELEMENT_ADDER_ROW_CONFIG_LABEL));
        when(cell3.getConfigLabels())
                .thenReturn(new LabelStack("BODY", AddingElementLabelAccumulator.ELEMENT_ADDER_ROW_CONFIG_LABEL,
                        AddingElementLabelAccumulator.ELEMENT_ADDER_CONFIG_LABEL));

        final IEditableRule rule = SuiteModelEditableRule.createEditableRule(model);

        assertThat(rule.isEditable(cell1, mock(IConfigRegistry.class))).isTrue();
        assertThat(rule.isEditable(cell2, mock(IConfigRegistry.class))).isTrue();
        assertThat(rule.isEditable(cell2, mock(IConfigRegistry.class))).isTrue();
    }
}
