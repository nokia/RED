/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.red.junit.jupiter.IntegerPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class ImportSettingsDataProviderTest {

    @Project
    static IProject project;

    private final ImportSettingsDataProvider dataProvider = new ImportSettingsDataProvider(null, null);

    @Test
    public void columnsAreCountedCorrectly_whenSettingsSectionIsEmpty() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(8);
    }

    @Test
    public void columnsAreCountedCorrectly_whenImportSettingIsEmpty() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Variables"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(8);
    }

    @Test
    public void columnsAreCountedCorrectly_whenImportSettingArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Library    lib.py   a    b    c    d"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(8);
    }

    @Test
    public void columnsAreCountedCorrectly_whenImportSettingArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Library    lib.py    a    b    c    d    e    f"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(10);
    }

    @Test
    public void columnsAreCountedCorrectly_whenSettingsSectionContainsManyImportSettings() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Variables    a    b    c    d    e",
                "Library    lib.py    ${a}    ${b}    ${c}    ${d}    ${e}    ${f}    ${g}    ${h}",
                "Resource     res.robot    ${a}    ${b}    ${c}    ${d}    ${e}    ${f}    ${g}    ${h}"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(12);
    }

    @Test
    public void columnsAreCountedCorrectly_whenImportSettingArgumentsWithAliasExceedLimit() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Library    lib.py    a    b    c    d    e    f    WITH NAME    alias"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(12);
    }

    @IntegerPreference(key = RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS, value = 15)
    @Test
    public void columnsAreCountedCorrectly_whenMinimalArgumentsColumnsFieldIsChangedInPreferences() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Variables    a    b    c"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(18);
    }

    private RobotSettingsSection createSettingsSection(final String... lines) throws Exception {
        final IFile file = createFile(project, "__init__.robot", lines);
        final RobotModel model = new RobotModel();
        return model.createSuiteFile(file).findSection(RobotSettingsSection.class).get();
    }

}
