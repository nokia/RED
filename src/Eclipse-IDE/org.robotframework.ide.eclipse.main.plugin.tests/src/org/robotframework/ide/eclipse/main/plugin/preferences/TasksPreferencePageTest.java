/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.Controls;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.StringPreference;


@ExtendWith({ FreshShellExtension.class, PreferencesExtension.class })
public class TasksPreferencePageTest {

    @FreshShell
    Shell shell;

    @Test
    public void thereIsAnEnablementButtonAndATableForTagsPlacedAtThePage() {
        final TasksPreferencePage page = new TasksPreferencePage();
        page.createControl(shell);

        final Table table = getTable();
        assertThat(table).isNotNull();

        final Button button = getEnablementButton();
        assertThat(button).isNotNull();
        assertThat(button.getText()).isEqualTo("Enable tasks detection");
    }

    @BooleanPreference(key = RedPreferences.TASKS_DETECTION_ENABLED, value = true)
    @Test
    public void buttonHasSelection_whenTasksDetectionIsEnabled() {
        final TasksPreferencePage page = new TasksPreferencePage();
        page.createControl(shell);

        final Button button = getEnablementButton();
        assertThat(button.getSelection()).isTrue();
    }

    @StringPreference(key = RedPreferences.TASKS_TAGS, value = "X;Y;Z")
    @StringPreference(key = RedPreferences.TASKS_PRIORITIES, value = "LOW;NORMAL;HIGH")
    @Test
    public void tableDisplaysAllTheTasksTags() {
        final TasksPreferencePage page = new TasksPreferencePage();
        page.createControl(shell);

        final Table table = getTable();
        assertThat(table.getItemCount()).isEqualTo(4);
        assertThat(table.getItem(0).getText(0)).isEqualTo("X");
        assertThat(table.getItem(0).getText(1)).isEqualTo("Low");
        assertThat(table.getItem(1).getText(0)).isEqualTo("Y");
        assertThat(table.getItem(1).getText(1)).isEqualTo("Normal");
        assertThat(table.getItem(2).getText(0)).isEqualTo("Z");
        assertThat(table.getItem(2).getText(1)).isEqualTo("High");
    }

    private Button getEnablementButton() {
        return Controls.getControls(shell, Button.class).get(0);
    }

    private Table getTable() {
        return Controls.getControls(shell, Table.class).get(0);
    }
}
