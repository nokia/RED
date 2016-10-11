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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class AutoDiscoveringPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.autodiscovering";

    public AutoDiscoveringPreferencePage() {
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
        final Group libGroup = new Group(parent, SWT.NONE);
        libGroup.setText("Libraries Autodiscovering");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(libGroup);
        GridLayoutFactory.fillDefaults().applyTo(libGroup);

        final BooleanFieldEditor projectModulesAdditionOnVirtualenvEditor = new BooleanFieldEditor(
                RedPreferences.PROJECT_MODULES_RECURSIVE_ADDITION_ON_VIRTUALENV_ENABLED,
                "Add project modules recursively to PYTHONPATH/CLASSPATH during Autodiscovering on virtualenv",
                libGroup);
        addField(projectModulesAdditionOnVirtualenvEditor);
        final Button button = (Button) projectModulesAdditionOnVirtualenvEditor.getDescriptionControl(libGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(button);
    }

}
