/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.Editors;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class RobotFormEditorTest {

    @Project(dirs = { "test_A", "test_A/test_B" })
    static IProject project;

    @BeforeAll
    public static void beforeClass() throws Exception {
        createFile(project, "suite.robot", "*** Settings ***");
        createFile(project, "test_A/suite.robot", "*** Settings ***");
        createFile(project, "test_A/test_B/suite.robot", "*** Settings ***");
    }

    @AfterEach
    public void afterTest() {
        Editors.closeAll();
    }

    @Test
    public void byDefaultEditorPartNameIsNotPrefixedWithProjectName() throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(getFile(project, "suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("suite.robot");
    }

    @BooleanPreference(key = RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, value = true)
    @Test
    public void editorPartNameIsPrefixedWithProjectName_whenParentDirectoryNamePreferenceIsEnabled() throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(getFile(project, "suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("RobotFormEditorTest/suite.robot");
    }

    @Test
    public void byDefaultEditorPartNameIsNotPrefixedWithDirectoryName() throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(getFile(project, "test_A/suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("suite.robot");
    }

    @BooleanPreference(key = RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, value = true)
    @Test
    public void editorPartNameIsPrefixedWithDirectoryName_whenParentDirectoryNamePreferenceIsEnabled()
            throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(getFile(project, "test_A/suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("test_A/suite.robot");
    }

    @Test
    public void byDefaultEditorPartNameIsNotPrefixedWithNestedDirectoryName() throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(getFile(project, "test_A/test_B/suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("suite.robot");
    }

    @BooleanPreference(key = RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, value = true)
    @Test
    public void editorPartNameIsPrefixedWithNestedDirectoryName_whenParentDirectoryNamePreferenceIsEnabled()
            throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(getFile(project, "test_A/test_B/suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("test_B/suite.robot");
    }
}
