/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;

import com.google.common.base.Function;

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

        assertThat(transform(editors, preferenceName())).containsOnly(
                RedPreferences.FILE_ELEMENTS_OPEN_MODE,
                RedPreferences.MINIMAL_NUMBER_OF_ARGUMENT_COLUMNS,
                RedPreferences.BEHAVIOR_ON_CELL_COMMIT,
                RedPreferences.SEPARATOR_MODE,
                RedPreferences.SEPARATOR_TO_USE);
    }

    private static Function<FieldEditor, String> preferenceName() {
        return new Function<FieldEditor, String>() {

            @Override
            public String apply(final FieldEditor editor) {
                return editor.getPreferenceName();
            }
        };
    }
}
