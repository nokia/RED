/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;


abstract class RedFieldEditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private final Map<String, FieldEditor> editorsMap = new HashMap<>();

    protected RedFieldEditorPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return RedPlugin.getDefault().getPreferenceStore();
    }

    @Override
    protected void addField(final FieldEditor editor) {
        super.addField(editor);
        editorsMap.put(editor.getPreferenceName(), editor);
    }

    protected final FieldEditor getFieldEditor(final String preferenceName) {
        return editorsMap.get(preferenceName);
    }
}
