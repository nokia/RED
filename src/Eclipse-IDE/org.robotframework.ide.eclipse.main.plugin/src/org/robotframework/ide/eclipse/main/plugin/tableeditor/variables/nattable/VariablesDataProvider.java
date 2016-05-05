/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;


/**
 * @author Michal Anglart
 *
 */
public class VariablesDataProvider implements IDataProvider, IRowDataProvider<RobotVariable> {

    private final RobotEditorCommandsStack commandsStack;
    private RobotVariablesSection section;

    public VariablesDataProvider(final RobotEditorCommandsStack commandsStack, final RobotVariablesSection section) {
        this.commandsStack = commandsStack;
        this.section = section;
    }

    public void setInput(final RobotVariablesSection section) {
        this.section = section;
    }

    public RobotVariablesSection getInput() {
        return section;
    }

    public List<RobotVariable> getDomainObjects(final PositionCoordinate[] positions) {
        final List<RobotVariable> variables = new ArrayList<>();
        for (final PositionCoordinate coordinate : positions) {
            if (section != null) {
                final RobotVariable var = section.getChildren().get(coordinate.rowPosition);
                variables.add(var);
            }
        }
        return variables;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getDataValue(final int column, final int row) {
        if (section != null) {
            if (row == section.getChildren().size()) {
                return column == 0 ? "...add new scalar" : "";
            }

            final RobotVariable variable = section.getChildren().get(row);
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
        return "";
    }

    @Override
    public int getRowCount() {
        if (section != null) {
            return section.getChildren().size() + 1;
        }
        return 0;
    }

    @Override
    public void setDataValue(final int column, final int row, final Object value) {
        final RobotVariable var = getRowObject(row);
        if (column == 0) {
            commandsStack.execute(new SetVariableNameCommand(var, (String) value));
        }

    }

    @Override
    public RobotVariable getRowObject(final int rowIndex) {
        if (section != null && rowIndex < section.getChildren().size()) {
            return section.getChildren().get(rowIndex);
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final RobotVariable rowObject) {
        if (section != null) {
            return section.getChildren().indexOf(rowObject);
        }
        return -1;
    }
}
