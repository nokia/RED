/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.nattable;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.MetadataMatchesFilter;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

class MetadataSettingsDataProvider implements IDataProvider, IRowDataProvider<RobotKeywordCall> {

    private RobotSettingsSection section;

    private SortedList<RobotKeywordCall> metadataSettings;

    private MetadataSettingsColumnsPropertyAccessor propertyAccessor;

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
            if (rowIndex == metadataSettings.size() - countInvisible()) {
                return columnIndex == 0 ? "...add new metadata" : "";
            }
            final RobotKeywordCall metadataSetting = getRowObject(rowIndex);
            return propertyAccessor.getDataValue(metadataSetting, columnIndex);
        }
        return "";
    }

    @Override
    public int getRowCount() {
        if (section != null) {
            return metadataSettings.size() - countInvisible() + 1;
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
        final RobotKeywordCall metadataSetting = getRowObject(rowIndex);
        propertyAccessor.setDataValue(metadataSetting, columnIndex, newValue);
    }

    @Override
    public RobotKeywordCall getRowObject(final int rowIndex) {
        if (section != null && rowIndex < metadataSettings.size()) {
            RobotKeywordCall rowObject = null;

            int count = 0;
            int realRowIndex = 0;
            while (count <= rowIndex) {
                rowObject = metadataSettings.get(realRowIndex);
                if (isPassingThroughFilter(rowObject)) {
                    count++;
                }
                realRowIndex++;
            }
            return rowObject;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final RobotKeywordCall rowObject) {
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
        }
        return -1;
    }

    private boolean isPassingThroughFilter(final RobotKeywordCall rowObject) {
        return filter == null || filter.select(null, null, rowObject);
    }

    void setMatches(final HeaderFilterMatchesCollection matches) {
        this.filter = matches == null ? null : new MetadataMatchesFilter(matches);
    }

    public MetadataSettingsColumnsPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    public SortedList<RobotKeywordCall> getSortedList() {
        return metadataSettings;
    }

}
