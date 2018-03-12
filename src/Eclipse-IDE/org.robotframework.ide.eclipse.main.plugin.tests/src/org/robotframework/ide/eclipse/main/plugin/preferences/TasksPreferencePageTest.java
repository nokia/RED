/*
* Copyright 2018 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;


public class TasksPreferencePageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @AfterClass
    public static void afterSuite() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.TASKS_DETECTION_ENABLED,
                store.getDefaultString(RedPreferences.TASKS_DETECTION_ENABLED));
        store.putValue(RedPreferences.TASKS_TAGS, store.getDefaultString(RedPreferences.TASKS_TAGS));
        store.putValue(RedPreferences.TASKS_PRIORITIES, store.getDefaultString(RedPreferences.TASKS_PRIORITIES));
    }

    @Test
    public void thereIsAnEnablementButtonAndATableForTagsPlacedAtThePage() {
        final TasksPreferencePage page = new TasksPreferencePage();
        page.createControl(shellProvider.getShell());

        final Table table = getTable();
        assertThat(table).isNotNull();

        final Button button = getEnablementButton();
        assertThat(button).isNotNull();
        assertThat(button.getText()).isEqualTo("Enable tasks detection");
    }

    @Test
    public void buttonHasSelection_whenTasksDetectionIsEnabled() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.TASKS_DETECTION_ENABLED, Boolean.TRUE.toString());

        final TasksPreferencePage page = new TasksPreferencePage();
        page.createControl(shellProvider.getShell());

        final Button button = getEnablementButton();
        assertThat(button.getSelection()).isTrue();
    }

    @Test
    public void tableDisplaysAllTheTasksTags() {
        final IPreferenceStore store = RedPlugin.getDefault().getPreferenceStore();
        store.putValue(RedPreferences.TASKS_TAGS, "X;Y;Z");
        store.putValue(RedPreferences.TASKS_PRIORITIES, "LOW;NORMAL;HIGH");

        final TasksPreferencePage page = new TasksPreferencePage();
        page.createControl(shellProvider.getShell());

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
        return Stream.of(((Composite) shellProvider.getShell().getChildren()[0]).getChildren())
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .findFirst()
                .orElse(null);
    }

    private Table getTable() {
        return Stream.of(((Composite) shellProvider.getShell().getChildren()[0]).getChildren())
                .filter(Table.class::isInstance)
                .map(Table.class::cast)
                .findFirst()
                .orElse(null);
    }
}
