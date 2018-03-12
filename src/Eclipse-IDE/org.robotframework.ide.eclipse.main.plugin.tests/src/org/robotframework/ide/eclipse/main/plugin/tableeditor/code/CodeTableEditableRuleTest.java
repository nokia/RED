/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.TableConfigurationLabels;

public class CodeTableEditableRuleTest {

    @Test
    public void cellIsNotEditable_whenModelIsReadOnly() {
        final RobotSuiteFile readOnlyModel = new RobotSuiteFileCreator().buildReadOnly();
        final IEditableRule editableRule = CodeTableEditableRule.createEditableRule(readOnlyModel);
        
        final IConfigRegistry configRegistry = mock(ConfigRegistry.class);
        final ILayerCell cell = mock(ILayerCell.class);

        assertThat(editableRule.isEditable(cell, configRegistry)).isFalse();

        verifyZeroInteractions(configRegistry);
    }

    @Test
    public void cellIsEditable_whenModelIsEditableAndThereIsNoCellNotEditableLabel() {
        final RobotSuiteFile readOnlyModel = new RobotSuiteFileCreator().build();
        final IEditableRule editableRule = CodeTableEditableRule.createEditableRule(readOnlyModel);

        final LabelStack labels = new LabelStack("some label", "other label");
        final IConfigRegistry configRegistry = mock(ConfigRegistry.class);
        final ILayerCell cell = mock(ILayerCell.class);
        when(cell.getConfigLabels()).thenReturn(labels);

        assertThat(editableRule.isEditable(cell, configRegistry)).isTrue();

        verifyZeroInteractions(configRegistry);
    }

    @Test
    public void cellIsNotEditable_whenModelIsEditableButThereIsCellNotEditableLabel() {
        final RobotSuiteFile readOnlyModel = new RobotSuiteFileCreator().build();
        final IEditableRule editableRule = CodeTableEditableRule.createEditableRule(readOnlyModel);

        final LabelStack labels = new LabelStack("some label", TableConfigurationLabels.CELL_NOT_EDITABLE_LABEL,
                "other label");
        final IConfigRegistry configRegistry = mock(ConfigRegistry.class);
        final ILayerCell cell = mock(ILayerCell.class);
        when(cell.getConfigLabels()).thenReturn(labels);

        assertThat(editableRule.isEditable(cell, configRegistry)).isFalse();

        verifyZeroInteractions(configRegistry);
    }

}
