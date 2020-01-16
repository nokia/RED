/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, FreshShellExtension.class })
public class ParameterizedFilePathStringFieldEditorTest {

    @Project(files = { "existing_file.txt" })
    static IProject project;

    @FreshShell
    Shell shell;

    @Test
    public void stateIsValid_whenFilePathIsEmpty() {
        final ParameterizedFilePathStringFieldEditor editor = new ParameterizedFilePathStringFieldEditor("foo", "label",
                shell);

        editor.getTextControl().setText("");
        assertThat(editor.checkState()).isTrue();
    }

    @Test
    public void stateIsValid_whenFileExists() {
        final ParameterizedFilePathStringFieldEditor editor = new ParameterizedFilePathStringFieldEditor("foo", "label",
                shell);

        editor.getTextControl().setText(getFile(project, "existing_file.txt").getLocation().toOSString());
        assertThat(editor.checkState()).isTrue();

        editor.getTextControl().setText("${workspace_loc:/" + project.getName() + "/existing_file.txt}");
        assertThat(editor.checkState()).isTrue();
    }

    @Test
    public void stateIsInvalid_whenFileDoesNotExist() {
        final ParameterizedFilePathStringFieldEditor editor = new ParameterizedFilePathStringFieldEditor("foo", "label",
                shell);

        editor.getTextControl().setText("not_existing_file.txt");
        assertThat(editor.checkState()).isFalse();

        editor.getTextControl().setText("${workspace_loc:/" + project.getName() + "/not_existing_file.txt}");
        assertThat(editor.checkState()).isFalse();
    }

    @Test
    public void valueIsInsertedIntoEditor() {
        final ParameterizedFilePathStringFieldEditor editor = new ParameterizedFilePathStringFieldEditor("foo", "label",
                shell);

        editor.getTextControl().setText("some_file.txt");
        editor.getTextControl().setSelection(5, 9);
        editor.insertValue("${variable}");

        assertThat(editor.getStringValue()).isEqualTo("some_${variable}.txt");
    }

}
