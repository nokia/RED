/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.nattable;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsMatchesFilter;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

public class ImportSettingsDataProvider implements IDataProvider, IRowDataProvider<Object> {
    
    private static final Object ADDING_TOKEN = new Object();

    private RobotSettingsSection section;

    private SettingsMatchesFilter filter;

    private SortedList<RobotKeywordCall> importSettings;
    
    private final ImportSettingsColumnsPropertyAccessor propertyAccessor;

    public ImportSettingsDataProvider(final RobotEditorCommandsStack commandsStack,
            final RobotSettingsSection section) {
        this.section = section;
        this.importSettings = createFrom(section);
        this.propertyAccessor = new ImportSettingsColumnsPropertyAccessor(commandsStack, countImportSettingsTableColumnsNumber());
    }

    private SortedList<RobotKeywordCall> createFrom(final RobotSettingsSection section) {
        if (importSettings == null) {
            importSettings = new SortedList<>(GlazedLists.<RobotKeywordCall> eventListOf(), null);
        }
        if (section != null) {
            importSettings.clear();
            importSettings.addAll(section.getImportSettings());
        }
        return importSettings;
    }

    public void setInput(final RobotSettingsSection section) {
        this.section = section;
        this.importSettings = createFrom(section);
        propertyAccessor.setNumberOfColumns(countImportSettingsTableColumnsNumber());
    }

    public RobotSettingsSection getInput() {
        return section;
    }
    
    SortedList<RobotKeywordCall> getSortedList() {
        return importSettings;
    }

    @Override
    public int getColumnCount() {
        return propertyAccessor.getColumnCount();
    }

    @Override
    public Object getDataValue(final int columnIndex, final int rowIndex) {
        if (section != null) {
            if (rowIndex == importSettings.size() - countInvisible()) {
                return columnIndex == 0 ? "...add new import" : "";
            }

            final Object importSetting = getRowObject(rowIndex);
            if (importSetting instanceof RobotKeywordCall) {
                return propertyAccessor.getDataValue((RobotKeywordCall) importSetting, columnIndex);
            }
        }
        return "";
    }

    @Override
    public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
        if (newValue instanceof RobotKeywordCall) {
            return;
        }
        final String newStringValue = newValue != null ? (String) newValue : "";
        final Object importSetting = getRowObject(rowIndex);
        if (importSetting != null && importSetting instanceof RobotKeywordCall) {
            propertyAccessor.setDataValue((RobotKeywordCall) importSetting, columnIndex, newStringValue);
        }
    }

    @Override
    public int getRowCount() {
        if (section != null) {
            return importSettings.size() - countInvisible() + 1;
        }
        return 0;
    }

    @Override
    public Object getRowObject(final int rowIndex) {
        if (section != null && rowIndex < importSettings.size()) {
            RobotKeywordCall rowObject = null;

            int count = 0;
            int realRowIndex = 0;
            while (count <= rowIndex) {
                rowObject = importSettings.get(realRowIndex);
                if (isPassingThroughFilter(rowObject)) {
                    count++;
                }
                realRowIndex++;
            }
            return rowObject;
        } else if (rowIndex == importSettings.size()) {
            return ADDING_TOKEN;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
        if (section != null) {
            final int realRowIndex = importSettings.indexOf(rowObject);
            int filteredIndex = realRowIndex;

            RobotKeywordCall currentRowElement = null;
            for (int i = 0; i <= realRowIndex; i++) {
                currentRowElement = importSettings.get(i);
                if (!isPassingThroughFilter(currentRowElement)) {
                    filteredIndex--;
                }
            }
            return filteredIndex;
        } else if (rowObject == ADDING_TOKEN) {
            return importSettings.size();
        }
        return -1;
    }

    private boolean isPassingThroughFilter(final RobotKeywordCall rowObject) {
        return filter == null || filter.select(null, null, rowObject);
    }

    void setMatches(final HeaderFilterMatchesCollection matches) {
        this.filter = matches == null ? null : new SettingsMatchesFilter(matches);
    }

    private int countInvisible() {
        int numberOfInvisible = 0;
        for (final RobotKeywordCall importSetting : importSettings) {
            if (!isPassingThroughFilter(importSetting)) {
                numberOfInvisible++;
            }
        }
        return numberOfInvisible;
    }

    private int countImportSettingsTableColumnsNumber() {
        return calculateLongestArgumentsLength() + 2; // setting name + args + comment
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        final List<?> elements = importSettings;
        if (elements != null) {
            for (final Object element : elements) {
                final RobotSetting setting = (RobotSetting) element;
                if (setting != null) {
                    max = Math.max(max, setting.getArguments().size());
                }
            }
        }
        return max;
    }

    public ImportSettingsColumnsPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

}
