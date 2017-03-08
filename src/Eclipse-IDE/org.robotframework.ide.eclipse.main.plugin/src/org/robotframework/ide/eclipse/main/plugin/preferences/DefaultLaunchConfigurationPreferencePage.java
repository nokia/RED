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
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.IRemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.script.ScriptRobotLaunchConfiguration;

public class DefaultLaunchConfigurationPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    private Group remoteGroup;

    private Button enableRemoteBtn;

    private StringFieldEditor remoteHost;

    private IntegerFieldEditor remotePort;

    private IntegerFieldEditor remoteTimeout;

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
        remoteGroup = new Group(parent, SWT.NONE);
        remoteGroup.setText("Remote tab");
        GridLayoutFactory.fillDefaults().applyTo(remoteGroup);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(remoteGroup);

        final BooleanFieldEditor enableRemote = new BooleanFieldEditor(RedPreferences.LAUNCH_REMOTE_ENABLED,
                "Enable remote values", remoteGroup);
        enableRemote.load();
        addField(enableRemote);

        enableRemoteBtn = (Button) enableRemote.getDescriptionControl(remoteGroup);
        GridDataFactory.fillDefaults().indent(5, 10).applyTo(enableRemoteBtn);

        remoteHost = new StringFieldEditor(RedPreferences.LAUNCH_REMOTE_HOST, "Local IP:", remoteGroup);
        remoteHost.setEmptyStringAllowed(false);
        remoteHost.setErrorMessage("Server IP cannot be empty");
        remoteHost.load();
        addField(remoteHost);

        remotePort = new IntegerFieldEditor(RedPreferences.LAUNCH_REMOTE_PORT, "Local port:", remoteGroup);
        remotePort.setValidRange(1, IRemoteRobotLaunchConfiguration.MAX_PORT);
        remotePort.load();
        addField(remotePort);

        remoteTimeout = new IntegerFieldEditor(RedPreferences.LAUNCH_REMOTE_TIMEOUT, "Connection timeout [s]:",
                remoteGroup);
        remoteTimeout.setValidRange(1, IRemoteRobotLaunchConfiguration.MAX_TIMEOUT);
        remoteTimeout.load();
        addField(remoteTimeout);

        enableRemoteBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {

                updateRemoteGroupState();
            }
        });
        enableRemoteBtn.setSelection(RedPlugin.getDefault().getPreferences().isLaunchRemoteEnabled());
        updateRemoteGroupState();
    }

    private void updateRemoteGroupState() {
        final boolean enabled = enableRemoteBtn.getSelection();
        remotePort.setEnabled(enabled, remoteGroup);
        remoteTimeout.setEnabled(enabled, remoteGroup);
        remoteHost.setEnabled(enabled, remoteGroup);
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

        GridDataFactory.fillDefaults().span(2, 1).applyTo(scriptPathEditor.getLabelControl(group));
    }

    @Override
    protected void performDefaults() {
        super.performDefaults();
        updateRemoteGroupState();
    }

}
