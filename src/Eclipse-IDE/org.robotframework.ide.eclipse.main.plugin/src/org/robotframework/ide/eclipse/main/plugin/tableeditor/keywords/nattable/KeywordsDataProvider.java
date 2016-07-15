/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.nattable.CodeElementsTreeFormat;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsMatchesCollection.KeywordsFilter;
import org.robotframework.red.nattable.IFilteringDataProvider;

import com.google.common.base.Predicate;

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
    
    public KeywordsDataProvider(final RobotEditorCommandsStack commandsStack,
            final RobotKeywordsSection section) {
        this.section = section;
        this.propertyAccessor = new KeywordsColumnsPropertyAccessor(commandsStack, countColumnsNumber());
        this.keywordsTreeFormat = new CodeElementsTreeFormat();
        createLists(section);
    }
    
    public void setInput(final RobotKeywordsSection section) {
        propertyAccessor.setNumberOfColumns(countColumnsNumber());
        this.section = section;
        createLists(section);
    }

    private void createLists(final RobotKeywordsSection section) {
        if (keywords == null) {
            keywordsSortedList = new SortedList<>(GlazedLists.<Object> eventListOf(), null);
            filteredList = new FilterList<>(keywordsSortedList);
            keywords = new TreeList<>(filteredList, keywordsTreeFormat, TreeList.nodesStartExpanded());
        }
        if (section != null) {
            keywordsSortedList.clear();
            
            for (final RobotKeywordDefinition robotKeywordDefinition : section.getChildren()) {
                keywordsSortedList.add(robotKeywordDefinition);
                keywordsSortedList.addAll(newArrayList(filter(robotKeywordDefinition.getChildren(), new Predicate<RobotKeywordCall>() {

                    @Override
                    public boolean apply(final RobotKeywordCall call) {
                        final ModelType modelType = call.getLinkedElement().getModelType();
                        return modelType != ModelType.USER_KEYWORD_DOCUMENTATION && modelType != ModelType.USER_KEYWORD_ARGUMENTS;
                    }
                })));
                keywordsSortedList.add(new AddingToken(robotKeywordDefinition, KeywordsAdderState.CALL));
            }
        }
    }

    private int countColumnsNumber() {
        return calculateLongestArgumentsLength() + 2; // keyword name + args + comment
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        if (keywords != null) {
            for (final Object element : keywords) {
                if (element instanceof RobotKeywordDefinition) {
                    final RobotKeywordDefinition keyword = (RobotKeywordDefinition) element;
                    if (keyword != null) {
                        max = Math.max(max, keyword.getEmbeddedArguments().size());
                    }
                } else if (element instanceof RobotKeywordCall) {
                    final RobotKeywordCall keyword = (RobotKeywordCall) element;
                    if (keyword != null) {
                        max = Math.max(max, keyword.getArguments().size());
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
