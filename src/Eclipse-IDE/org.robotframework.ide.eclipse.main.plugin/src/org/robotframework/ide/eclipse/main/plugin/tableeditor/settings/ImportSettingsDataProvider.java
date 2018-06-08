/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.AddingToken;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.red.nattable.IFilteringDataProvider;

import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.matchers.Matcher;

public class ImportSettingsDataProvider implements IFilteringDataProvider, IRowDataProvider<Object> {

    private final AddingToken addingToken = new AddingToken(null, SettingsAdderState.IMPORT);

    private RobotSettingsSection section;

    private SortedList<RobotKeywordCall> imports;
    private FilterList<RobotKeywordCall> filteredImports;

    private SettingsMatchesFilter filter;

    private final ImportSettingsColumnsPropertyAccessor propertyAccessor;

    public ImportSettingsDataProvider(final RobotEditorCommandsStack commandsStack,
            final RobotSettingsSection section) {
        this.propertyAccessor = new ImportSettingsColumnsPropertyAccessor(commandsStack,
                countImportSettingsTableColumnsNumber());
        setInput(section);
    }

    void setInput(final RobotSettingsSection section) {
        this.section = section;
        createLists(section);
        this.propertyAccessor.setNumberOfColumns(countImportSettingsTableColumnsNumber());
    }

    private int countImportSettingsTableColumnsNumber() {
        return calculateLongestArgumentsLength() + 3; // setting name + args + empty cell + comment
    }

    private int calculateLongestArgumentsLength() {
        int max = RedPlugin.getDefault().getPreferences().getMinimalNumberOfArgumentColumns();
        if (filteredImports != null) {
            for (final Object element : filteredImports) {
                final RobotSetting setting = (RobotSetting) element;
                if (setting != null) {
                    max = Math.max(max, setting.getArguments().size());
                }
            }
        }
        return max;
    }

    private void createLists(final RobotSettingsSection section) {
        if (imports == null) {
            imports = new SortedList<>(GlazedLists.<RobotKeywordCall>eventListOf(), null);
            filteredImports = new FilterList<>(imports);
        }
        if (section != null) {
            filteredImports.setMatcher(null);
            imports.clear();
            imports.addAll(section.getImportSettings());
        }
    }

    SortedList<RobotKeywordCall> getSortedList() {
        return imports;
    }

    RobotSettingsSection getInput() {
        return section;
    }

    ImportSettingsColumnsPropertyAccessor getPropertyAccessor() {
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
            return filteredImports.size() + addingTokens;
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
        final Object element = getRowObject(rowIndex);
        if (element instanceof RobotKeywordCall) {
            final String newStringValue = newValue != null ? (String) newValue : "";
            propertyAccessor.setDataValue((RobotKeywordCall) element, columnIndex, newStringValue);
        }
    }

    @Override
    public Object getRowObject(final int rowIndex) {
        if (section != null && rowIndex < filteredImports.size()) {
            return filteredImports.get(rowIndex);
        } else if (rowIndex == filteredImports.size()) {
            return addingToken;
        }
        return null;
    }

    @Override
    public int indexOfRowObject(final Object rowObject) {
        if (rowObject == addingToken) {
            return filteredImports.size();
        } else {
            return filteredImports.indexOf(rowObject);
        }
    }

    @Override
    public boolean isFilterSet() {
        return filter != null;
    }

    void setFilter(final SettingsMatchesFilter filter) {
        this.filter = filter;
        if (filter == null) {
            filteredImports.setMatcher(null);
        } else {
            filteredImports.setMatcher(new Matcher<RobotKeywordCall>() {
                @Override
                public boolean matches(final RobotKeywordCall item) {
                    return filter.isMatching(item);
                }
            });
        }
    }

    boolean isProvided(final RobotKeywordCall setting) {
        return filteredImports.contains(setting);
    }
}
