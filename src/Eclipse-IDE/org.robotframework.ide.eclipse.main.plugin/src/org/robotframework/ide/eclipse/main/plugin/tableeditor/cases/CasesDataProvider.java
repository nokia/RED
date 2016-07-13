/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesMatchesCollection.CasesFilter;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;

public class CasesDataProvider implements IFilteringDataProvider, IRowDataProvider<Object> {

    private final AddingToken casesAddingToken = new AddingToken(null, CasesAdderState.CASE);

    private RobotCasesSection section;

    private SortedList<Object> casesSortedList;
    
    private TreeList<Object> cases;
    
    private final CasesColumnsPropertyAccessor propertyAccessor;
    
    private final CasesElementsTreeFormat casesTreeFormat;

    private CasesFilter filter;
    
    public CasesDataProvider(final RobotEditorCommandsStack commandsStack,
            final RobotCasesSection section) {
        this.section = section;
        this.propertyAccessor = new CasesColumnsPropertyAccessor(commandsStack, countColumnsNumber());
        this.casesTreeFormat = new CasesElementsTreeFormat();
        createFrom(section);
    }
    
    private void createFrom(final RobotCasesSection section) {
        if (cases == null) {
            casesSortedList = new SortedList<>(GlazedLists.<Object> eventListOf(), null);
            cases = new TreeList<>(casesSortedList, casesTreeFormat, TreeList.nodesStartExpanded());
        }
        if (section != null) {
            casesSortedList.clear();
            
            for (final RobotCase robotCase : section.getChildren()) {
                casesSortedList.add(robotCase);
                casesSortedList.addAll(robotCase.getChildren());
                casesSortedList.add(new AddingToken(robotCase, CasesAdderState.CALL));
            }
        }

    }
     
    public void setInput(final RobotCasesSection section) {
        this.section = section;
        createFrom(section);
        propertyAccessor.setColumnCount(countColumnsNumber());
    }

    public RobotCasesSection getInput() {
        return section;
    }
    
    SortedList<Object> getSortedList() {
        return casesSortedList;
    }
    
    TreeList<Object> getTreeList() {
        return cases;
    }

    public CasesElementsTreeFormat getCasesTreeFormat() {
        return casesTreeFormat;
    }

    @Override
    public Object getRowObject(final int rowIndex) {
        if (section != null && rowIndex < cases.size()) {
            Object rowObject = null;

            int count = 0;
            int realRowIndex = 0;
            while (count <= rowIndex && realRowIndex < cases.size()) {
                rowObject = cases.get(realRowIndex);
                if (isPassingThroughFilter(rowObject)) {
                    count++;
                } else {
                    rowObject = null;
                }
                realRowIndex++;
            }

            return (rowObject == null) ? casesAddingToken : rowObject;
        } else if (rowIndex == cases.size()) {
            return casesAddingToken;
        }
        return null;
        //
        // if (rowIndex < cases.size()) {
        // return cases.get(rowIndex);
        // } else if (rowIndex == cases.size()) {
        // return casesAddingToken;
        // }
        // return null;
    }
    
    @Override
    public int indexOfRowObject(final Object rowObject) {
        // if (section != null) {
        // final int realRowIndex = cases.indexOf(rowObject);
        // final int filteredIndex = realRowIndex;
        //
        // //TODO: handle filtering
        //
        // return filteredIndex;
        // } else if (rowObject == casesAddingToken) {
        // return cases.size();
        // }
        // return -1;

        if (section != null) {
            final int realRowIndex = cases.indexOf(rowObject);
            int filteredIndex = realRowIndex;

            Object currentRowElement = null;
            for (int i = 0; i <= realRowIndex; i++) {
                currentRowElement = cases.get(i);
                if (!isPassingThroughFilter(currentRowElement)) {
                    filteredIndex--;
                }
            }
            return filteredIndex;
        } else if (rowObject == casesAddingToken) {
            return cases.size();
        }
        return -1;
    }
    
    @Override
    public Object getDataValue(final int column, final int row) {
        if (section != null) {
            final Object element = getRowObject(row);
            if (element instanceof RobotElement) {
                return propertyAccessor.getDataValue(element, column);
            } else if (element instanceof AddingToken && column == 0) {
                return ((AddingToken) element).getLabel();
            }
        }
        return "";
    }
    
    @Override
    public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
        if (newValue instanceof RobotElement) {
            return;
        }
        final Object keyword = getRowObject(rowIndex);
        propertyAccessor.setDataValue(keyword, columnIndex, newValue);
    }
    
    @Override
    public int getRowCount() {
        if (section != null) {
            final int addingTokens = isFilterSet() ? 1 : 0;
            return cases.size() - countInvisible() + 1 - addingTokens;
        }
        return 0;
    }

    private int countInvisible() {
        int numberOfInvisible = 0;
        for (final Object object : cases) {
            if (!isPassingThroughFilter(object)) {
                numberOfInvisible++;
            }
        }
        return numberOfInvisible;
    }
    
    @Override
    public int getColumnCount() {
        return propertyAccessor.getColumnCount();
    }

    public CasesColumnsPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    boolean isPassingThroughFilter(final Object rowObject) {
        return filter == null || filter.isMatching(rowObject);
    }

    void setMatches(final HeaderFilterMatchesCollection matches) {
        this.filter = matches == null ? null : new CasesFilter(matches);
    }

    private int countColumnsNumber() {
        return calculateLongestArgumentsLength() + 2; // keyword name + args + comment
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        final List<?> elements = cases;
        if (elements != null) {
            for (final Object element : elements) {
                if (element instanceof RobotKeywordCall) {
                    max = Math.max(max, ((RobotKeywordCall) element).getArguments().size());
                }
            }
        }
        return max;
    }
}
