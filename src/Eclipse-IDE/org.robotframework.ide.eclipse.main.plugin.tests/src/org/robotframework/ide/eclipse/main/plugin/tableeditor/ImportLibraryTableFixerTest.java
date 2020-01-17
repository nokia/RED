/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.junit.Editors;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class ImportLibraryTableFixerTest {

    @Project
    static IProject project;

    private final RobotProject robotProject = new RobotModel().createRobotProject(project);

    @AfterEach
    public void after() {
        Editors.closeAll();
    }

    @Test
    public void libraryImportIsAdded_whenSettingsSectionDoesNotExist() throws Exception {
        final IFile suiteFile = createFile(project, "suite.robot");
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
        final IFile suiteFile = createFile(project, "suite.robot", "*** Settings ***");
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
        final IFile suiteFile = createFile(project, "suite.robot", "*** Settings ***", "Library  SomeLib");
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
        final IFile suiteFile = createFile(project, "suite.robot", "*** Settings ***", "Library  FirstLib",
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
