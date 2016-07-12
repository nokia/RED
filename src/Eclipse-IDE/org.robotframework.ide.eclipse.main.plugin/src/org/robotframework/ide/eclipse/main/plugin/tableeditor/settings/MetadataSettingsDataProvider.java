/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

class MetadataSettingsDataProvider implements IFilteringDataProvider, IRowDataProvider<Object> {

    private static final Object ADDING_TOKEN = new Object();

    private RobotSettingsSection section;

    private SortedList<RobotKeywordCall> metadataSettings;

    private final MetadataSettingsColumnsPropertyAccessor propertyAccessor;

    private MetadataMatchesFilter filter;

    MetadataSettingsDataProvider(final RobotEditorCommandsStack commandsStack, final RobotSettingsSection section) {
        this.section = section;
        this.metadataSettings = createFrom(section);
        this.propertyAccessor = new MetadataSettingsColumnsPropertyAccessor(commandsStack);
    }

    private SortedList<RobotKeywordCall> createFrom(final RobotSettingsSection section) {
        if (metadataSettings == null) {
            metadataSettings = new SortedList<>(GlazedLists.<RobotKeywordCall> eventListOf(), null);
        }
        if (section != null) {
            metadataSettings.clear();
            metadataSettings.addAll(section.getMetadataSettings());
        }
        return metadataSettings;
    }

    public void setInput(final RobotSettingsSection section) {
        this.section = section;
        this.metadataSettings = createFrom(section);
    }

    public RobotSettingsSection getInput() {
        return section;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getDataValue(final int columnIndex, final int rowIndex) {
        if (section != null) {
            if (rowIndex == metadataSettings.size() - countInvisible() && !isFilterSet()) {
                return columnIndex == 0 ? "...add new metadata" : "";
            }
            final Object metadataSetting = getRowObject(rowIndex);
            if (metadataSetting instanceof RobotKeywordCall) {
                return propertyAccessor.getDataValue((RobotKeywordCall) metadataSetting, columnIndex);
            }
        }
        return "";
    }

    @Override
    public int getRowCount() {
        if (section != null) {
            final int addingTokens = isFilterSet() ? 1 : 0;
            return metadataSettings.size() - countInvisible() + 1 - addingTokens;
        }
        return 0;
    }

    private int countInvisible() {
        int numberOfInvisible = 0;
        for (final RobotKeywordCall setting : metadataSettings) {
            if (!isPassingThroughFilter(setting)) {
                numberOfInvisible++;
            }
        }
        return numberOfInvisible;
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
        if (section != null && rowIndex < metadataSettings.size()) {
            RobotKeywordCall rowObject = null;

            int count = 0;
            int realRowIndex = 0;
            while (count <= rowIndex && realRowIndex < metadataSettings.size()) {
                rowObject = metadataSettings.get(realRowIndex);
                if (isPassingThroughFilter(rowObject)) {
                    count++;
                }
                realRowIndex++;
            }
            return rowObject;
        } else if (rowIndex == metadataSettings.size()) {
            return ADDING_TOKEN;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
        if (section != null) {
            final int realRowIndex = metadataSettings.indexOf(rowObject);
            int filteredIndex = realRowIndex;

            RobotKeywordCall currentRowElement = null;
            for (int i = 0; i <= realRowIndex; i++) {
                currentRowElement = metadataSettings.get(i);
                if (!isPassingThroughFilter(currentRowElement)) {
                    filteredIndex--;
                }
            }
            return filteredIndex;
        } else if (rowObject == ADDING_TOKEN) {
            return metadataSettings.size();
        }
        return -1;
    }

    boolean isPassingThroughFilter(final RobotKeywordCall rowObject) {
        return filter == null || filter.isMatching(rowObject);
    }

    void setMatches(final HeaderFilterMatchesCollection matches) {
        this.filter = matches == null ? null : new MetadataMatchesFilter(matches);
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    public MetadataSettingsColumnsPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    public SortedList<RobotKeywordCall> getSortedList() {
        return metadataSettings;
    }

}
