/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.jface.preferences.ComboBoxFieldEditor;
import org.robotframework.red.junit.jupiter.FreshShell;
import org.robotframework.red.junit.jupiter.FreshShellExtension;

@ExtendWith(FreshShellExtension.class)
public class ContentAssistPreferencePageTest {

    @FreshShell
    Shell shell;

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final ContentAssistPreferencePage page = new ContentAssistPreferencePage();
        page.init(workbench);

        verifyNoInteractions(workbench);
    }

    @Test
    public void checkIfEditorsForAllContentAssistPreferencesAreDefined() throws Exception {
        final ContentAssistPreferencePage page = new ContentAssistPreferencePage();
        page.createControl(shell);

        final List<FieldEditor> editors = FieldEditorPreferencePageHelper.getEditors(page);
        assertThat(editors).hasSize(8);

        final Map<Class<?>, List<String>> namesGroupedByType = editors.stream()
                .collect(groupingBy(FieldEditor::getClass, mapping(FieldEditor::getPreferenceName, toList())));
        assertThat(namesGroupedByType).hasEntrySatisfying(BooleanFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.ASSISTANT_AUTO_INSERT_ENABLED,
                        RedPreferences.ASSISTANT_AUTO_ACTIVATION_ENABLED,
                        RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED));
        assertThat(namesGroupedByType).hasEntrySatisfying(IntegerFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.ASSISTANT_AUTO_ACTIVATION_DELAY));
        assertThat(namesGroupedByType).hasEntrySatisfying(StringFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.ASSISTANT_AUTO_ACTIVATION_CHARS));
        assertThat(namesGroupedByType).hasEntrySatisfying(ComboBoxFieldEditor.class,
                names -> assertThat(names).containsOnly(RedPreferences.ASSISTANT_KEYWORD_PREFIX_AUTO_ADDITION,
                        RedPreferences.ASSISTANT_LINKED_ARGUMENTS_MODE, RedPreferences.ASSISTANT_MATCHING_KEYWORD));
    }
}
