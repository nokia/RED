/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.nattable.AddingElementLabelAccumulator;
import org.robotframework.red.nattable.configs.SuiteModelEditableRule;

class GeneralSettingsTableEditableRule extends SuiteModelEditableRule {
    
    public static IEditableRule createEditableRule(final RobotSuiteFile fileModel) {
        return new GeneralSettingsTableEditableRule(fileModel.isEditable());
    }

    private GeneralSettingsTableEditableRule(final boolean isEditable) {
        super(isEditable);
    }

    @Override
    public boolean isEditable(final ILayerCell cell, final IConfigRegistry configRegistry) {
        return super.isEditable(cell, configRegistry) && (cell.getColumnIndex() > 0
                || cell.getConfigLabels().hasLabel(AddingElementLabelAccumulator.ELEMENT_ADDER_CONFIG_LABEL));
    }
}