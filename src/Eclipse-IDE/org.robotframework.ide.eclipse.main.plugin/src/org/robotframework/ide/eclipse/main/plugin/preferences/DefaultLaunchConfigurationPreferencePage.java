/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

public class DefaultLaunchConfigurationPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    @SuppressWarnings("unused")
    private static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.launch.default";

    public DefaultLaunchConfigurationPreferencePage() {
        super(DefaultLaunchConfigurationPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RedPlugin.PLUGIN_ID));
        setDescription("Configure default robot launch configuration");
    }

    @Override
    public void init(IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        final Composite buttonsParent = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().applyTo(buttonsParent);
        GridDataFactory.fillDefaults().grab(true, false).indent(10, 3).span(2, 1).applyTo(buttonsParent);

        final StringFieldEditor additionalInterpreterArguments = new StringFieldEditor(
                RedPreferences.ADDITIONAL_INTERPRETER_ARGUMENTS,
                "Additional interpreter arguments:", parent);
        additionalInterpreterArguments.load();
        addField(additionalInterpreterArguments);
        final StringFieldEditor additionalRobotArguments = new StringFieldEditor(
                RedPreferences.ADDITIONAL_ROBOT_ARGUMENTS, "Additional Robot Framework arguments:", parent);
        additionalRobotArguments.load();
        addField(additionalRobotArguments);

    }

}
