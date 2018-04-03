/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditor;
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
import org.robotframework.red.jface.preferences.ParameterizedFilePathStringFieldEditor;
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

        final List<FieldEditor> editors = FieldEditorPreferencePageHelper.getEditors(page);
        assertThat(editors).hasSize(7);

        final Map<Class<?>, List<String>> namesGroupedByType = editors.stream()
                .collect(groupingBy(FieldEditor::getClass, mapping(FieldEditor::getPreferenceName, toList())));
        assertThat(namesGroupedByType).hasEntrySatisfying(IntegerFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.LAUNCH_AGENT_CONNECTION_PORT,
                        RedPreferences.LAUNCH_AGENT_CONNECTION_TIMEOUT));
        assertThat(namesGroupedByType).hasEntrySatisfying(ParameterizedFilePathStringFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.LAUNCH_EXECUTABLE_FILE_PATH));
        assertThat(namesGroupedByType).hasEntrySatisfying(StringFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS,
                        RedPreferences.LAUNCH_AGENT_CONNECTION_HOST,
                        RedPreferences.LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS,
                        RedPreferences.LAUNCH_ADDITIONAL_EXECUTABLE_FILE_ARGUMENTS));
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
