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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
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
    public void checkIfEditorsForAllLaunchConfigurationPreferencesAreDefined() throws Exception {
        final DefaultLaunchConfigurationPreferencePage page = new DefaultLaunchConfigurationPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<String> integerPrefNames = newArrayList(RedPreferences.LAUNCH_AGENT_CONNECTION_PORT,
                RedPreferences.LAUNCH_AGENT_CONNECTION_TIMEOUT);
        final List<String> stringPrefNames = newArrayList(RedPreferences.LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS,
                RedPreferences.LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS, RedPreferences.LAUNCH_AGENT_CONNECTION_HOST,
                RedPreferences.LAUNCH_ADDITIONAL_EXECUTABLE_FILE_ARGUMENTS);

        final List<FieldEditor> editors = FieldEditorPreferencePageHelper.getEditors(page);
        assertThat(editors).hasSize(7);
        for (final FieldEditor editor : editors) {
            if (editor instanceof IntegerFieldEditor) {
                integerPrefNames.remove(editor.getPreferenceName());
            } else if (editor instanceof StringFieldEditor) {
                stringPrefNames.remove(editor.getPreferenceName());
            } else if (editor instanceof FileFieldEditor) {
                assertThat(editor.getPreferenceName()).isEqualTo(RedPreferences.LAUNCH_EXECUTABLE_FILE_PATH);
            }
        }
        assertThat(integerPrefNames).isEmpty();
        assertThat(stringPrefNames).isEmpty();
    }

    @Test
    public void checkIfExportClientScriptButtonIsDefined() throws Exception {
        final DefaultLaunchConfigurationPreferencePage page = new DefaultLaunchConfigurationPreferencePage();
        page.createControl(shellProvider.getShell());

        boolean buttonFound = false;

        final Composite pageControl = (Composite) page.getControl();
        final Composite fieldEditorParent = (Composite) pageControl.getChildren()[1];
        for (final Control fieldEditorParentControl : fieldEditorParent.getChildren()) {
            if (fieldEditorParentControl instanceof Group) {
                for (final Control groupControl : ((Group) fieldEditorParentControl).getChildren()) {
                    if (groupControl instanceof Button) {
                        if (((Button) groupControl).getText().equals("Export Client Script")) {
                            buttonFound = true;
                            break;
                        }
                    }
                }
            }
        }

        assertThat(buttonFound).isTrue();
    }
}
