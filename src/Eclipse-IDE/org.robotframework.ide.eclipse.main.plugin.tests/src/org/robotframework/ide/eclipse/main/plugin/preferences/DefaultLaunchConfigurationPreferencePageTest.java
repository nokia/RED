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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.jface.preferences.ParameterizedFilePathStringFieldEditor;
import org.robotframework.red.junit.Controls;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ShellProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultLaunchConfigurationPreferencePageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

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

        final boolean buttonFound = Controls.getControls((Composite) page.getControl(), Button.class)
                .stream()
                .anyMatch(button -> button.getText().equals("Export Client Script"));

        assertThat(buttonFound).isTrue();
    }

    @Test
    public void checkIfTableDisplaysAllEnvironmentVariables() throws JsonProcessingException {
        final Map<String, String> input = new LinkedHashMap<>();
        input.put("VAR_1", "some value");
        input.put("VAR_2", "1234");
        input.put("EMPTY_VAR", "");

        preferenceUpdater.setValue(RedPreferences.LAUNCH_ENVIRONMENT_VARIABLES,
                new ObjectMapper().writeValueAsString(input));

        final DefaultLaunchConfigurationPreferencePage page = new DefaultLaunchConfigurationPreferencePage();
        page.createControl(shellProvider.getShell());

        final Table table = Controls.getControls((Composite) page.getControl(), Table.class).get(0);

        assertThat(table.getItemCount()).isEqualTo(4);
        assertThat(table.getItem(0).getText(0)).isEqualTo("VAR_1");
        assertThat(table.getItem(0).getText(1)).isEqualTo("some value");
        assertThat(table.getItem(1).getText(0)).isEqualTo("VAR_2");
        assertThat(table.getItem(1).getText(1)).isEqualTo("1234");
        assertThat(table.getItem(2).getText(0)).isEqualTo("EMPTY_VAR");
        assertThat(table.getItem(2).getText(1)).isEqualTo("");
    }
}
