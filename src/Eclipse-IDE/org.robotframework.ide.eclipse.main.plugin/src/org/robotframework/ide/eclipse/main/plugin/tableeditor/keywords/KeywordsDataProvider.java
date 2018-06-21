/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.AModelElement;
import org.rf.ide.core.testdata.model.ModelType;
import org.rf.ide.core.testdata.model.table.keywords.UserKeyword;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotDefinitionSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.CodeElementsTreeFormat;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.ExecutablesRowHolderCommentService;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.KeywordsMatchesCollection.KeywordsFilter;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.matchers.Matcher;

public class KeywordsDataProvider implements IFilteringDataProvider, IRowDataProvider<Object> {

    private final AddingToken keywordsAddingToken = new AddingToken(null, KeywordsAdderState.KEYWORD);

    private RobotKeywordsSection section;

    private SortedList<Object> keywordsSortedList;

    private FilterList<Object> filteredList;

    private TreeList<Object> keywords;

    private final KeywordsColumnsPropertyAccessor propertyAccessor;

    private final CodeElementsTreeFormat keywordsTreeFormat;

    private KeywordsFilter filter;

    KeywordsDataProvider(final RobotEditorCommandsStack commandsStack, final RobotKeywordsSection section) {
        this.section = section;
        this.keywordsTreeFormat = new CodeElementsTreeFormat();
        createLists(section);
        this.propertyAccessor = new KeywordsColumnsPropertyAccessor(commandsStack, countColumnsNumber());
    }

    public void setInput(final RobotKeywordsSection section) {
        this.section = section;
        createLists(section);
        propertyAccessor.setNumberOfColumns(countColumnsNumber());
    }

    private void createLists(final RobotKeywordsSection section) {
        if (keywords == null) {
            keywordsSortedList = new SortedList<>(GlazedLists.<Object>eventListOf(), null);
            filteredList = new FilterList<>(keywordsSortedList);
            keywords = new TreeList<>(filteredList, keywordsTreeFormat, TreeList.nodesStartExpanded());
        }
        if (section != null) {
            keywordsSortedList.clear();

            for (final RobotKeywordDefinition robotKeywordDefinition : section.getChildren()) {
                keywordsSortedList.add(robotKeywordDefinition);
                keywordsSortedList.addAll(filteredCalls(robotKeywordDefinition));
                keywordsSortedList.add(new AddingToken(robotKeywordDefinition, KeywordsAdderState.CALL));
            }
        }
    }

    private List<RobotKeywordCall> filteredCalls(final RobotKeywordDefinition keywordDefinition) {
        final List<RobotKeywordCall> allCalls = keywordDefinition.getChildren();
        final List<RobotKeywordCall> filteredCalls = new ArrayList<>();
        for (final RobotKeywordCall call : allCalls) {
            if (call instanceof RobotDefinitionSetting) {
                final RobotDefinitionSetting setting = (RobotDefinitionSetting) call;
                @SuppressWarnings("unchecked")
                final AModelElement<UserKeyword> linkedSetting = (AModelElement<UserKeyword>) setting
                        .getLinkedElement();
                final UserKeyword userKeyword = keywordDefinition.getLinkedElement();
                if (!userKeyword.isDuplicatedSetting(linkedSetting)
                        && linkedSetting.getModelType() != ModelType.USER_KEYWORD_ARGUMENTS) {
                    filteredCalls.add(call);
                }
            } else {
                filteredCalls.add(call);
            }
        }
        return filteredCalls;
    }

    private int countColumnsNumber() {
        // add 1 for name column
        int max = 1 + RedPlugin.getDefault().getPreferences().getMinimalNumberOfArgumentColumns();
        if (keywords != null) {
            for (final Object element : keywords) {
                if (element instanceof RobotKeywordDefinition) {
                    final RobotKeywordDefinition keyword = (RobotKeywordDefinition) element;
                    max = Math.max(max, keyword.getEmbeddedArguments().size());

                } else if (element instanceof RobotKeywordCall) {
                    final RobotKeywordCall keyword = (RobotKeywordCall) element;
                    if (keyword.getLinkedElement().getModelType() != ModelType.USER_KEYWORD_DOCUMENTATION) {
                        // add 1 for empty cell
                        max = Math.max(max, ExecutablesRowHolderCommentService.execRowView(keyword).size() + 1);
                    }
                }
            }
        }
        return max;
    }

    SortedList<Object> getSortedList() {
        return keywordsSortedList;
    }

    TreeList<Object> getTreeList() {
        return keywords;
    }

    RobotKeywordsSection getInput() {
        return section;
    }

    CodeElementsTreeFormat getTreeFormat() {
        return keywordsTreeFormat;
    }

    KeywordsColumnsPropertyAccessor getPropertyAccessor() {
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
            return keywords.size() + addingTokens;
        }
        return 0;
    }

    @Override
    public Object getDataValue(final int colIndex, final int rowIndex) {
        if (section != null) {
            final Object element = getRowObject(rowIndex);
            if (element instanceof RobotElement) {
                return propertyAccessor.getDataValue(element, colIndex);
            } else if (element instanceof AddingToken && colIndex == 0) {
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
        if (rowIndex < keywords.size()) {
            return keywords.get(rowIndex);
        } else if (rowIndex == keywords.size()) {
            return keywordsAddingToken;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
        if (section != null) {
            return keywords.indexOf(rowObject);
        } else if (rowObject == keywordsAddingToken) {
            return keywords.size();
        }
        return -1;
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    void setFilter(final KeywordsFilter filter) {
        this.filter = filter;

        if (filter == null) {
            filteredList.setMatcher(null);
        } else {
            filteredList.setMatcher(new Matcher<Object>() {

                @Override
                public boolean matches(final Object item) {
                    return filter.isMatching(item);
                }
            });
        }
    }

    boolean isProvided(final RobotElement element) {
        return keywords.contains(element);
    }
}
