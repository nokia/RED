/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;


/**
 * @author Michal Anglart
 *
 */
public class VariableValuesTypeLabelAccumulator implements IConfigLabelAccumulator {

    private final VariablesDataProvider dataProvider;
    
    public VariableValuesTypeLabelAccumulator(final VariablesDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        if (columnPosition == 1) {
            final RobotVariable variable = dataProvider.getRowObject(rowPosition);
            if (variable != null) {
                configLabels.addLabel(variable.getType().name());
            }
        }
    }
}
