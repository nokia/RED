/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;

@ExtendWith(FreshShellExtension.class)
public class LaunchingPreferencePageTest {

    @FreshShell
    Shell shell;

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final LaunchingPreferencePage page = new LaunchingPreferencePage();
        page.init(workbench);

        verifyNoInteractions(workbench);
    }

    @Test
    public void checkIfAllBooleanEditorsAreDefined() throws Exception {
        final LaunchingPreferencePage page = new LaunchingPreferencePage();
        page.createControl(shell);

        final List<BooleanFieldEditor> editors = FieldEditorPreferencePageHelper.getEditorsOfType(page,
                BooleanFieldEditor.class);

        assertThat(editors).extracting(FieldEditor::getPreferenceName)
                .containsOnly(RedPreferences.LAUNCH_USE_ARGUMENT_FILE,
                        RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE,
                        RedPreferences.LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT, RedPreferences.LIMIT_MSG_LOG_OUTPUT);

    }

    @Test
    public void checkIfAllIntegerEditorsAreDefined() throws Exception {
        final LaunchingPreferencePage page = new LaunchingPreferencePage();
        page.createControl(shell);

        final List<IntegerFieldEditor> editors = FieldEditorPreferencePageHelper.getEditorsOfType(page,
                IntegerFieldEditor.class);

        assertThat(editors).extracting(FieldEditor::getPreferenceName)
                .containsOnly(RedPreferences.LIMIT_MSG_LOG_LENGTH);
    }
}
