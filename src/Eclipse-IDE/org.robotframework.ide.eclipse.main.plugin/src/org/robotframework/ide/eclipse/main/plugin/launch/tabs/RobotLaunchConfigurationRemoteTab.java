/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.IRemoteRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationsValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;

import com.google.common.base.CaseFormat;

/**
 * @author mmarzec
 */
public class RobotLaunchConfigurationRemoteTab extends AbstractLaunchConfigurationTab
        implements ILaunchConfigurationTab {

    private final boolean isRunMode;

    private ProjectComposite projectComposite;

    private Text hostTxt;

    private Text portTxt;

    private Text timeoutTxt;

    private Text commandLineArgument;

    public RobotLaunchConfigurationRemoteTab(final boolean isRunMode) {
        this.isRunMode = isRunMode;
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        createProjectGroup(composite);
        createServerGroup(composite);
        createClientGroup(composite);

        setControl(composite);
    }

    private void createProjectGroup(final Composite parent) {
        final Group projectGroup = new Group(parent, SWT.NONE);
        projectGroup.setText("Project");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(3, 3).extendedMargins(0, 0, 0, 20).applyTo(projectGroup);

        projectComposite = new ProjectComposite(projectGroup, new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectComposite);
    }

    private void createServerGroup(final Composite parent) {
        final Group serverGroup = new Group(parent, SWT.NONE);
        serverGroup.setText("RED Server");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(serverGroup);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(3, 3).extendedMargins(0, 0, 0, 20).applyTo(serverGroup);

        final Label description = new Label(serverGroup, SWT.WRAP);
        description
                .setText("Setup server which will track execution of Robot tests running on remotely connected client");
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(description);

        final Label hostLbl = new Label(serverGroup, SWT.NONE);
        hostLbl.setText("Local IP:");

        hostTxt = new Text(serverGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(hostTxt);
        hostTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        // spacer to occupy next grid cell
        new Label(serverGroup, SWT.NONE);

        final Label portLbl = new Label(serverGroup, SWT.NONE);
        portLbl.setText("Local port:");

        portTxt = new Text(serverGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(portTxt);
        portTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        // spacer to occupy next grid cell
        new Label(serverGroup, SWT.NONE);

        final Label timeoutLbl = new Label(serverGroup, SWT.NONE);
        timeoutLbl.setText("Connection timeout [ms]:");

        timeoutTxt = new Text(serverGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(timeoutTxt);
        timeoutTxt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void createClientGroup(final Composite parent) {
        final Group clientGroup = new Group(parent, SWT.NONE);
        clientGroup.setText("Remote Client");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(clientGroup);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(clientGroup);

        final Label description = new Label(clientGroup, SWT.WRAP);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(description);
        description.setText("Export script which should be added as listener of tests execution on client machine");

        final Button exportBtn = new Button(clientGroup, SWT.PUSH);
        GridDataFactory.swtDefaults().applyTo(exportBtn);
        exportBtn.setText("Export Client Script");
        exportBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent event) {
                final DirectoryDialog dirDialog = new DirectoryDialog(getShell());
                final String fileName = "TestRunnerAgent.py";
                dirDialog.setMessage("Choose \"" + fileName + "\" export destination.");
                final String dir = dirDialog.open();
                if (dir != null) {
                    final File scriptFile = new File(dir + File.separator + fileName);
                    try {
                        Files.copy(RobotRuntimeEnvironment.class.getResourceAsStream(fileName), scriptFile.toPath(),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (final IOException e) {
                        final String message = "Unable to copy file to " + scriptFile.getAbsolutePath();
                        ErrorDialog.openError(getShell(), "File copy problem", message,
                                new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, e));
                    }
                }
            }
        });

        final Label description2 = new Label(clientGroup, SWT.WRAP);
        GridDataFactory.fillDefaults().indent(0, 15).grab(true, false).applyTo(description2);
        description2.setText("Add following argument to command line when running tests on client side");

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
            final IRemoteRobotLaunchConfiguration robotConfig = LaunchConfigurationsWrappers
                    .remoteLaunchConfiguration(configuration);
            final String hostIp = robotConfig.getRemoteDebugHost();
            final String port = robotConfig.getRemoteDebugPortValue();
            final String timeout = robotConfig.getRemoteDebugTimeoutValue();

            if (robotConfig.isDefiningProjectDirectly()) {
                projectComposite.setInput(robotConfig.getProjectName());
            } else {
                projectComposite.disposeGroup();
            }

            hostTxt.setText(hostIp);
            portTxt.setText(port);
            timeoutTxt.setText(timeout);

            updateCommandLineArguments(hostIp, port);
        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    private void updateCommandLineArguments(final String hostIp, final String portString) {
        final String mode = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, Boolean.toString(!isRunMode));
        commandLineArgument.setText("--listener TestRunnerAgent.py:" + portString + ":" + mode + ":" + hostIp);
    }

    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);

        updateCommandLineArguments(hostTxt.getText(), portTxt.getText());

        try {
            new LaunchConfigurationsValidator()
                    .validate(LaunchConfigurationsWrappers.remoteLaunchConfiguration(configuration));
        } catch (final LaunchConfigurationValidationFatalException e) {
            setErrorMessage(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public boolean canSave() {
        return projectComposite.isDisposed() || !projectComposite.getSelectedProjectName().isEmpty();
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final IRemoteRobotLaunchConfiguration robotConfig = LaunchConfigurationsWrappers
                .remoteLaunchConfiguration(configuration);
        try {
            if (robotConfig.isDefiningProjectDirectly()) {
                robotConfig.setProjectName(projectComposite.getSelectedProjectName());
            }
            robotConfig.setRemoteDebugHostValue(hostTxt.getText().trim());
            robotConfig.setRemoteDebugPortValue(portTxt.getText().trim());
            robotConfig.setRemoteDebugTimeoutValue(timeoutTxt.getText().trim());
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Problem with Launch Configuration",
                    "RED was unable to load the working copy of Launch Configuration.");
        }
    }

    @Override
    public String getName() {
        return "Remote";
    }

    @Override
    public String getMessage() {
        return "Create or edit a configuration to launch server for remotely running Robot Framework tests";
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(RedImages.getRemoteRobotImage());
    }
}
