/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;


public class FoldingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public FoldingPreferencePage() {
        super(FieldEditorPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RedPlugin.PLUGIN_ID));
        setDescription("Configure which elements are foldable in Red Source Editor");
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        final Composite buttonsParent = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(buttonsParent);
        GridDataFactory.fillDefaults().grab(true, false).indent(10, 3).span(2, 1).applyTo(buttonsParent);

        addField(new BooleanFieldEditor(RedPreferences.FOLDABLE_SECTIONS, "Sections (tables)", buttonsParent));
        addField(new BooleanFieldEditor(RedPreferences.FOLDABLE_CASES, "Test cases", buttonsParent));
        addField(new BooleanFieldEditor(RedPreferences.FOLDABLE_KEYWORDS, "Keywords", buttonsParent));
        addField(new BooleanFieldEditor(RedPreferences.FOLDABLE_DOCUMENTATION,
                "Documentations settings (of suites, keywords, cases)", buttonsParent));
        
        final IntegerFieldEditor lineLimitEditor = new IntegerFieldEditor(RedPreferences.FOLDING_LINE_LIMIT,
                "Minimum number of lines element must span", parent);
        lineLimitEditor.setValidRange(1, 500);
        addField(lineLimitEditor);
    }
}
