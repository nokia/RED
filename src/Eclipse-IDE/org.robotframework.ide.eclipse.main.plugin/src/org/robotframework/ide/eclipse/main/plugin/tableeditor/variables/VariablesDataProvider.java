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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesMatchesCollection.VariableFilter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.VariablesTableAdderStatesConfiguration.VariablesAdderState;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;

/**
 * @author Michal Anglart
 */
class VariablesDataProvider implements IFilteringDataProvider, IRowDataProvider<Object> {

    private final AddingToken addingToken = new AddingToken(null, VariablesAdderState.SCALAR, VariablesAdderState.LIST,
            VariablesAdderState.DICTIONARY);

    private RobotVariablesSection section;

    private SortedList<RobotVariable> variables;

    private FilterList<RobotVariable> filteredVariables;

    private VariableFilter filter;

    private final VariableColumnsPropertyAccessor propertyAccessor;


    VariablesDataProvider(final RobotEditorCommandsStack commandsStack, final RobotVariablesSection section) {
        this.propertyAccessor = new VariableColumnsPropertyAccessor(commandsStack);
        setInput(section);
    }

    void setInput(final RobotVariablesSection section) {
        this.section = section;
        this.variables = createFrom(section);
    }

    private SortedList<RobotVariable> createFrom(final RobotVariablesSection section) {
        if (variables == null) {
            variables = new SortedList<>(GlazedLists.<RobotVariable> eventListOf(), null);
            filteredVariables = new FilterList<>(variables);
        }
        if (section != null) {
            filteredVariables.setMatcher(null);
            variables.clear();
            variables.addAll(section.getChildren());
        }
        return variables;
    }

    SortedList<RobotVariable> getSortedList() {
        return variables;
    }

    RobotVariablesSection getInput() {
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
    public int getRowCount() {
        if (section != null) {
            final int addingTokens = isFilterSet() ? 0 : 1;
            return filteredVariables.size() + addingTokens;
        }
        return 0;
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
        if (section != null && rowIndex < filteredVariables.size()) {
            return filteredVariables.get(rowIndex);
        } else if (rowIndex == filteredVariables.size()) {
            return addingToken;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
        if (rowObject == addingToken) {
            return filteredVariables.size();
        } else {
            return filteredVariables.indexOf(rowObject);
        }
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    void setFilter(final VariableFilter filter) {
        this.filter = filter;
        if (filter == null) {
            filteredVariables.setMatcher(null);
        } else {
            filteredVariables.setMatcher(new Matcher<RobotVariable>() {
                @Override
                public boolean matches(final RobotVariable item) {
                    return filter.isMatching(item);
                }
            });
        }
    }

    boolean isProvided(final RobotVariable robotVariable) {
        return filteredVariables.contains(robotVariable);
    }

    void switchAddderToNextState() {
        addingToken.switchToNext();
    }

    VariablesAdderState getAdderState() {
        return (VariablesAdderState) addingToken.getState();
    }
}
