/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetScalarValueCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableCommentCommand;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;

import com.google.common.collect.ImmutableBiMap;


/**
 * @author Michal Anglart
 *
 */
public class VariableColumnsPropertyAccessor implements IColumnPropertyAccessor<RobotVariable> {

    private final ImmutableBiMap<Integer, String> properties = 
            ImmutableBiMap.of(0, "name", 1, "value", 2, "comment");

    private final RobotEditorCommandsStack commandsStack;

    public VariableColumnsPropertyAccessor(final RobotEditorCommandsStack commandsStack) {
        this.commandsStack = commandsStack;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getDataValue(final RobotVariable variable, final int column) {
        if (column == 0) {
            return variable.getType() == VariableType.INVALID ? variable.getName()
                    : variable.getPrefix() + variable.getName() + variable.getSuffix();
        } else if (column == 1) {
            return variable.getValue();
        } else if (column == 2) {
            return variable.getComment();
        }
        throw new IllegalStateException("Unknown column with " + column + " index");
    }

    @Override
    public void setDataValue(final RobotVariable variable, final int column, final Object value) {
        if (column == 0) {
            commandsStack.execute(new SetVariableNameCommand(variable, (String) value));
        } else if (column == 1) {
            // other types are handled by more sophisticated cell editors
            if (variable.getType() == VariableType.SCALAR) {
                commandsStack.execute(new SetScalarValueCommand(variable, (String) value));
            }
        } else if (column == 2) {
            commandsStack.execute(new SetVariableCommentCommand(variable, (String) value));
        } else {
            throw new IllegalStateException("Unknown column with " + column + " index");
        }
    }

    @Override
    public String getColumnProperty(final int columnIndex) {
        return properties.get(columnIndex);
    }

    @Override
    public int getColumnIndex(final String propertyName) {
        return properties.inverse().get(propertyName);
    }
}
