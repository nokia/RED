/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.rf.ide.core.testdata.model.ModelType;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordDefinition;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.nattable.KeywordsEditorFormFragment.KeywordsTreeFormat;

import com.google.common.base.Predicate;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TreeList;

public class KeywordsDataProvider implements IDataProvider, IRowDataProvider<Object> {

    public static final Object ADDING_TOKEN = new Object();

    private RobotKeywordsSection section;

    private SortedList<Object> keywordsSortedList;
    
    private TreeList<Object> keywords;
    
    private KeywordsColumnsPropertyAccessor propertyAccessor;
    
    private KeywordsTreeFormat keywordsTreeFormat;
    
    public KeywordsDataProvider(final RobotEditorCommandsStack commandsStack,
            final RobotKeywordsSection section) {
        this.section = section;
        propertyAccessor = new KeywordsColumnsPropertyAccessor(commandsStack, countImportSettingsTableColumnsNumber());
        keywordsTreeFormat = new KeywordsTreeFormat();
        createFrom(section);
    }
    
    private void createFrom(final RobotKeywordsSection section) {
        if (keywords == null) {
            keywordsSortedList = new SortedList<>(GlazedLists.<Object> eventListOf(), null);
            keywords = new TreeList<Object>(keywordsSortedList, keywordsTreeFormat, TreeList.NODES_START_EXPANDED);
        }
        if (section != null) {
            keywordsSortedList.clear();
            
            for (RobotKeywordDefinition robotKeywordDefinition : section.getChildren()) {
                keywordsSortedList.add(robotKeywordDefinition);
                keywordsSortedList.addAll(newArrayList(filter(robotKeywordDefinition.getChildren(), new Predicate<RobotKeywordCall>() {

                    @Override
                    public boolean apply(final RobotKeywordCall call) {
                        final ModelType modelType = call.getLinkedElement().getModelType();
                        return modelType != ModelType.USER_KEYWORD_DOCUMENTATION && modelType != ModelType.USER_KEYWORD_ARGUMENTS;
                    }
                })));
                keywordsSortedList.add(new RobotKeywordCallAdder(robotKeywordDefinition));
            }
        }

    }
     
    public void setInput(final RobotKeywordsSection section) {
        this.section = section;
        createFrom(section);
        propertyAccessor.setNumberOfColumns(countImportSettingsTableColumnsNumber());
    }

    public RobotKeywordsSection getInput() {
        return section;
    }
    
    SortedList<Object> getSortedList() {
        return keywordsSortedList;
    }
    
    TreeList<Object> getTreeList() {
        return keywords;
    }

    public KeywordsTreeFormat getKeywordsTreeFormat() {
        return keywordsTreeFormat;
    }

    @Override
    public Object getRowObject(final int rowIndex) {

        if(rowIndex < keywords.size()) {
            return keywords.get(rowIndex);
        } else if (rowIndex == keywords.size()) {
            return ADDING_TOKEN;
        }
        return null;
    }
    
    @Override
    public int indexOfRowObject(Object rowObject) {
        if (section != null) {
            final int realRowIndex = keywords.indexOf(rowObject);
            int filteredIndex = realRowIndex;
            
            //TODO: handle filtering

            return filteredIndex;
        } else if (rowObject == ADDING_TOKEN) {
            return keywords.size();
        }
        return -1;
    }
    
    @Override
    public Object getDataValue(int colIndex, int rowIndex) {
        if (section != null) {
            if (rowIndex == keywords.size()) {
                return colIndex == 0 ? "...add new keyword" : "";
            }
            
            final Object keyword = getRowObject(rowIndex);
            return propertyAccessor.getDataValue(keyword, colIndex);
        }
        return "";
    }
    
    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        if (newValue instanceof RobotElement) {
            return;
        }
        final Object keyword = getRowObject(rowIndex);
        propertyAccessor.setDataValue(keyword, columnIndex, newValue);
    }
    
    @Override
    public int getRowCount() {
        if (section != null) {
            return keywords.size() + 1; // + keyword definition adder
        }
        return 0;
    }
    
    @Override
    public int getColumnCount() {
        return propertyAccessor.getColumnCount();
    }

    void setMatches(final HeaderFilterMatchesCollection matches) {
        
    }

    private int countImportSettingsTableColumnsNumber() {
        return calculateLongestArgumentsLength() + 2; // keyword name + args + comment
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        final List<?> elements = (List<?>) keywords;
        if (elements != null) {
            for (final Object element : elements) {
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

    public KeywordsColumnsPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }
    
    public static class RobotKeywordCallAdder {

        private RobotKeywordDefinition parent;

        public RobotKeywordCallAdder(final RobotKeywordDefinition parent) {
            this.parent = parent;
        }

        public RobotKeywordDefinition getParent() {
            return parent;
        }
    }
}
