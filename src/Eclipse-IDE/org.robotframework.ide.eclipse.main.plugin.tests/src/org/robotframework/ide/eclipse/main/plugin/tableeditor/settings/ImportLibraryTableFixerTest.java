/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.settings;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.ProjectProvider;

public class ImportLibraryTableFixerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ImportLibraryTableFixerTest.class);

    @Test
    public void libraryImportIsAdded_whenSettingsSectionDoesNotExist() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject, projectProvider.createFile("suite.robot"));

        final ImportLibraryTableFixer fixer = new ImportLibraryTableFixer("LibToFix");
        fixer.apply(robotFile);

        final Optional<RobotSettingsSection> section = robotFile.findSection(RobotSettingsSection.class);
        assertThat(section).isPresent();
        assertThat(section.get().getLibrariesPathsOrNamesWithAliases().keySet()).containsOnly("LibToFix");
    }

    @Test
    public void libraryImportIsAdded_whenEmptySettingsSectionExists() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject,
                projectProvider.createFile("suite.robot", "*** Settings ***"));

        final ImportLibraryTableFixer fixer = new ImportLibraryTableFixer("LibToFix");
        fixer.apply(robotFile);

        final Optional<RobotSettingsSection> section = robotFile.findSection(RobotSettingsSection.class);
        assertThat(section).isPresent();
        assertThat(section.get().getLibrariesPathsOrNamesWithAliases().keySet()).containsOnly("LibToFix");
    }

    @Test
    public void libraryImportIsAdded_whenNotEmptySettingsSectionExists() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject,
                projectProvider.createFile("suite.robot", "*** Settings ***", "Library  SomeLib"));

        final ImportLibraryTableFixer fixer = new ImportLibraryTableFixer("LibToFix");
        fixer.apply(robotFile);

        final Optional<RobotSettingsSection> section = robotFile.findSection(RobotSettingsSection.class);
        assertThat(section).isPresent();
        assertThat(section.get().getLibrariesPathsOrNamesWithAliases().keySet()).containsOnly("SomeLib", "LibToFix");
    }

    @Test
    public void libraryImportIsAdded_whenSeveralSettingsSectionsExist() throws Exception {
        final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());
        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject, projectProvider.createFile("suite.robot",
                "*** Settings ***", "Library  FirstLib", "*** Settings ***", "Library  SecondLib"));

        final ImportLibraryTableFixer fixer = new ImportLibraryTableFixer("LibToFix");
        fixer.apply(robotFile);

        final Optional<RobotSettingsSection> section = robotFile.findSection(RobotSettingsSection.class);
        assertThat(section).isPresent();
        assertThat(section.get().getLibrariesPathsOrNamesWithAliases().keySet()).containsOnly("FirstLib", "SecondLib",
                "LibToFix");
    }
}
