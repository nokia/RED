/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.texteditor.contentAssist.RedCompletionBuilder.AcceptanceMode;


public class ContentAssistPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ContentAssistPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RedPlugin.PLUGIN_ID));
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();
        createInsertionGroup(parent);
    }

    private void createInsertionGroup(final Composite parent) {
        final RadioGroupFieldEditor insertionModeEditor = new RadioGroupFieldEditor(
                RedPreferences.ASSISTANT_COMPLETION_MODE, "Proposals insertion", 2, createModes(), parent, true);
        addField(insertionModeEditor);
    }

    private String[][] createModes() {
        return new String[][] { new String[] { "Completion inserts", AcceptanceMode.INSERT.name() },
                new String[] { "Completion overrides", AcceptanceMode.SUBSTITUTE.name() } };
    }

}
