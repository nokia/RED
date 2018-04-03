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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.jface.preferences.ComboBoxFieldEditor;
import org.robotframework.red.jface.preferences.RegexValidatedStringFieldEditor;
import org.robotframework.red.junit.ShellProvider;

public class SuiteEditorPreferencePageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final SuiteEditorPreferencePage page = new SuiteEditorPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void checkIfEditorsForAllSuiteEditorPreferencesAreDefined() throws Exception {
        final SuiteEditorPreferencePage page = new SuiteEditorPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<FieldEditor> editors = FieldEditorPreferencePageHelper.getEditors(page);
        assertThat(editors).hasSize(7);

        final Map<Class<?>, List<String>> namesGroupedByType = editors.stream()
                .collect(groupingBy(FieldEditor::getClass, mapping(FieldEditor::getPreferenceName, toList())));
        assertThat(namesGroupedByType).hasEntrySatisfying(BooleanFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.PARENT_DIRECTORY_NAME_IN_TAB));
        assertThat(namesGroupedByType).hasEntrySatisfying(ComboBoxFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.FILE_ELEMENTS_OPEN_MODE,
                        RedPreferences.CELL_WRAPPING, RedPreferences.BEHAVIOR_ON_CELL_COMMIT));
        assertThat(namesGroupedByType).hasEntrySatisfying(IntegerFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS));
        assertThat(namesGroupedByType).hasEntrySatisfying(RadioGroupFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.SEPARATOR_MODE));
        assertThat(namesGroupedByType).hasEntrySatisfying(RegexValidatedStringFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.SEPARATOR_TO_USE));
    }
}
