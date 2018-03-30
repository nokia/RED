/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;

/**
 * @author bembenek
 */
class LaunchConfigurationExecutorTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private InterpretersComposite interpretersComposite;

    private AdditionalArgumentsComposite interpreterArgumentsComposite;

    private ExecutableFileComposite executableFileComposite;

    private AdditionalArgumentsComposite executableFileArgumentsComposite;

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
            interpreterArgumentsComposite.setInput(robotConfig.getInterpreterArguments());
            executableFileComposite.setInput(robotConfig.getExecutableFilePath());
            executableFileArgumentsComposite.setInput(robotConfig.getExecutableFileArguments());
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
            robotConfig.setInterpreterArguments(interpreterArgumentsComposite.getArguments());
            robotConfig.setExecutableFilePath(executableFileComposite.getSelectedExecutableFilePath());
            robotConfig.setExecutableFileArguments(executableFileArgumentsComposite.getArguments());
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

        interpretersComposite = new InterpretersComposite(group, newExecutor -> updateLaunchConfigurationDialog());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(interpretersComposite);

        final Label interpreterArgumentsDescription = new Label(group, SWT.WRAP);
        interpreterArgumentsDescription.setText("Additional interpreter arguments:");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(interpreterArgumentsDescription);

        interpreterArgumentsComposite = new AdditionalArgumentsComposite(group, e -> updateLaunchConfigurationDialog());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(interpreterArgumentsComposite);
    }

    private void createExecutableFileGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("External script");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().spacing(2, 2).margins(0, 3).applyTo(group);

        final Label executableFileDescription = new Label(group, SWT.WRAP);
        executableFileDescription.setText("Executable file to run Robot Framework tests:");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executableFileDescription);

        executableFileComposite = new ExecutableFileComposite(group, e -> updateLaunchConfigurationDialog());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executableFileComposite);

        final Label executableFileArgumentsDescription = new Label(group, SWT.WRAP);
        executableFileArgumentsDescription.setText(
                "Additional executable file arguments. Python interpreter's and Robot parameters will be added afterwards.");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executableFileArgumentsDescription);

        executableFileArgumentsComposite = new AdditionalArgumentsComposite(group,
                e -> updateLaunchConfigurationDialog());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executableFileArgumentsComposite);
    }

}
