/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.validate.DefaultDataValidator;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.robotframework.red.nattable.configs.AddingElementConfiguration;


/**
 * @author Michal Anglart
 *
 */
public class VariablesEditConfiguration extends DefaultEditConfiguration {

    @Override
    public void configureRegistry(final IConfigRegistry configRegistry) {
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, createEditableRule());
        configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new TextCellEditor(true, true),
                DisplayMode.NORMAL, GridRegion.BODY);
        configRegistry.registerConfigAttribute(EditConfigAttributes.DATA_VALIDATOR, new DefaultDataValidator());
        configRegistry.registerConfigAttribute(EditConfigAttributes.OPEN_ADJACENT_EDITOR, Boolean.TRUE,
                DisplayMode.EDIT, GridRegion.BODY);
        configRegistry.registerConfigAttribute(EditConfigAttributes.ACTIVATE_EDITOR_ON_TRAVERSAL, Boolean.TRUE,
                DisplayMode.EDIT, GridRegion.BODY);
    }

    private IEditableRule createEditableRule() {
        return new IEditableRule() {

            @Override
            public boolean isEditable(final ILayerCell cell, final IConfigRegistry configRegistry) {
                return !cell.getConfigLabels().hasLabel(AddingElementConfiguration.ELEMENT_ADDER_ROW_CONFIG_LABEL);
            }

            @Override
            public boolean isEditable(final int columnIndex, final int rowIndex) {
                throw new IllegalStateException("Shouldn't be called");
            }
        };
    }
}
