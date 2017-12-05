/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.Editors;
import org.robotframework.red.junit.ProjectProvider;

public class ImportLibraryTableFixerTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ImportLibraryTableFixerTest.class);

    private final RobotProject robotProject = new RobotModel().createRobotProject(projectProvider.getProject());

    @After
    public void after() {
        Editors.closeAll();
    }

    @Test
    public void libraryImportIsAdded_whenSettingsSectionDoesNotExist() throws Exception {
        final IFile suiteFile = projectProvider.createFile("suite.robot");
        Editors.openInRobotEditor(suiteFile);

        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject, suiteFile);

        final ImportLibraryTableFixer fixer = new ImportLibraryTableFixer("LibToFix");
        fixer.apply(robotFile);

        final Optional<RobotSettingsSection> section = robotFile.findSection(RobotSettingsSection.class);
        assertThat(section).isPresent();
        assertThat(section.get().getLibrariesSettings()).hasSize(1);
        assertThat(section.get().getLibrariesSettings().get(0).getLabel()).isEqualTo("Library");
        assertThat(section.get().getLibrariesSettings().get(0).getArguments()).containsExactly("LibToFix");
    }

    @Test
    public void libraryImportIsAdded_whenEmptySettingsSectionExists() throws Exception {
        final IFile suiteFile = projectProvider.createFile("suite.robot", "*** Settings ***");
        Editors.openInRobotEditor(suiteFile);

        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject, suiteFile);

        final ImportLibraryTableFixer fixer = new ImportLibraryTableFixer("LibToFix");
        fixer.apply(robotFile);

        final Optional<RobotSettingsSection> section = robotFile.findSection(RobotSettingsSection.class);
        assertThat(section.get().getLibrariesSettings()).hasSize(1);
        assertThat(section.get().getLibrariesSettings().get(0).getLabel()).isEqualTo("Library");
        assertThat(section.get().getLibrariesSettings().get(0).getArguments()).containsExactly("LibToFix");
    }

    @Test
    public void libraryImportIsAdded_whenNotEmptySettingsSectionExists() throws Exception {
        final IFile suiteFile = projectProvider.createFile("suite.robot", "*** Settings ***", "Library  SomeLib");
        Editors.openInRobotEditor(suiteFile);

        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject, suiteFile);

        final ImportLibraryTableFixer fixer = new ImportLibraryTableFixer("LibToFix");
        fixer.apply(robotFile);

        final Optional<RobotSettingsSection> section = robotFile.findSection(RobotSettingsSection.class);
        assertThat(section).isPresent();
        assertThat(section.get().getLibrariesSettings()).hasSize(2);
        assertThat(section.get().getLibrariesSettings().get(0).getLabel()).isEqualTo("Library");
        assertThat(section.get().getLibrariesSettings().get(0).getArguments()).containsExactly("SomeLib");
        assertThat(section.get().getLibrariesSettings().get(1).getLabel()).isEqualTo("Library");
        assertThat(section.get().getLibrariesSettings().get(1).getArguments()).containsExactly("LibToFix");
    }

    @Test
    public void libraryImportIsAdded_whenSeveralSettingsSectionsExist() throws Exception {
        final IFile suiteFile = projectProvider.createFile("suite.robot", "*** Settings ***", "Library  FirstLib",
                "*** Settings ***", "Library  SecondLib");
        Editors.openInRobotEditor(suiteFile);

        final RobotSuiteFile robotFile = new RobotSuiteFile(robotProject, suiteFile);

        final ImportLibraryTableFixer fixer = new ImportLibraryTableFixer("LibToFix");
        fixer.apply(robotFile);

        final Optional<RobotSettingsSection> section = robotFile.findSection(RobotSettingsSection.class);
        assertThat(section).isPresent();
        assertThat(section.get().getLibrariesSettings()).hasSize(3);
        assertThat(section.get().getLibrariesSettings().get(0).getLabel()).isEqualTo("Library");
        assertThat(section.get().getLibrariesSettings().get(0).getArguments()).containsExactly("FirstLib");
        assertThat(section.get().getLibrariesSettings().get(1).getLabel()).isEqualTo("Library");
        assertThat(section.get().getLibrariesSettings().get(1).getArguments()).containsExactly("SecondLib");
        assertThat(section.get().getLibrariesSettings().get(2).getLabel()).isEqualTo("Library");
        assertThat(section.get().getLibrariesSettings().get(2).getArguments()).containsExactly("LibToFix");
    }
}
