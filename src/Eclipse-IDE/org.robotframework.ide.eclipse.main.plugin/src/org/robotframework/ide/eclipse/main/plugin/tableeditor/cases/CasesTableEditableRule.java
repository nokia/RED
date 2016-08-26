/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.nattable.configs.SuiteModelEditableRule;

/**
 * @author wypych
 */
public class CasesTableEditableRule extends SuiteModelEditableRule {

    private CasesTableEditableRule(final boolean isEditable) {
        super(isEditable);
    }

    public static IEditableRule createEditableRule(final RobotSuiteFile fileModel) {
        return new CasesTableEditableRule(fileModel.isEditable());
    }

    @Override
    public boolean isEditable(final ILayerCell cell, final IConfigRegistry configRegistry) {
        return super.isEditable(cell, configRegistry) && !cell.getConfigLabels()
                .hasLabel(CasesElementsLabelAccumulator.CELL_NOT_EDITABLE_LABEL);
    }
}
