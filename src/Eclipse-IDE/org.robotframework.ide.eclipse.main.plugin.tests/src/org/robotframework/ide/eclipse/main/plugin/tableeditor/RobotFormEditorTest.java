/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.Editors;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;

public class RobotFormEditorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(RobotFormEditorTest.class);

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    @BeforeClass
    public static void beforeClass() throws Exception {
        projectProvider.createFile("suite.robot", "*** Settings ***");
        projectProvider.createDir("test_A");
        projectProvider.createFile("test_A/suite.robot", "*** Settings ***");
        projectProvider.createDir("test_A/test_B");
        projectProvider.createFile("test_A/test_B/suite.robot", "*** Settings ***");
    }

    @After
    public void afterTest() {
        Editors.closeAll();
    }

    @Test
    public void byDefaultEditorPartNameIsNotPrefixedWithProjectName() throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(projectProvider.getFile("suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("suite.robot");
    }

    @Test
    public void editorPartNameIsPrefixedWithProjectName_whenParentDirectoryNamePreferenceIsEnabled() throws Exception {
        preferenceUpdater.setValue(RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, true);

        final RobotFormEditor editor = Editors.openInRobotEditor(projectProvider.getFile("suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("RobotFormEditorTest/suite.robot");
    }

    @Test
    public void byDefaultEditorPartNameIsNotPrefixedWithDirectoryName() throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(projectProvider.getFile("test_A/suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("suite.robot");
    }

    @Test
    public void editorPartNameIsPrefixedWithDirectoryName_whenParentDirectoryNamePreferenceIsEnabled()
            throws Exception {
        preferenceUpdater.setValue(RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, true);

        final RobotFormEditor editor = Editors.openInRobotEditor(projectProvider.getFile("test_A/suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("test_A/suite.robot");
    }

    @Test
    public void byDefaultEditorPartNameIsNotPrefixedWithNestedDirectoryName() throws Exception {
        final RobotFormEditor editor = Editors.openInRobotEditor(projectProvider.getFile("test_A/test_B/suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("suite.robot");
    }

    @Test
    public void editorPartNameIsPrefixedWithNestedDirectoryName_whenParentDirectoryNamePreferenceIsEnabled()
            throws Exception {
        preferenceUpdater.setValue(RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB, true);

        final RobotFormEditor editor = Editors.openInRobotEditor(projectProvider.getFile("test_A/test_B/suite.robot"));

        assertThat(editor.getPartName()).isEqualTo("test_B/suite.robot");
    }
}
