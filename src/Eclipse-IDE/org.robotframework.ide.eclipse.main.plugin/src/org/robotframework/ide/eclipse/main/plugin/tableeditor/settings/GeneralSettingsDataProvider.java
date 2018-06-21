/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import java.util.Map.Entry;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;

public class GeneralSettingsDataProvider
        implements IFilteringDataProvider, IRowDataProvider<Entry<String, RobotElement>> {

    private RobotSettingsSection section;

    private SortedList<Entry<String, RobotElement>> generalSettings;
    private FilterList<Entry<String, RobotElement>> filteredGeneralSettings;

    private SettingsMatchesFilter filter;

    private final GeneralSettingsColumnsPropertyAccessor propertyAccessor;

    public GeneralSettingsDataProvider(final RobotEditorCommandsStack commandsStack,
            final RobotSettingsSection section) {
        this.propertyAccessor = new GeneralSettingsColumnsPropertyAccessor(section, commandsStack,
                countGeneralSettingsTableColumnsNumber());
        setInput(section);
    }

    public void setInput(final RobotSettingsSection section) {
        propertyAccessor.setSection(section);
        this.section = section;
        createLists(section);
        propertyAccessor.setNumberOfColumns(countGeneralSettingsTableColumnsNumber());
    }

    private int countGeneralSettingsTableColumnsNumber() {
        return calculateLongestArgumentsLength() + 3; // setting name + args + empty cell + comment
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMinimalNumberOfArgumentColumns();
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

    private void createLists(final RobotSettingsSection section) {
        if (generalSettings == null) {
            @SuppressWarnings("unchecked")
            final EventList<Entry<String, RobotElement>> eventList = GlazedLists
                    .<Entry<String, RobotElement>>eventListOf();
            generalSettings = new SortedList<>(eventList, null);
            filteredGeneralSettings = new FilterList<>(generalSettings);
        }
        if (section != null) {
            filteredGeneralSettings.setMatcher(null);
            generalSettings.clear();
            generalSettings.addAll(GeneralSettingsModel.fillSettingsMapping(section).entrySet());
        }
    }

    SortedList<Entry<String, RobotElement>> getSortedList() {
        return generalSettings;
    }

    RobotSettingsSection getInput() {
        return section;
    }

    GeneralSettingsColumnsPropertyAccessor getPropertyAccessor() {
        return propertyAccessor;
    }

    @Override
    public int getColumnCount() {
        return propertyAccessor.getColumnCount();
    }

    @Override
    public int getRowCount() {
        if (section != null) {
            // no need to add one more row count for element adder row
            return filteredGeneralSettings.size();
        }
        return 0;
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
    public Entry<String, RobotElement> getRowObject(final int rowIndex) {
        if (section != null && rowIndex < filteredGeneralSettings.size()) {
            return filteredGeneralSettings.get(rowIndex);
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Entry<String, RobotElement> settingEntry) {
        if (section != null) {
            return filteredGeneralSettings.indexOf(settingEntry);
        }
        return -1;
    }

    Entry<String, RobotElement> getEntryForSetting(final RobotSetting setting) {
        for (final Entry<String, RobotElement> entry : generalSettings) {
            final RobotElement robotElement = entry.getValue();
            if (robotElement != null) {
                final RobotSetting entrySetting = (RobotSetting) robotElement;
                if (setting == entrySetting) {
                    return entry;
                }
            }
        }
        throw new IllegalStateException("There has to be the setting provided by data provider");
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    void setFilter(final SettingsMatchesFilter filter) {
        this.filter = filter;
        if (filter == null) {
            filteredGeneralSettings.setMatcher(null);
        } else {
            filteredGeneralSettings.setMatcher(new Matcher<Entry<String, RobotElement>>() {
                @Override
                public boolean matches(final Entry<String, RobotElement> item) {
                    return filter.isMatching(item);
                }
            });
        }
    }

    boolean isProvided(final Entry<String, RobotElement> setting) {
        return filteredGeneralSettings.contains(setting);
    }
}
