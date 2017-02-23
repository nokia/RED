/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;

public class DefaultLaunchConfigurationPreferencePageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final DefaultLaunchConfigurationPreferencePage page = new DefaultLaunchConfigurationPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void thereAreIntegerEditorsAndStringEditorsAndFileEditor() throws Exception {
        final DefaultLaunchConfigurationPreferencePage page = new DefaultLaunchConfigurationPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<String> integerPrefNames = newArrayList(RedPreferences.LAUNCH_REMOTE_PORT,
                RedPreferences.LAUNCH_REMOTE_TIMEOUT);
        final List<String> stringPrefNames = newArrayList(RedPreferences.LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS,
                RedPreferences.LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS, RedPreferences.LAUNCH_REMOTE_HOST,
                RedPreferences.LAUNCH_ADDITIONAL_SCRIPT_ARGUMENTS, RedPreferences.LAUNCH_SCRIPT_RUN_COMMAND);

        final List<FieldEditor> editors = FieldEditorPreferencePageHelper.getEditors(page);
        assertThat(editors).hasSize(8);
        for (final Object ed : editors) {
            final FieldEditor editor = (FieldEditor) ed;

            if (editor instanceof IntegerFieldEditor) {
                integerPrefNames.remove(editor.getPreferenceName());
            } else if (editor instanceof StringFieldEditor) {
                stringPrefNames.remove(editor.getPreferenceName());
            } else if (editor instanceof FileFieldEditor) {
                final FileFieldEditor fileFieldEditor = (FileFieldEditor) editor;
                assertThat(fileFieldEditor.getPreferenceName()).isEqualTo(RedPreferences.LAUNCH_SCRIPT_PATH);
            }
        }
        assertThat(stringPrefNames).isEmpty();
        assertThat(integerPrefNames).isEmpty();
    }
}
