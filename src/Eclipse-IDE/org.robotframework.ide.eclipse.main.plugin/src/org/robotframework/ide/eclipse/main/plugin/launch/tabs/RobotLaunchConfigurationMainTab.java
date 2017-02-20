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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.InterpretersComposite.InterpreterListener;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationsValidator.LaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationsValidator.LaunchConfigurationValidationFatalException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.SuitesListener;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.TagsComposite.TagsListener;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;

import com.google.common.base.Optional;

/**
 * @author mmarzec
 */
public class RobotLaunchConfigurationMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private InterpretersComposite interpretersComposite;

    private Text argumentsText;

    private Text interpreterArgumentsText;

    private TagsProposalsSupport tagsSupport;

    private Button includeTagsBtn;

    private TagsComposite includedTagsComposite;

    private Button excludeTagsBtn;

    private TagsComposite excludedTagsComposite;

    private ProjectComposite projectComposite;

    private SuitesToRunComposite suitesToRunComposite;

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        RobotLaunchConfiguration.fillDefaults(configuration);
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            final String projectName = robotConfig.getProjectName();

            interpretersComposite.setInput(robotConfig.isUsingInterpreterFromProject(), robotConfig.getExecutor());
            argumentsText.setText(robotConfig.getExecutorArguments());
            interpreterArgumentsText.setText(robotConfig.getInterpreterArguments());

            includeTagsBtn.setSelection(robotConfig.isIncludeTagsEnabled());
            includedTagsComposite.setInput(robotConfig.getIncludedTags());

            excludeTagsBtn.setSelection(robotConfig.isExcludeTagsEnabled());
            excludedTagsComposite.setInput(robotConfig.getExcludedTags());

            projectComposite.setInput(projectName);
            suitesToRunComposite.initialize(projectName, robotConfig.getSuitePaths());

            tagsSupport.switchTo(projectName, robotConfig.collectSuitesToRun());
        } catch (final CoreException e) {
            tagsSupport.switchTo("", new HashMap<IResource, List<String>>());
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        try {
            robotConfig.setUsingInterpreterFromProject(interpretersComposite.isUsingProjectInterpreter());
            robotConfig.setExecutor(interpretersComposite.getChosenSystemExecutor());
            robotConfig.setProjectName(projectComposite.getSelectedProjectName());
            robotConfig.setExecutorArguments(argumentsText.getText());
            robotConfig.setInterpreterArguments(interpreterArgumentsText.getText());

            robotConfig.setSuitePaths(suitesToRunComposite.extractSuitesToRun());

            robotConfig.setIsIncludeTagsEnabled(includeTagsBtn.getSelection());
            robotConfig.setIncludedTags(includedTagsComposite.getInput());
            robotConfig.setIsExcludeTagsEnabled(excludeTagsBtn.getSelection());
            robotConfig.setExcludedTags(excludedTagsComposite.getInput());
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Problem with Launch Configuration",
                    "RED was unable to load the working copy of Launch Configuration.");
        }
    }

    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);
        tagsSupport.switchTo("", new HashMap<IResource, List<String>>());

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        try {
            suitesToRunComposite.switchTo(robotConfig.getProjectName());
            new LaunchConfigurationsValidator().validate(robotConfig);

            return userDoNotWriteNewTagCurrently();
        } catch (final LaunchConfigurationValidationException e) {
            setWarningMessage(e.getMessage());
            return userDoNotWriteNewTagCurrently();
        } catch (final LaunchConfigurationValidationFatalException e) {
            setErrorMessage(e.getMessage());
            return false;
        } catch (final CoreException e) {
            setErrorMessage(e.getMessage());
            return false;
        } finally {
            try {
                tagsSupport.switchTo(robotConfig.getProjectName(), robotConfig.collectSuitesToRun());
            } catch (final CoreException e) {
                throw new IllegalStateException("Shouldn't happen", e);
            }
        }
    }

    private boolean userDoNotWriteNewTagCurrently() {
        // we don't want to Enter key launch whole configuration when user is editing tags
        return !excludedTagsComposite.userIsFocusingOnNewTab() && !includedTagsComposite.userIsFocusingOnNewTab();
    }

    @Override
    public String getName() {
        return "Main";
    }

    @Override
    public Image getImage() {
        return ImagesManager.getImage(RedImages.getRobotImage());
    }

    @Override
    public boolean canSave() {
        return projectComposite.isDisposedOrFilled();
    }

    @Override
    public String getMessage() {
        return "Create or edit a configuration to launch Robot Framework tests";
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        createExecutorGroup(composite);
        createTagsGroup(composite);
        createProjectGroup(composite);
        createSuitesGroup(composite);

        setControl(composite);
    }

    private void createExecutorGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Executor");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().spacing(2, 2).margins(0, 3).applyTo(group);

        interpretersComposite = new InterpretersComposite(group, new InterpreterListener() {

            @Override
            public void interpreterChanged(final Optional<SuiteExecutor> newExecutor) {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(interpretersComposite);

        interpreterArgumentsText = createLabeledText(group, "Additional interpreter arguments:", 5);
        argumentsText = createLabeledText(group, "Additional Robot Framework arguments:", 0);
    }

    private Text createLabeledText(final Composite parent, final String label, final int vIndent) {
        final Label lbl = new Label(parent, SWT.NONE);
        lbl.setText(label);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, vIndent).applyTo(lbl);

        final Text txt = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).indent(10, 0).applyTo(txt);
        txt.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        return txt;
    }

    private void createTagsGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Tags");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(group);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(2, 1).applyTo(group);

        tagsSupport = new TagsProposalsSupport();
        includeTagsBtn = createCheckBoxButton(group, "Only run tests with these tags:");
        includedTagsComposite = createTagsComposite(group, tagsSupport);

        excludeTagsBtn = createCheckBoxButton(group, "Skip tests with these tags:");
        excludedTagsComposite = createTagsComposite(group, tagsSupport);
    }

    private Button createCheckBoxButton(final Composite parent, final String text) {
        final Button button = new Button(parent, SWT.CHECK);
        GridDataFactory.fillDefaults().indent(5, 3).align(SWT.BEGINNING, SWT.BEGINNING).applyTo(button);
        button.setText(text);
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        return button;
    }

    private TagsComposite createTagsComposite(final Composite parent, final TagsProposalsSupport tagsSupport) {
        final TagsComposite composite = new TagsComposite(parent, tagsSupport, new TagsChangesListener());
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, true).span(2, 1).applyTo(composite);
        return composite;
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
    }

    private void createSuitesGroup(final Composite parent) {
        final Group group = new Group(parent, SWT.NONE);
        group.setText("Test Suite(s)");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(group);
        GridLayoutFactory.fillDefaults().applyTo(group);

        suitesToRunComposite = new SuitesToRunComposite(group, new SuitesListener() {

            @Override
            public void suitesChanged() {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().grab(true, true).applyTo(suitesToRunComposite);
    }

    private class TagsChangesListener implements TagsListener {

        @Override
        public void tagAdded(final String tag) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void tagRemoved(final String tag) {
            updateLaunchConfigurationDialog();
        }

        @Override
        public void newTagIsEdited() {
            updateLaunchConfigurationDialog();
        }
    }
}
