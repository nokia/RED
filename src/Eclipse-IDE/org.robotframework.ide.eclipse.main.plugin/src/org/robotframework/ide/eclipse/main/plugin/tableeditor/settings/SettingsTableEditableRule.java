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
import org.robotframework.red.nattable.configs.AddingElementStyleConfiguration;

class SettingsTableEditableRule implements IEditableRule {
    
    private final boolean isEditable;

    public static IEditableRule createEditableRule(final RobotSuiteFile fileModel) {
        return new SettingsTableEditableRule(fileModel.isEditable());
    }

    private SettingsTableEditableRule(final boolean isEditable) {
        this.isEditable = isEditable;
    }

    @Override
    public boolean isEditable(final ILayerCell cell, final IConfigRegistry configRegistry) {
        return isEditable && (cell.getColumnIndex() > 0
                || cell.getConfigLabels().hasLabel(AddingElementStyleConfiguration.ELEMENT_ADDER_CONFIG_LABEL));
    }

    @Override
    public boolean isEditable(final int columnIndex, final int rowIndex) {
        throw new IllegalStateException("Shouldn't be called");
    }
}