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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.red.junit.ShellProvider;

public class DebuggerPreferencePageTest {

    @Rule
    public ShellProvider shellProvider = new ShellProvider();

    @Test
    public void initDoesNothing() {
        final IWorkbench workbench = mock(IWorkbench.class);

        final DebuggerPreferencePage page = new DebuggerPreferencePage();
        page.init(workbench);

        verifyZeroInteractions(workbench);
    }

    @Test
    public void checkIfAllBooleanEditorsAreDefined() throws Exception {
        final DebuggerPreferencePage page = new DebuggerPreferencePage();
        page.createControl(shellProvider.getShell());

        final List<String> booleanPrefNames = newArrayList(RedPreferences.DEBUGGER_OMIT_LIB_KEYWORDS);
        final List<String> radioPrefNames = newArrayList(RedPreferences.DEBUGGER_SUSPEND_ON_ERROR);

        final List<FieldEditor> editors = FieldEditorPreferencePageHelper.getEditors(page);
        assertThat(editors).hasSize(2);
        for (final FieldEditor editor : editors) {
            if (editor instanceof BooleanFieldEditor) {
                booleanPrefNames.remove(editor.getPreferenceName());
            } else if (editor instanceof RadioGroupFieldEditor) {
                radioPrefNames.remove(editor.getPreferenceName());
            }
        }
        assertThat(booleanPrefNames).isEmpty();
        assertThat(radioPrefNames).isEmpty();
    }
}
