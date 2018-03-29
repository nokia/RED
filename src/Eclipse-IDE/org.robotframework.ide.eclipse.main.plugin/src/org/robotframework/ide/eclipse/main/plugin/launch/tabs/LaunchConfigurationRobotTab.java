/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationTabValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.TagsComposite.TagsListener;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;

/**
 * @author mmarzec
 */
class LaunchConfigurationRobotTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private AdditionalArgumentsComposite robotArgumentsComposite;

    private IncludeExcludeTagsComposite includeExcludeTagsComposite;

    private ProjectComposite projectComposite;

    private SuitesToRunComposite suitesToRunComposite;

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
            projectComposite.setInput(robotConfig.getProjectName());
            suitesToRunComposite.setInput(robotConfig.getProjectName(), robotConfig.getSuitePaths());
            includeExcludeTagsComposite.setInput(robotConfig.isIncludeTagsEnabled(), robotConfig.getIncludedTags(),
                    robotConfig.isExcludeTagsEnabled(), robotConfig.getExcludedTags());
            includeExcludeTagsComposite.switchTo(robotConfig.getProjectName(), robotConfig.collectSuitesToRun());
            robotArgumentsComposite.setInput(robotConfig.getRobotArguments());
        } catch (final CoreException e) {
            includeExcludeTagsComposite.switchTo("", new HashMap<IResource, List<String>>());
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        try {
            robotConfig.setProjectName(projectComposite.getSelectedProjectName());
            robotConfig.setSuitePaths(suitesToRunComposite.extractSuitesToRun());
            robotConfig.setIsIncludeTagsEnabled(includeExcludeTagsComposite.isIncludeTagsEnabled());
            robotConfig.setIncludedTags(includeExcludeTagsComposite.getIncludedTags());
            robotConfig.setIsExcludeTagsEnabled(includeExcludeTagsComposite.isExcludeTagsEnabled());
            robotConfig.setExcludedTags(includeExcludeTagsComposite.getExcludedTags());
            robotConfig.setRobotArguments(robotArgumentsComposite.getArguments());
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Problem with Launch Configuration",
                    "RED was unable to load the working copy of Launch Configuration.");
        }
    }

    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);
        includeExcludeTagsComposite.switchTo("", new HashMap<IResource, List<String>>());

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        try {
            suitesToRunComposite.switchTo(robotConfig.getProjectName());
            new LaunchConfigurationTabValidator().validateRobotTab(robotConfig);

            return includeExcludeTagsComposite.userDoNotWriteNewTagCurrently();
        } catch (final LaunchConfigurationValidationException e) {
            setWarningMessage(e.getMessage());
            return includeExcludeTagsComposite.userDoNotWriteNewTagCurrently();
        } catch (final LaunchConfigurationValidationFatalException e) {
            setErrorMessage(e.getMessage());
            return false;
        } catch (final CoreException e) {
            setErrorMessage(e.getMessage());
            return false;
        } finally {
            try {
                includeExcludeTagsComposite.switchTo(robotConfig.getProjectName(), robotConfig.collectSuitesToRun());
            } catch (final CoreException e) {
                throw new IllegalStateException("Shouldn't happen", e);
            }
        }
    }

    @Override
    public String getName() {
        return "Robot";
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(RedImages.getRobotImage());
    }

    @Override
    public boolean canSave() {
        return !projectComposite.getSelectedProjectName().isEmpty();
    }

    @Override
    public String getMessage() {
        return "Edit Robot Framework parameters for launch configuration";
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        createProjectGroup(composite);
        createSuitesGroup(composite);
        createTagsGroup(composite);
        createArgumentsGroup(composite);

        setControl(composite);
    }

    private void createArgumentsGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Arguments");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().spacing(2, 2).margins(0, 3).applyTo(group);

        final Label robotArgumentsDescription = new Label(group, SWT.WRAP);
        robotArgumentsDescription.setText("Additional Robot Framework arguments:");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(robotArgumentsDescription);

        robotArgumentsComposite = new AdditionalArgumentsComposite(group, e -> updateLaunchConfigurationDialog());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(robotArgumentsComposite);
    }

    private void createTagsGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Tags");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().spacing(2, 2).margins(0, 3).applyTo(group);

        includeExcludeTagsComposite = new IncludeExcludeTagsComposite(group, new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        }, new TagsListener() {

            @Override
            public void newTagIsEdited() {
                updateLaunchConfigurationDialog();
            }

            @Override
            public void tagAdded(final String tag) {
                updateLaunchConfigurationDialog();
            }

            @Override
            public void tagRemoved(final String tag) {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(includeExcludeTagsComposite);
    }

    private void createProjectGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Project");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().spacing(2, 2).margins(0, 3).applyTo(group);

        projectComposite = new ProjectComposite(group, e -> updateLaunchConfigurationDialog());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectComposite);
    }

    private void createSuitesGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Test Suite(s)");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
        GridLayoutFactory.fillDefaults().applyTo(group);

        suitesToRunComposite = new SuitesToRunComposite(group, () -> updateLaunchConfigurationDialog());
        GridDataFactory.fillDefaults().grab(true, true).applyTo(suitesToRunComposite);
    }

}
