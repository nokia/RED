/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.code;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSetting;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.settings.ImportSettingsColumnsPropertyAccessor;

public class ImportSettingsColumnsPropertyAccessorTest {

    @Test
    public void cellValuesAreProvided_forLibraryImport() {
        final RobotSetting setting = createLibraryImport("Library  SomeLib  arg1  arg2  arg3");

        final ImportSettingsColumnsPropertyAccessor propertyAccessor = new ImportSettingsColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), 8);

        assertThat(propertyAccessor.getDataValue(setting, 0)).isEqualTo("Library");
        assertThat(propertyAccessor.getDataValue(setting, 1)).isEqualTo("SomeLib");
        assertThat(propertyAccessor.getDataValue(setting, 2)).isEqualTo("arg1");
        assertThat(propertyAccessor.getDataValue(setting, 3)).isEqualTo("arg2");
        assertThat(propertyAccessor.getDataValue(setting, 4)).isEqualTo("arg3");
        assertThat(propertyAccessor.getDataValue(setting, 5)).isEqualTo("");
        assertThat(propertyAccessor.getDataValue(setting, 6)).isEqualTo("");
        assertThat(propertyAccessor.getDataValue(setting, 7)).isEqualTo("");
    }

    @Test
    public void cellValuesAreProvided_forLibraryImportWithAlias() {
        final RobotSetting setting = createLibraryImport("Library  SomeLib  arg1  arg2  arg3  WITH NAME  alias");

        final ImportSettingsColumnsPropertyAccessor propertyAccessor = new ImportSettingsColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), 8);

        assertThat(propertyAccessor.getDataValue(setting, 0)).isEqualTo("Library");
        assertThat(propertyAccessor.getDataValue(setting, 1)).isEqualTo("SomeLib");
        assertThat(propertyAccessor.getDataValue(setting, 2)).isEqualTo("arg1");
        assertThat(propertyAccessor.getDataValue(setting, 3)).isEqualTo("arg2");
        assertThat(propertyAccessor.getDataValue(setting, 4)).isEqualTo("arg3");
        assertThat(propertyAccessor.getDataValue(setting, 5)).isEqualTo("WITH NAME");
        assertThat(propertyAccessor.getDataValue(setting, 6)).isEqualTo("alias");
        assertThat(propertyAccessor.getDataValue(setting, 7)).isEqualTo("");
    }

    @Test
    public void cellValuesAreProvided_forLibraryImportWithComment() {
        final RobotSetting setting = createLibraryImport("Library  SomeLib  arg1  arg2  arg3  #comment");

        final ImportSettingsColumnsPropertyAccessor propertyAccessor = new ImportSettingsColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), 8);

        assertThat(propertyAccessor.getDataValue(setting, 0)).isEqualTo("Library");
        assertThat(propertyAccessor.getDataValue(setting, 1)).isEqualTo("SomeLib");
        assertThat(propertyAccessor.getDataValue(setting, 2)).isEqualTo("arg1");
        assertThat(propertyAccessor.getDataValue(setting, 3)).isEqualTo("arg2");
        assertThat(propertyAccessor.getDataValue(setting, 4)).isEqualTo("arg3");
        assertThat(propertyAccessor.getDataValue(setting, 5)).isEqualTo("");
        assertThat(propertyAccessor.getDataValue(setting, 6)).isEqualTo("");
        assertThat(propertyAccessor.getDataValue(setting, 7)).isEqualTo("#comment");
    }

    @Test
    public void cellValuesAreProvided_forResourceImport() {
        final RobotSetting setting = createResourceImport("Resource  res.robot");

        final ImportSettingsColumnsPropertyAccessor propertyAccessor = new ImportSettingsColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), 5);

        assertThat(propertyAccessor.getDataValue(setting, 0)).isEqualTo("Resource");
        assertThat(propertyAccessor.getDataValue(setting, 1)).isEqualTo("res.robot");
        assertThat(propertyAccessor.getDataValue(setting, 2)).isEqualTo("");
        assertThat(propertyAccessor.getDataValue(setting, 3)).isEqualTo("");
        assertThat(propertyAccessor.getDataValue(setting, 4)).isEqualTo("");
    }

    @Test
    public void cellValuesAreProvided_forResourceImportWithComment() {
        final RobotSetting setting = createResourceImport("Resource  res.robot  #comment");

        final ImportSettingsColumnsPropertyAccessor propertyAccessor = new ImportSettingsColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), 5);

        assertThat(propertyAccessor.getDataValue(setting, 0)).isEqualTo("Resource");
        assertThat(propertyAccessor.getDataValue(setting, 1)).isEqualTo("res.robot");
        assertThat(propertyAccessor.getDataValue(setting, 2)).isEqualTo("");
        assertThat(propertyAccessor.getDataValue(setting, 3)).isEqualTo("");
        assertThat(propertyAccessor.getDataValue(setting, 4)).isEqualTo("#comment");
    }

    @Test
    public void cellValuesAreProvided_forVariablesImport() {
        final RobotSetting setting = createVariableImport("Variables  vars.py  1  2");

        final ImportSettingsColumnsPropertyAccessor propertyAccessor = new ImportSettingsColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), 5);

        assertThat(propertyAccessor.getDataValue(setting, 0)).isEqualTo("Variables");
        assertThat(propertyAccessor.getDataValue(setting, 1)).isEqualTo("vars.py");
        assertThat(propertyAccessor.getDataValue(setting, 2)).isEqualTo("1");
        assertThat(propertyAccessor.getDataValue(setting, 3)).isEqualTo("2");
        assertThat(propertyAccessor.getDataValue(setting, 4)).isEqualTo("");
    }

    @Test
    public void cellValuesAreProvided_forVariablesImportWithComment() {
        final RobotSetting setting = createVariableImport("Variables  vars.py  1  2  #comment");

        final ImportSettingsColumnsPropertyAccessor propertyAccessor = new ImportSettingsColumnsPropertyAccessor(
                new RobotEditorCommandsStack(), 5);

        assertThat(propertyAccessor.getDataValue(setting, 0)).isEqualTo("Variables");
        assertThat(propertyAccessor.getDataValue(setting, 1)).isEqualTo("vars.py");
        assertThat(propertyAccessor.getDataValue(setting, 2)).isEqualTo("1");
        assertThat(propertyAccessor.getDataValue(setting, 3)).isEqualTo("2");
        assertThat(propertyAccessor.getDataValue(setting, 4)).isEqualTo("#comment");
    }

    private static RobotSetting createLibraryImport(final String line) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine(line)
                .build();
        final RobotSettingsSection section = model.findSection(RobotSettingsSection.class).get();
        return section.getLibrariesSettings().get(0);
    }

    private static RobotSetting createResourceImport(final String line) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine(line)
                .build();
        final RobotSettingsSection section = model.findSection(RobotSettingsSection.class).get();
        return section.getResourcesSettings().get(0);
    }

    private static RobotSetting createVariableImport(final String line) {
        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLine("*** Settings ***")
                .appendLine(line)
                .build();
        final RobotSettingsSection section = model.findSection(RobotSettingsSection.class).get();
        return section.getVariablesSettings().get(0);
    }
}
