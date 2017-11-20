/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;

public class LaunchingPreferencePageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final LaunchingPreferencePage page = new LaunchingPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void checkIfAllBooleanEditorsAreDefined() throws Exception {
        final LaunchingPreferencePage page = new LaunchingPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<String> prefNames = FieldEditorPreferencePageHelper.getEditorsOfType(page,
                BooleanFieldEditor.class).stream().map(FieldEditor::getPreferenceName).collect(toList());

        assertThat(prefNames).containsOnly(RedPreferences.LAUNCH_USE_ARGUMENT_FILE,
                RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE,
                RedPreferences.LAUNCH_USE_SINGLE_COMMAND_LINE_ARGUMENT, RedPreferences.LIMIT_MSG_LOG_OUTPUT);

    }

    @Test
    public void checkIfAllIntegerEditorsAreDefined() throws Exception {
        final LaunchingPreferencePage page = new LaunchingPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<String> prefNames = FieldEditorPreferencePageHelper.getEditorsOfType(page, IntegerFieldEditor.class)
                .stream()
                .map(FieldEditor::getPreferenceName)
                .collect(toList());

        assertThat(prefNames).containsOnly(RedPreferences.LIMIT_MSG_LOG_LENGTH);
    }
}
