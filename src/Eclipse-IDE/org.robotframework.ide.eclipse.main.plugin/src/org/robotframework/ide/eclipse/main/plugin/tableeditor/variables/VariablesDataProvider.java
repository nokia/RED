/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariablesSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesMatchesCollection.VariableFilter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesTableAdderStatesConfiguration.VariablesAdderState;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

/**
 * @author Michal Anglart
 */
public class VariablesDataProvider implements IFilteringDataProvider, IRowDataProvider<Object> {

    private final AddingToken addingToken = new AddingToken(VariablesAdderState.SCALAR, VariablesAdderState.LIST,
            VariablesAdderState.DICTIONARY);

    private RobotVariablesSection section;

    private SortedList<RobotVariable> variables;

    private VariableFilter filter;

    private final VariableColumnsPropertyAccessor propertyAccessor;

    VariablesDataProvider(final RobotEditorCommandsStack commandsStack, final RobotVariablesSection section) {
        this.section = section;
        this.variables = createFrom(section);
        this.propertyAccessor = new VariableColumnsPropertyAccessor(commandsStack);
    }

    private SortedList<RobotVariable> createFrom(final RobotVariablesSection section) {
        if (variables == null) {
            variables = new SortedList<>(GlazedLists.<RobotVariable> eventListOf(), null);
        }
        if (section != null) {
            variables.clear();
            variables.addAll(section.getChildren());
        }
        return variables;
    }

    public void setInput(final RobotVariablesSection section) {
        this.section = section;
        this.variables = createFrom(section);
    }

    SortedList<RobotVariable> getSortedList() {
        return variables;
    }

    public RobotVariablesSection getInput() {
        return section;
    }

    VariableColumnsPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    @Override
    public int getColumnCount() {
        return propertyAccessor.getColumnCount();
    }

    @Override
    public Object getDataValue(final int column, final int row) {
        if (section != null) {
            final Object element = getRowObject(row);
            if (element instanceof RobotVariable) {
                return propertyAccessor.getDataValue((RobotVariable) element, column);
            } else if (element instanceof AddingToken && column == 0 && !isFilterSet()) {
                return ((AddingToken) element).getLabel();
            }
        }
        return "";
    }

    @Override
    public int getRowCount() {
        if (section != null) {
            final int addingTokens = isFilterSet() ? 1 : 0;
            return variables.size() - countInvisible() + 1 - addingTokens;
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
        final Object variable = getRowObject(row);
        if (variable instanceof RobotVariable) {
            propertyAccessor.setDataValue((RobotVariable) variable, column, value);
        }
    }

    @Override
    public Object getRowObject(final int rowIndex) {
        if (section != null && rowIndex < variables.size()) {
            RobotVariable rowObject = null;

            int count = 0;
            int realRowIndex = 0;
            while (count <= rowIndex && realRowIndex < variables.size()) {
                rowObject = variables.get(realRowIndex);
                if (isPassingThroughFilter(rowObject)) {
                    count++;
                } else {
                    rowObject = null;
                }
                realRowIndex++;
            }

            return (rowObject == null) ? addingToken : rowObject;
        } else if (rowIndex == variables.size()) {
            return addingToken;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
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
        } else if (rowObject == addingToken) {
            return variables.size();
        }
        return -1;
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    boolean isPassingThroughFilter(final RobotVariable rowObject) {
        return filter == null || filter.isMatching(rowObject);
    }

    void setMatches(final HeaderFilterMatchesCollection matches) {
        this.filter = matches == null ? null : new VariableFilter(matches);
    }

    void switchAddderToNextState() {
        addingToken.switchToNext();
    }

    public VariablesAdderState getAdderState() {
        return (VariablesAdderState) addingToken.getState();
    }
}
