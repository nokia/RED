/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.script.ScriptRobotLaunchConfiguration;

public class DefaultLaunchConfigurationPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    @SuppressWarnings("unused")
    private static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.launch.default";

    public DefaultLaunchConfigurationPreferencePage() {
        super(DefaultLaunchConfigurationPreferencePage.GRID);
        setPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE, RedPlugin.PLUGIN_ID));
        setDescription("Configure default robot launch configurations");
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createRobotLaunchConfiguration(parent);
        createRemoteRobotLaunchConfiguration(parent);
        createScriptRobotLaunchConfiguration(parent);
    }

    private void createRobotLaunchConfiguration(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Main tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final StringFieldEditor additionalInterpreterArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS, "Additional interpreter arguments:", group);
        additionalInterpreterArguments.load();
        addField(additionalInterpreterArguments);

        final StringFieldEditor additionalRobotArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS, "Additional Robot Framework arguments:", group);
        additionalRobotArguments.load();
        addField(additionalRobotArguments);

        GridDataFactory.fillDefaults().grab(true, false).applyTo(additionalRobotArguments.getLabelControl(group));
    }

    private void createRemoteRobotLaunchConfiguration(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Remote tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final StringFieldEditor remoteHost = new StringFieldEditor(RedPreferences.LAUNCH_REMOTE_HOST, "Local IP:",
                group);
        remoteHost.load();
        addField(remoteHost);

        final IntegerFieldEditor remotePort = new IntegerFieldEditor(RedPreferences.LAUNCH_REMOTE_PORT, "Local port:",
                group);
        remotePort.setValidRange(1, 65_535);
        remotePort.load();
        addField(remotePort);

        final IntegerFieldEditor remoteTimeout = new IntegerFieldEditor(RedPreferences.LAUNCH_REMOTE_TIMEOUT,
                "Connection timeout [ms]:", group);
        remoteTimeout.setValidRange(1, 3_600_000);
        remoteTimeout.load();
        addField(remoteTimeout);
    }

    private void createScriptRobotLaunchConfiguration(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Script tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final FileFieldEditor scriptPathEditor = new FileFieldEditor(RedPreferences.LAUNCH_SCRIPT_PATH,
                "Executor script path:", group);
        scriptPathEditor.setFileExtensions(ScriptRobotLaunchConfiguration.getSystemDependentScriptExtensions());
        scriptPathEditor.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile());
        scriptPathEditor.load();
        addField(scriptPathEditor);

        final StringFieldEditor additionalScriptArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_SCRIPT_ARGUMENTS, "Additional script arguments:", group);
        additionalScriptArguments.load();
        addField(additionalScriptArguments);

        final StringFieldEditor scriptRunCommand = new StringFieldEditor(RedPreferences.LAUNCH_SCRIPT_RUN_COMMAND,
                "Script run command:", group);
        scriptRunCommand.load();
        addField(scriptRunCommand);

        GridDataFactory.fillDefaults().span(2, 1).applyTo(scriptPathEditor.getLabelControl(group));
    }

}
