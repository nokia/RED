/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createDir;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.LibraryType;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class RedXmlVersionUpdaterTest {

    @Project
    static IProject project;

    @Test
    public void testAutoupdateIsNotPossible_forUnknownVersion() {
        final RobotProjectConfig configFile = new RobotProjectConfig();
        configFile.setVersion("unknown");
        final RobotProject robotProject = mock(RobotProject.class);
        when(robotProject.getRobotProjectConfig()).thenReturn(configFile);

        assertThat(RedXmlVersionUpdater.isAutoUpdatePossible(robotProject)).isFalse();
    }

    @Test
    public void testAutoupdateIsPossible_forOldestVersion() {
        final RobotProjectConfig configFile = new RobotProjectConfig();
        configFile.setVersion("1.0");
        final RobotProject robotProject = mock(RobotProject.class);
        when(robotProject.getRobotProjectConfig()).thenReturn(configFile);

        assertThat(RedXmlVersionUpdater.isAutoUpdatePossible(robotProject)).isTrue();
    }

    @Test
    public void testAutoupdateIsNotPossible_forCurrentVersion() {
        final RobotProjectConfig configFile = RobotProjectConfig.create();
        final RobotProject robotProject = mock(RobotProject.class);
        when(robotProject.getRobotProjectConfig()).thenReturn(configFile);

        assertThat(RedXmlVersionUpdater.isAutoUpdatePossible(robotProject)).isFalse();
    }

    @Test
    public void testExecuteRedXmlUpdate_updatedRedXmlProperly() throws Exception {
        final String projectName = RedXmlVersionUpdaterTest.class.getSimpleName();

        createDir(project, "path");
        createDir(project, "path/to");
        createFile(project, "path/to/__init__.py", "#init content here");
        createFile(project, "path/to/library.py", "#library content here");
        createFile(project, "path/to/lib.py", "class WithClassInside:", "def print_sth(self):",
                "       print(\"sth\")");

        final RobotProjectConfig configFile = new RobotProjectConfig();
        configFile.setVersion("1.0");
        configFile.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "library", projectName + "/path/to"));
        configFile.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "to.lib.WithClassInside", projectName + "/path"));
        configFile.addReferencedLibrary(ReferencedLibrary.create(LibraryType.PYTHON, "to", projectName + "/path"));
        configFile.addReferencedLibrary(
                ReferencedLibrary.create(LibraryType.PYTHON, "lib", projectName + "/non-existing"));

        RedXmlVersionUpdater.executeRedXmlUpdate(configFile);

        assertThat(configFile.getVersion().getVersion()).isEqualTo(RobotProjectConfig.CURRENT_VERSION);
        assertThat(configFile.getReferencedLibraries()).containsExactly(
                ReferencedLibrary.create(LibraryType.PYTHON, "library", projectName + "/path/to/library.py"),
                ReferencedLibrary.create(LibraryType.PYTHON, "to.lib.WithClassInside", projectName + "/path/to/lib.py"),
                ReferencedLibrary.create(LibraryType.PYTHON, "to", projectName + "/path/to/__init__.py"),
                ReferencedLibrary.create(LibraryType.PYTHON, "lib", projectName + "/non-existing"));
    }

    @Test
    public void testThatCurrentRedXmlVersion_hasItsOwnRecord_inTransitions() {
        // The purpose of this test is to fail if there is new red.xml version
        // without properly implemented RedXmlVersionTransition.
        // Add one for your version then recofigure this test if needed
        // in case of non-numeric version or multiple aliases.
        final int currentVersion = Integer.parseInt(RobotProjectConfig.CURRENT_VERSION);
        assertThat(currentVersion).isEqualTo(RedXmlVersionUpdater.RED_XML_TRANSITIONS.size());
    }

}
