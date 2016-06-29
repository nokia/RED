/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.nattable;

import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.GeneralSettingsModel;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.SettingsMatchesFilter;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

public class GeneralSettingsDataProvider
        implements IFilteringDataProvider, IRowDataProvider<Entry<String, RobotElement>> {

    private RobotSettingsSection section;

    private SettingsMatchesFilter filter;

    private SortedList<Entry<String, RobotElement>> generalSettings;

    private final GeneralSettingsColumnsPropertyAccessor propertyAccessor;

    public GeneralSettingsDataProvider(final RobotEditorCommandsStack commandsStack,
            final RobotSettingsSection section) {
        this.section = section;
        this.generalSettings = createFrom(section);
        this.propertyAccessor = new GeneralSettingsColumnsPropertyAccessor(section, commandsStack,
                countGeneralSettingsTableColumnsNumber());
    }

    private SortedList<Entry<String, RobotElement>> createFrom(final RobotSettingsSection section) {
        if (generalSettings == null) {
            generalSettings = new SortedList<>(GlazedLists.<Entry<String, RobotElement>> eventListOf(), null);
        }
        if (section != null) {
            generalSettings.clear();
            generalSettings.addAll(GeneralSettingsModel.fillSettingsMapping(section).entrySet());
        }
        return generalSettings;
    }

    public void setInput(final RobotSettingsSection section) {
        this.section = section;
        this.generalSettings = createFrom(section);
        propertyAccessor.setSection(section);
        propertyAccessor.setNumberOfColumns(countGeneralSettingsTableColumnsNumber());
    }

    public RobotSettingsSection getInput() {
        return section;
    }

    @Override
    public int getColumnCount() {
        return propertyAccessor.getColumnCount();
    }

    @Override
    public Object getDataValue(final int columnIndex, final int rowIndex) {
        if (section != null) {
            final Entry<String, RobotElement> settingEntry = getRowObject(rowIndex);
            return propertyAccessor.getDataValue(settingEntry, columnIndex);
        }
        return "";
    }

    @Override
    public void setDataValue(final int columnIndex, final int rowIndex, final Object newValue) {
        if (newValue instanceof Entry<?, ?>) {
            return;
        }
        final Entry<String, RobotElement> settingEntry = getRowObject(rowIndex);
        propertyAccessor.setDataValue(settingEntry, columnIndex, newValue);
    }

    @Override
    public int getRowCount() {
        if (section != null) {
            return generalSettings.size() - countInvisible(); //no need to add one more row count for element adder row
        }
        return 0;
    }

    @Override
    public Entry<String, RobotElement> getRowObject(final int rowIndex) {
        if (section != null && rowIndex < generalSettings.size()) {
            Entry<String, RobotElement> rowObject = null;

            int count = 0;
            int realRowIndex = 0;
            while (count <= rowIndex) {
                rowObject = generalSettings.get(realRowIndex);
                final RobotSetting setting = (RobotSetting) rowObject.getValue();
                if (isPassingThroughFilter(setting)) {
                    count++;
                }
                realRowIndex++;
            }
            return rowObject;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Entry<String, RobotElement> settingEntry) {
        if (section != null) {
            final int realRowIndex = generalSettings.indexOf(settingEntry);
            int filteredIndex = realRowIndex;

            RobotSetting currentRowElement = null;
            for (int i = 0; i <= realRowIndex; i++) {
                currentRowElement = (RobotSetting) generalSettings.get(i).getValue();
                if (!isPassingThroughFilter(currentRowElement)) {
                    filteredIndex--;
                }
            }
            return filteredIndex;
        }
        return -1;
    }

    boolean isPassingThroughFilter(final RobotSetting rowObject) {
        return filter == null || filter.select(null, null, rowObject);
    }

    void setMatches(final HeaderFilterMatchesCollection matches) {
        this.filter = matches == null ? null : new SettingsMatchesFilter(matches);
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    private int countInvisible() {
        int numberOfInvisible = 0;
        for (final Entry<String, RobotElement> settingEntry : generalSettings) {
            final RobotSetting setting = (RobotSetting) settingEntry.getValue();
            if (!isPassingThroughFilter(setting)) {
                numberOfInvisible++;
            }
        }
        return numberOfInvisible;
    }

    private int countGeneralSettingsTableColumnsNumber() {
        return calculateLongestArgumentsLength() + 2; // setting name + args + comment
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMimalNumberOfArgumentColumns();
        if (generalSettings != null) {
            for (final Entry<String, RobotElement> element : generalSettings) {
                final RobotSetting setting = (RobotSetting) element.getValue();
                if (setting != null) {
                    max = Math.max(max, setting.getArguments().size());
                }
            }
        }
        return max;
    }

    public GeneralSettingsColumnsPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }


    public SortedList<Entry<String, RobotElement>> getSortedList() {
        return generalSettings;
    }
}
