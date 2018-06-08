/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.CasesMatchesCollection.CasesFilter;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsTreeFormat;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.matchers.Matcher;

public class CasesDataProvider implements IFilteringDataProvider, IRowDataProvider<Object> {

    private final AddingToken casesAddingToken = new AddingToken(null, CasesAdderState.CASE);

    private RobotCasesSection section;

    private SortedList<Object> casesSortedList;

    private FilterList<Object> filterList;

    private TreeList<Object> cases;

    private final CasesColumnsPropertyAccessor propertyAccessor;

    private final CodeElementsTreeFormat casesTreeFormat;

    private CasesFilter filter;

    CasesDataProvider(final RobotEditorCommandsStack commandsStack, final RobotCasesSection section) {
        this.section = section;
        this.casesTreeFormat = new CodeElementsTreeFormat();
        createLists(section);
        this.propertyAccessor = new CasesColumnsPropertyAccessor(commandsStack, countColumnsNumber());
    }

    void setInput(final RobotCasesSection section) {
        this.section = section;
        createLists(section);
        propertyAccessor.setColumnCount(countColumnsNumber());
    }

    private int countColumnsNumber() {
        // add 1 for name column
        int max = 1 + RedPlugin.getDefault().getPreferences().getMinimalNumberOfArgumentColumns();
        if (cases != null) {
            for (final Object element : cases) {
                if (element instanceof RobotKeywordCall) {
                    final RobotKeywordCall keyword = (RobotKeywordCall) element;
                    if (keyword.getLinkedElement().getModelType() != ModelType.TEST_CASE_DOCUMENTATION) {
                        // add 1 for empty cell
                        max = Math.max(max, ExecutablesRowHolderCommentService.execRowView(keyword).size() + 1);
                    }
                }
            }
        }
        return max;
    }

    private void createLists(final RobotCasesSection section) {
        if (cases == null) {
            casesSortedList = new SortedList<>(GlazedLists.<Object>eventListOf(), null);
            filterList = new FilterList<>(casesSortedList);
            cases = new TreeList<>(filterList, casesTreeFormat, TreeList.nodesStartExpanded());
        }
        if (section != null) {
            casesSortedList.clear();

            for (final RobotCase robotCase : section.getChildren()) {
                casesSortedList.add(robotCase);
                casesSortedList.addAll(filteredCalls(robotCase));
                casesSortedList.add(new AddingToken(robotCase, CasesAdderState.CALL));
            }
        }

    }

    private List<RobotKeywordCall> filteredCalls(final RobotCase robotCase) {
        final List<RobotKeywordCall> allCalls = robotCase.getChildren();
        final List<RobotKeywordCall> filteredCalls = new ArrayList<>();
        for (final RobotKeywordCall call : allCalls) {
            if (call instanceof RobotDefinitionSetting) {
                final RobotDefinitionSetting setting = (RobotDefinitionSetting) call;
                @SuppressWarnings("unchecked")
                final AModelElement<TestCase> linkedSetting = (AModelElement<TestCase>) setting.getLinkedElement();
                final TestCase testCase = robotCase.getLinkedElement();
                if (!testCase.isDuplicatedSetting(linkedSetting)) {
                    filteredCalls.add(call);
                }
            } else {
                filteredCalls.add(call);
            }
        }
        return filteredCalls;
    }

    SortedList<Object> getSortedList() {
        return casesSortedList;
    }

    TreeList<Object> getTreeList() {
        return cases;
    }

    RobotCasesSection getInput() {
        return section;
    }

    CodeElementsTreeFormat getTreeFormat() {
        return casesTreeFormat;
    }

    CasesColumnsPropertyAccessor getPropertyAccessor() {
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
            return cases.size() + addingTokens;
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
        if (rowIndex < cases.size()) {
            return cases.get(rowIndex);
        } else if (rowIndex == cases.size()) {
            return casesAddingToken;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
        if (section != null) {
            return cases.indexOf(rowObject);
        } else if (rowObject == casesAddingToken) {
            return cases.size();
        }
        return -1;
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    void setFilter(final CasesFilter filter) {
        this.filter = filter;

        if (filter == null) {
            filterList.setMatcher(null);
        } else {
            filterList.setMatcher(new Matcher<Object>() {

                @Override
                public boolean matches(final Object item) {
                    return filter.isMatching(item);
                }
            });
        }
    }

    boolean isProvided(final RobotElement element) {
        return cases.contains(element);
    }
}
