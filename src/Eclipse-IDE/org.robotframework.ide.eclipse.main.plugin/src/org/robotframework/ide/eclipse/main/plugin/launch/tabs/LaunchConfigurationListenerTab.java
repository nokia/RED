/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;
import org.robotframework.red.jface.dialogs.ScriptExportDialog;

/**
 * @author mmarzec
 */
class LaunchConfigurationListenerTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private final boolean isAgentTypeButtonSelection;

    private Button useLocalAgentButton;

    private Button useRemoteAgentButton;

    private ProjectComposite projectComposite;

    private Group serverGroup;

    private Text hostTxt;

    private Text portTxt;

    private Text timeoutTxt;

    private Group clientGroup;

    private Text commandLineArgument;

    LaunchConfigurationListenerTab(final boolean isAgentTypeButtonSelection) {
        this.isAgentTypeButtonSelection = isAgentTypeButtonSelection;
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        if (isAgentTypeButtonSelection) {
            createAgentGroup(composite);
        } else {
            createProjectGroup(composite);
        }
        createServerGroup(composite);
        createClientGroup(composite);

        setControl(composite);
    }

    private void createProjectGroup(final Composite parent) {
        final Group projectGroup = new Group(parent, SWT.NONE);
        projectGroup.setText("Project");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).extendedMargins(0, 0, 0, 20).applyTo(projectGroup);

        projectComposite = new ProjectComposite(projectGroup, e -> updateLaunchConfigurationDialog());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectComposite);
    }

    private void createAgentGroup(final Composite parent) {
        final Group agentGroup = new Group(parent, SWT.NONE);
        agentGroup.setText("Test Runner Agent");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(agentGroup);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 5).extendedMargins(0, 0, 0, 20).applyTo(agentGroup);

        final Label agentDescription = new Label(agentGroup, SWT.WRAP);
        agentDescription.setText("Choose listener connection type");
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(agentDescription);

        useLocalAgentButton = createAgentTypeSelectionButton(agentGroup,
                String.format("Use local agent connection (localhost with free port and %ds timeout)",
                        AgentConnectionServer.DEFAULT_CONNECTION_TIMEOUT));

        useRemoteAgentButton = createAgentTypeSelectionButton(agentGroup,
                "Use remote agent connection (custom server parameters)");
    }

    private Button createAgentTypeSelectionButton(final Composite parent, final String text) {
        final Button button = createRadioButton(parent, text);
        GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(button);
        button.addSelectionListener(widgetSelectedAdapter(e -> {
            updateLaunchConfigurationDialog();
            updateRemoteGroupState();
        }));
        return button;
    }

    private void updateRemoteGroupState() {
        if (isAgentTypeButtonSelection) {
            final boolean isRemoteAgent = useRemoteAgentButton.getSelection();
            Arrays.stream(serverGroup.getChildren()).forEach(control -> control.setEnabled(isRemoteAgent));
            Arrays.stream(clientGroup.getChildren()).forEach(control -> control.setEnabled(isRemoteAgent));
        }
    }

    private void createServerGroup(final Composite parent) {
        serverGroup = new Group(parent, SWT.NONE);
        serverGroup.setText("RED Server");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(serverGroup);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(3, 3).extendedMargins(0, 0, 0, 20).applyTo(serverGroup);

        final Label serverDescription = new Label(serverGroup, SWT.WRAP);
        serverDescription
                .setText("Setup server which will track execution of Robot tests running on remotely connected client");
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(serverDescription);

        hostTxt = createLabeledText(serverGroup, "Local IP:");

        portTxt = createLabeledText(serverGroup, "Local port:");

        timeoutTxt = createLabeledText(serverGroup, "Connection timeout [s]:");
    }

    private Text createLabeledText(final Composite parent, final String label) {
        final Label lbl = new Label(parent, SWT.NONE);
        lbl.setText(label);

        final Text txt = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(txt);
        txt.addModifyListener(e -> updateLaunchConfigurationDialog());

        // spacer to occupy next grid cell
        new Label(parent, SWT.NONE);

        return txt;
    }

    private void createClientGroup(final Composite parent) {
        clientGroup = new Group(parent, SWT.NONE);
        clientGroup.setText("Remote Client");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(clientGroup);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(clientGroup);

        final Label exportDescription = new Label(clientGroup, SWT.WRAP);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(exportDescription);
        exportDescription
                .setText("Export script which should be added as listener of tests execution on client machine");

        final Button exportBtn = new Button(clientGroup, SWT.PUSH);
        GridDataFactory.swtDefaults().applyTo(exportBtn);
        exportBtn.setText("Export Client Script");
        exportBtn.addSelectionListener(
                widgetSelectedAdapter(e -> new ScriptExportDialog(getShell(), "TestRunnerAgent.py").open()));

        final Label commandLineDescription = new Label(clientGroup, SWT.WRAP);
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).applyTo(commandLineDescription);
        commandLineDescription.setText("Add following argument to command line when running tests on client side");

        commandLineArgument = new Text(clientGroup, SWT.BORDER | SWT.READ_ONLY);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(commandLineArgument);
    }

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        try {
            LaunchConfigurationsWrappers.robotLaunchConfiguration(configuration).fillDefaults();
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Problem with Launch Configuration",
                    "RED was unable to load the working copy of Launch Configuration.");
        }
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            final IRobotLaunchConfiguration robotConfig = LaunchConfigurationsWrappers
                    .robotLaunchConfiguration(configuration);

            if (isAgentTypeButtonSelection) {
                useLocalAgentButton.setSelection(!robotConfig.isUsingRemoteAgent());
                useRemoteAgentButton.setSelection(robotConfig.isUsingRemoteAgent());
            } else {
                projectComposite.setInput(robotConfig.getProjectName());
            }

            hostTxt.setText(robotConfig.getAgentConnectionHostValue());
            portTxt.setText(robotConfig.getAgentConnectionPortValue());
            timeoutTxt.setText(robotConfig.getAgentConnectionTimeoutValue());

            updateCommandLineArguments();
            updateRemoteGroupState();
        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    private void updateCommandLineArguments() {
        commandLineArgument.setText("--listener TestRunnerAgent.py:" + hostTxt.getText() + ":" + portTxt.getText());
    }

    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);

        updateCommandLineArguments();

        try {
            new LaunchConfigurationTabValidator()
                    .validateListenerTab(LaunchConfigurationsWrappers.robotLaunchConfiguration(configuration));
        } catch (final LaunchConfigurationValidationFatalException e) {
            setErrorMessage(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean canSave() {
        return isAgentTypeButtonSelection || !projectComposite.getSelectedProjectName().isEmpty();
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final IRobotLaunchConfiguration robotConfig = LaunchConfigurationsWrappers
                .robotLaunchConfiguration(configuration);
        try {
            if (isAgentTypeButtonSelection) {
                robotConfig.setUsingRemoteAgent(useRemoteAgentButton.getSelection());
            } else {
                robotConfig.setProjectName(projectComposite.getSelectedProjectName());
            }
            robotConfig.setAgentConnectionHostValue(hostTxt.getText().trim());
            robotConfig.setAgentConnectionPortValue(portTxt.getText().trim());
            robotConfig.setAgentConnectionTimeoutValue(timeoutTxt.getText().trim());
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Problem with Launch Configuration",
                    "RED was unable to load the working copy of Launch Configuration.");
        }
    }

    @Override
    public String getName() {
        return "Listener";
    }

    @Override
    public String getMessage() {
        return "Edit test runner agent listener parameters for launch configuration";
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(RedImages.getRemoteRobotImage());
    }
}
