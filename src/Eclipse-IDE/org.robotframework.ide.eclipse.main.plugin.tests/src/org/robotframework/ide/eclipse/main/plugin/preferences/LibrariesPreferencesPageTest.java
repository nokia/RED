/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;

public class LibrariesPreferencesPageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final LibrariesPreferencesPage page = new LibrariesPreferencesPage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void editorsForLibrariesPreferencesAreDefined_byDefault() throws Exception {
        final LibrariesPreferencesPage page = new LibrariesPreferencesPage();
        page.createControl(shellProvider.getShell());

        final List<FieldEditor> editors = FieldEditorPreferencePageHelper.getEditors(page);
        assertThat(editors).hasSize(2);

        final FieldEditor autodiscoveringEditor = editors.get(0);
        assertThat(autodiscoveringEditor).isInstanceOf(BooleanFieldEditor.class);
        assertThat(autodiscoveringEditor.getPreferenceName())
                .isEqualTo(RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED);

        final FieldEditor generatingLibdocEditor = editors.get(1);
        assertThat(generatingLibdocEditor).isInstanceOf(BooleanFieldEditor.class);
        assertThat(generatingLibdocEditor.getPreferenceName())
                .isEqualTo(RedPreferences.PYTHON_LIBRARIES_LIBDOCS_GENERATION_IN_SEPARATE_PROCESS_ENABLED);
    }
}
