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
public class GeneralSettingsDataProviderTest {

    @Project
    static IProject project;

    private final GeneralSettingsDataProvider dataProvider = new GeneralSettingsDataProvider(null, null);

    @Test
    public void columnsAreCountedCorrectly_whenSettingsSectionIsEmpty() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(7);
    }

    @Test
    public void columnsAreCountedCorrectly_whenSettingIsEmpty() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Metadata"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(7);
    }

    @Test
    public void columnsAreCountedCorrectly_whenSettingArgumentsDoNotExceedLimit() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Force Tags    a    b    c    d"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(7);
    }

    @Test
    public void columnsAreCountedCorrectly_whenSettingArgumentsExceedLimit() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Force Tags    a    b    c    d    e    f"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(9);
    }

    @Test
    public void columnsAreCountedCorrectly_whenSettingsSectionContainsManySettings() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Force Tags    a    b    c    d    e",
                "Suite Setup    Log Many    ${a}    ${b}    ${c}    ${d}    ${e}",
                "Test Setup     Log Many    ${a}    ${b}    ${c}    ${d}    ${e}    ${f}    ${g}    ${h}"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(12);
    }

    @IntegerPreference(key = RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS, value = 15)
    @Test
    public void columnsAreCountedCorrectly_whenMinimalArgumentsColumnsFieldIsChangedInPreferences() throws Exception {
        dataProvider.setInput(createSettingsSection("*** Settings ***",
                "Force Tags    a    b    c"));

        assertThat(dataProvider.getColumnCount()).isEqualTo(17);
    }

    private RobotSettingsSection createSettingsSection(final String... lines) throws Exception {
        final IFile file = createFile(project, "__init__.robot", lines);
        final RobotModel model = new RobotModel();
        return model.createSuiteFile(file).findSection(RobotSettingsSection.class).get();
    }

}
