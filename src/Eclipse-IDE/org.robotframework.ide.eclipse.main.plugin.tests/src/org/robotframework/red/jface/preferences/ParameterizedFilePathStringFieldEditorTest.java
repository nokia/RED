/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.red.junit.ProjectProvider;
import org.robotframework.red.junit.ShellProvider;

public class ParameterizedFilePathStringFieldEditorTest {

    private static final String PROJECT_NAME = ParameterizedFilePathStringFieldEditorTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("existing_file.txt");
    }

    @Test
    public void stateIsValid_whenFilePathIsEmpty() {
        final ParameterizedFilePathStringFieldEditor editor = new ParameterizedFilePathStringFieldEditor("foo", "label",
                shellProvider.getShell());

        editor.getTextControl().setText("");
        assertThat(editor.checkState()).isTrue();
    }

    @Test
    public void stateIsValid_whenFileExists() {
        final ParameterizedFilePathStringFieldEditor editor = new ParameterizedFilePathStringFieldEditor("foo", "label",
                shellProvider.getShell());

        editor.getTextControl().setText(projectProvider.getFile("existing_file.txt").getLocation().toOSString());
        assertThat(editor.checkState()).isTrue();

        editor.getTextControl().setText("${workspace_loc:/" + PROJECT_NAME + "/existing_file.txt}");
        assertThat(editor.checkState()).isTrue();
    }

    @Test
    public void stateIsInvalid_whenFileDoesNotExist() {
        final ParameterizedFilePathStringFieldEditor editor = new ParameterizedFilePathStringFieldEditor("foo", "label",
                shellProvider.getShell());

        editor.getTextControl().setText("not_existing_file.txt");
        assertThat(editor.checkState()).isFalse();

        editor.getTextControl().setText("${workspace_loc:/" + PROJECT_NAME + "/not_existing_file.txt}");
        assertThat(editor.checkState()).isFalse();
    }

}
