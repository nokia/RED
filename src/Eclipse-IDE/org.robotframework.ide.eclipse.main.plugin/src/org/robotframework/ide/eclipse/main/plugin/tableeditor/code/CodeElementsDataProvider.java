/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCodeHoldingElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotFileInternalElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken.TokenState;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;

public abstract class CodeElementsDataProvider<T extends RobotFileInternalElement>
        implements IFilteringDataProvider, IRowDataProvider<Object> {

    private final AddingToken addingToken;
    private final TokenState secondaryState;

    private T section;

    private SortedList<Object> sortedElements;
    private FilterList<Object> filteredElements;
    private TreeList<Object> elements;

    private final CodeElementsColumnsPropertyAccessor propertyAccessor;

    private final CodeElementsTreeFormat treeFormat;

    private CodeElementsFilter filter;

    public CodeElementsDataProvider(final T section, final CodeElementsColumnsPropertyAccessor propertyAccessor,
            final TokenState primaryState, final TokenState secondaryState) {
        this.addingToken = new AddingToken(null, primaryState);
        this.secondaryState = secondaryState;
        this.section = section;
        this.treeFormat = new CodeElementsTreeFormat();
        this.propertyAccessor = propertyAccessor;

        createLists(section);
        this.propertyAccessor.setColumnCount(countColumnsNumber());
    }

    public void setInput(final T section) {
        this.section = section;
        createLists(section);
        propertyAccessor.setColumnCount(countColumnsNumber());
    }

    private void createLists(final T section) {
        if (elements == null) {
            sortedElements = new SortedList<>(GlazedLists.eventListOf(), null);
            filteredElements = new FilterList<>(sortedElements);
            elements = new TreeList<>(filteredElements, treeFormat, TreeList.nodesStartExpanded());
        }
        if (section != null) {
            sortedElements.clear();

            for (final RobotElement elem : section.getChildren()) {
                final RobotCodeHoldingElement<?> holder = (RobotCodeHoldingElement<?>) elem;
                sortedElements.add(holder);
                sortedElements.addAll(filteredCalls(holder));
                sortedElements.add(new AddingToken(holder, secondaryState));
            }
        }
    }

    private List<RobotKeywordCall> filteredCalls(final RobotCodeHoldingElement<?> holder) {
        final List<RobotKeywordCall> allCalls = holder.getChildren();
        final List<RobotKeywordCall> filteredCalls = new ArrayList<>();
        for (final RobotKeywordCall call : allCalls) {
            if (call.isLocalSetting()) {
                if (shouldAddSetting(call)) {
                    filteredCalls.add(call);
                }
            } else {
                filteredCalls.add(call);
            }
        }
        return filteredCalls;
    }

    protected abstract boolean shouldAddSetting(final RobotKeywordCall setting);

    private int countColumnsNumber() {
        // add 1 for name column
        int max = 1 + RedPlugin.getDefault().getPreferences().getMinimalNumberOfArgumentColumns();
        if (elements != null) {
            for (final Object element : elements) {
                max = Math.max(max, numberOfColumns(element));
            }
        }
        return max;
    }

    protected abstract int numberOfColumns(final Object element);

    public SortedList<Object> getSortedList() {
        return sortedElements;
    }

    public TreeList<Object> getTreeList() {
        return elements;
    }

    public T getInput() {
        return section;
    }

    public CodeElementsTreeFormat getTreeFormat() {
        return treeFormat;
    }

    public IColumnPropertyAccessor<Object> getPropertyAccessor() {
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
            return elements.size() + addingTokens;
        }
        return 0;
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
        final Object element = getRowObject(rowIndex);
        if (element instanceof RobotElement) {
            propertyAccessor.setDataValue(element, columnIndex, newValue);
        }
    }

    @Override
    public Object getRowObject(final int rowIndex) {
        if (rowIndex < elements.size()) {
            return elements.get(rowIndex);
        } else if (rowIndex == elements.size()) {
            return addingToken;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
        if (section != null) {
            return elements.indexOf(rowObject);
        } else if (rowObject == addingToken) {
            return elements.size();
        }
        return -1;
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    public void setFilter(final CodeElementsFilter filter) {
        this.filter = filter;
        this.filteredElements.setMatcher(filter == null ? null : filter::isMatching);
    }

    public boolean isProvided(final RobotElement element) {
        return elements.contains(element);
    }
}
