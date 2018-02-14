/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.InterpretersComposite.InterpreterListener;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;

/**
 * @author bembenek
 */
class LaunchConfigurationExecutorTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private InterpretersComposite interpretersComposite;

    private Text interpreterArgumentsText;

    private ExecutableFileComposite executableFileComposite;

    private Text executableFileArgumentsText;

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
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        try {
            interpretersComposite.setInput(robotConfig.isUsingInterpreterFromProject(), robotConfig.getInterpreter());
            interpreterArgumentsText.setText(robotConfig.getInterpreterArguments());
            executableFileComposite.setInput(robotConfig.getExecutableFilePath());
            executableFileArgumentsText.setText(robotConfig.getExecutableFileArguments());
        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        try {
            robotConfig.setUsingInterpreterFromProject(interpretersComposite.isUsingProjectInterpreter());
            robotConfig.setInterpreter(interpretersComposite.getChosenSystemExecutor());
            robotConfig.setInterpreterArguments(interpreterArgumentsText.getText().trim());
            robotConfig.setExecutableFilePath(executableFileComposite.getSelectedExecutableFilePath());
            robotConfig.setExecutableFileArguments(executableFileArgumentsText.getText().trim());
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Problem with Launch Configuration",
                    "RED was unable to load the working copy of Launch Configuration.");
        }
    }

    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        try {
            new LaunchConfigurationTabValidator().validateExecutorTab(robotConfig);
        } catch (final LaunchConfigurationValidationException e) {
            setWarningMessage(e.getMessage());
        } catch (final LaunchConfigurationValidationFatalException e) {
            setErrorMessage(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return "Executor";
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(RedImages.getScriptRobotImage());
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public String getMessage() {
        return "Edit interpreter parameters for launch configuration";
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        createInterpreterGroup(composite);
        createExecutableFileGroup(composite);

        setControl(composite);
    }

    private void createInterpreterGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Interpreter");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().spacing(2, 2).margins(0, 3).extendedMargins(0, 0, 0, 20).applyTo(group);

        interpretersComposite = new InterpretersComposite(group, new InterpreterListener() {

            @Override
            public void interpreterChanged(final Optional<SuiteExecutor> newExecutor) {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(interpretersComposite);

        interpreterArgumentsText = createLabeledText(group, "Additional interpreter arguments:");
    }

    private void createExecutableFileGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("External script");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().spacing(2, 2).margins(0, 3).applyTo(group);

        final Label executableFileDescription = new Label(group, SWT.WRAP);
        executableFileDescription.setText(
                "Executable file to run Robot Framework tests:");
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(executableFileDescription);

        executableFileComposite = new ExecutableFileComposite(group, new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        }, RobotLaunchConfiguration.getSystemDependentExecutableFileExtensions());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executableFileComposite);

        executableFileArgumentsText = createLabeledText(group, "Additional executable file arguments. Python interpreter's and Robot parameters will be added afterwards.");
    }

    private Text createLabeledText(final Composite parent, final String label) {
        final Label lbl = new Label(parent, SWT.NONE);
        lbl.setText(label);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(lbl);

        final Text txt = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txt);
        txt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        return txt;
    }

}
