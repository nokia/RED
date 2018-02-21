/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.nebula.widgets.nattable.layer.LabelStack;
import org.eclipse.nebula.widgets.nattable.layer.cell.IConfigLabelAccumulator;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.red.nattable.configs.CommentsLabelAccumulator;
import org.robotframework.red.nattable.configs.VariablesLabelAccumulator;

/**
 * @author Michal Anglart
 *
 */
public class VariableTypesAndColumnsLabelAccumulator implements IConfigLabelAccumulator {

    static String getNameColumnLabel(final VariableType type) {
        return "NAME_" + type.name();
    }

    static String getValueColumnLabel(final VariableType type) {
        return "VALUE_" + type.name();
    }

    static String getCommentColumnLabel(final VariableType type) {
        return "COMMENT_" + type.name();
    }

    private final VariablesDataProvider dataProvider;

    public VariableTypesAndColumnsLabelAccumulator(final VariablesDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @Override
    public void accumulateConfigLabels(final LabelStack configLabels, final int columnPosition, final int rowPosition) {
        final Object variable = dataProvider.getRowObject(rowPosition);
        if (variable instanceof RobotVariable) {
            final RobotVariable var = (RobotVariable) variable;
            final VariableType type = var.getType();
            if (columnPosition == 0) {
                configLabels.addLabel(getNameColumnLabel(type));
                configLabels.addLabel(VariablesLabelAccumulator.VARIABLE_CONFIG_LABEL);
            } else if (columnPosition == 1) {
                configLabels.addLabel(getValueColumnLabel(type));
            } else if (columnPosition == 2) {
                configLabels.addLabel(getCommentColumnLabel(type));
                configLabels.addLabel(CommentsLabelAccumulator.COMMENT_CONFIG_LABEL);
            }
        }
    }
}
