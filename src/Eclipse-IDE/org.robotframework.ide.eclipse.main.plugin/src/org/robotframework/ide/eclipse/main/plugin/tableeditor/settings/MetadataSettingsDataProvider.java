/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;

class MetadataSettingsDataProvider implements IFilteringDataProvider, IRowDataProvider<Object> {

    private final AddingToken addingToken = new AddingToken(null, SettingsAdderState.METADATA);

    private RobotSettingsSection section;

    private SortedList<RobotKeywordCall> metadata;
    private FilterList<RobotKeywordCall> filteredMetadata;

    private MetadataMatchesFilter filter;

    private final MetadataSettingsColumnsPropertyAccessor propertyAccessor;


    MetadataSettingsDataProvider(final RobotEditorCommandsStack commandsStack, final RobotSettingsSection section) {
        this.propertyAccessor = new MetadataSettingsColumnsPropertyAccessor(commandsStack);
        setInput(section);
    }

    void setInput(final RobotSettingsSection section) {
        this.section = section;
        createLists(section);
    }

    private void createLists(final RobotSettingsSection section) {
        if (metadata == null) {
            metadata = new SortedList<>(GlazedLists.<RobotKeywordCall> eventListOf(), null);
            filteredMetadata = new FilterList<>(metadata);
        }
        if (section != null) {
            filteredMetadata.setMatcher(null);
            metadata.clear();
            metadata.addAll(section.getMetadataSettings());
        }
    }

    SortedList<RobotKeywordCall> getSortedList() {
        return metadata;
    }

    RobotSettingsSection getInput() {
        return section;
    }

    MetadataSettingsColumnsPropertyAccessor getPropertyAccessor() {
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
            return filteredMetadata.size() + addingTokens;
        }
        return 0;
    }

    @Override
    public Object getDataValue(final int columnIndex, final int rowIndex) {
        if (section != null) {
            final Object element = getRowObject(rowIndex);
            if (element instanceof RobotKeywordCall) {
                return propertyAccessor.getDataValue((RobotKeywordCall) element, columnIndex);
            } else if (element instanceof AddingToken && columnIndex == 0 && !isFilterSet()) {
                return ((AddingToken) element).getLabel();
            }
        }
        return "";
    }

    @Override
    public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
        if (newValue instanceof RobotKeywordCall) {
            return;
        }
        final Object metadataSetting = getRowObject(rowIndex);
        if (metadataSetting instanceof RobotKeywordCall) {
            propertyAccessor.setDataValue((RobotKeywordCall) metadataSetting, columnIndex, newValue);
        }
    }

    @Override
    public Object getRowObject(final int rowIndex) {
        if (section != null && rowIndex < filteredMetadata.size()) {
            return filteredMetadata.get(rowIndex);
        } else if (rowIndex == filteredMetadata.size()) {
            return addingToken;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
        if (rowObject == addingToken) {
            return filteredMetadata.size();
        } else {
            return filteredMetadata.indexOf(rowObject);
        }
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    void setFilter(final MetadataMatchesFilter filter) {
        this.filter = filter;
        if (filter == null) {
            filteredMetadata.setMatcher(null);
        } else {
            filteredMetadata.setMatcher(new Matcher<RobotKeywordCall>() {
                @Override
                public boolean matches(final RobotKeywordCall item) {
                    return filter.isMatching(item);
                }
            });
        }
    }

    boolean isProvided(final RobotKeywordCall setting) {
        return filteredMetadata.contains(setting);
    }
}
