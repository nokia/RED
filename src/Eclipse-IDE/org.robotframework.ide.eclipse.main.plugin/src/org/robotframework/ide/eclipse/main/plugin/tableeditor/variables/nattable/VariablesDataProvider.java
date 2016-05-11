/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.table.variables.AVariable.VariableType;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.SetVariableNameCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.nattable.VariablesMatchesCollection.VariableFilter;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

/**
 * @author Michal Anglart
 *
 */
class VariablesDataProvider implements IDataProvider, IRowDataProvider<RobotVariable> {

    private final RobotEditorCommandsStack commandsStack;

    private RobotVariablesSection section;
    private SortedList<RobotVariable> variables;

    private VariableFilter filter;

    VariablesDataProvider(final RobotEditorCommandsStack commandsStack, final RobotVariablesSection section) {
        this.commandsStack = commandsStack;
        this.section = section;
        this.variables = createFrom(section);
    }

    private SortedList<RobotVariable> createFrom(final RobotVariablesSection section) {
        if (section != null) {
            final EventList<RobotVariable> vars = GlazedLists.eventList(section.getChildren());
            return new SortedList<>(vars, null);
        }
        return null;
    }

    public void setInput(final RobotVariablesSection section) {
        this.section = section;
        this.variables = createFrom(section);
    }

    public RobotVariablesSection getInput() {
        return section;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getDataValue(final int column, final int row) {
        if (section != null) {
            if (row == variables.size() - countInvisible()) {
                return column == 0 ? "...add new scalar" : "";
            }

            final RobotVariable variable = getRowObject(row);
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
            return variables.size() - countInvisible() + 1;
        }
        return 0;
    }

    private int countInvisible() {
        int numberOfInvisible = 0;
        for (final RobotVariable variable : variables) {
            if (!isPassingThroughFilter(variable)) {
                numberOfInvisible++;
            }
        }
        return numberOfInvisible;
    }

    @Override
    public void setDataValue(final int column, final int row, final Object value) {
        if (value instanceof RobotVariable) {
            return;
        }
        final RobotVariable var = getRowObject(row);
        if (column == 0) {
            commandsStack.execute(new SetVariableNameCommand(var, (String) value));
        }
    }

    @Override
    public RobotVariable getRowObject(final int rowIndex) {
        if (section != null && rowIndex < variables.size()) {
            RobotVariable rowObject = null;

            int count = 0;
            int realRowIndex = 0;
            while (count <= rowIndex) {
                rowObject = variables.get(realRowIndex);
                if (isPassingThroughFilter(rowObject)) {
                    count++;
                }
                realRowIndex++;
            }
            return rowObject;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final RobotVariable rowObject) {
        if (section != null) {
            final int realRowIndex = variables.indexOf(rowObject);
            int filteredIndex = realRowIndex;

            RobotVariable currentRowElement = null;
            for (int i = 0; i <= realRowIndex; i++) {
                currentRowElement = variables.get(i);
                if (!isPassingThroughFilter(currentRowElement)) {
                    filteredIndex--;
                }
            }
            return filteredIndex;
        }
        return -1;
    }

    private boolean isPassingThroughFilter(final RobotVariable rowObject) {
        return filter == null || filter.isMatching(rowObject);
    }

    void setMatches(final HeaderFilterMatchesCollection matches) {
        this.filter = matches == null ? null : new VariableFilter(matches);
    }
}
