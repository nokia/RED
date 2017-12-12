/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.red.jface.dialogs.ScriptExportDialog;

public class DefaultLaunchConfigurationPreferencePage extends RedFieldEditorPreferencePage {

    public DefaultLaunchConfigurationPreferencePage() {
        setDescription("Configure default robot launch configurations");
    }

    @Override
    protected void createFieldEditors() {
        final Composite parent = getFieldEditorParent();

        createRobotLaunchConfigurationPreferences(parent);
        createListenerLaunchConfigurationPreferences(parent);
        createExecutorLaunchConfigurationPreferences(parent);
    }

    private void createRobotLaunchConfigurationPreferences(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Robot tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final StringFieldEditor additionalRobotArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_ROBOT_ARGUMENTS, "Additional Robot Framework arguments:", group);
        additionalRobotArguments.load();
        addField(additionalRobotArguments);

        GridDataFactory.fillDefaults().grab(true, false).applyTo(additionalRobotArguments.getLabelControl(group));
    }

    private void createListenerLaunchConfigurationPreferences(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Listener tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final StringFieldEditor remoteHost = new StringFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_HOST,
                "Server IP:", group);
        remoteHost.setEmptyStringAllowed(false);
        remoteHost.setErrorMessage("Server IP cannot be empty");
        remoteHost.load();
        addField(remoteHost);

        final IntegerFieldEditor remotePort = new IntegerFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_PORT,
                "Server port:", group);
        remotePort.setValidRange(AgentConnectionServer.MIN_CONNECTION_PORT, AgentConnectionServer.MAX_CONNECTION_PORT);
        remotePort.load();
        addField(remotePort);

        final IntegerFieldEditor remoteTimeout = new IntegerFieldEditor(RedPreferences.LAUNCH_AGENT_CONNECTION_TIMEOUT,
                "Server connection timeout [s]:", group);
        remoteTimeout.setValidRange(AgentConnectionServer.MIN_CONNECTION_TIMEOUT,
                AgentConnectionServer.MAX_CONNECTION_TIMEOUT);
        remoteTimeout.load();
        addField(remoteTimeout);

        final Button exportBtn = new Button(group, SWT.PUSH);
        GridDataFactory.swtDefaults().indent(25, 5).applyTo(exportBtn);
        exportBtn.setText("Export Client Script");
        exportBtn.addSelectionListener(
                widgetSelectedAdapter(e -> new ScriptExportDialog(getShell(), "TestRunnerAgent.py").open()));
    }

    private void createExecutorLaunchConfigurationPreferences(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Executor tab");
        GridLayoutFactory.fillDefaults().applyTo(group);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, 10).span(2, 1).applyTo(group);

        final StringFieldEditor additionalInterpreterArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_INTERPRETER_ARGUMENTS, "Additional interpreter arguments:", group);
        additionalInterpreterArguments.load();
        addField(additionalInterpreterArguments);

        final FileFieldEditor scriptPathEditor = new FileFieldEditor(RedPreferences.LAUNCH_EXECUTABLE_FILE_PATH,
                "Executable file:", true, StringFieldEditor.VALIDATE_ON_KEY_STROKE, group);
        scriptPathEditor.setFileExtensions(RobotLaunchConfiguration.getSystemDependentExecutableFileExtensions());
        scriptPathEditor.setFilterPath(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile());
        scriptPathEditor.load();
        addField(scriptPathEditor);

        final StringFieldEditor additionalScriptArguments = new StringFieldEditor(
                RedPreferences.LAUNCH_ADDITIONAL_EXECUTABLE_FILE_ARGUMENTS, "Additional executable file arguments:",
                group);
        additionalScriptArguments.load();
        addField(additionalScriptArguments);

        GridDataFactory.fillDefaults().span(2, 1).applyTo(scriptPathEditor.getLabelControl(group));
    }

}
