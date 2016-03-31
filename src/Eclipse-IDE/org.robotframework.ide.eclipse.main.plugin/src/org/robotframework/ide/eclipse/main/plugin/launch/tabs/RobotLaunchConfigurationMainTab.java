/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
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
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.IntepretersComposite.InterpreterListener;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.RobotLaunchConfigurationValidator.RobotLaunchConfigurationValidationException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.RobotLaunchConfigurationValidator.RobotLaunchConfigurationValidationFatalException;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.SuitesListener;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.TagsComposite.TagsListener;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Optional;

/**
 * @author mmarzec
 *
 */
public class RobotLaunchConfigurationMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private IntepretersComposite interpretersComposite;
    private Text argumentsText;
    private Text interpreterArgumentsText;
    
    private TagsProposalsSupport tagsSupport;

    private Button includeTagsBtn;
    private TagsComposite includedTagsComposite;
    
    private Button excludeTagsBtn;
    private TagsComposite excludedTagsComposite;

    private Text projectText;

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
            interpreterArgumentsText.setText(robotConfig.getInterpeterArguments());

            includeTagsBtn.setSelection(robotConfig.isIncludeTagsEnabled());
            includedTagsComposite.setInput(robotConfig.getIncludedTags());

            excludeTagsBtn.setSelection(robotConfig.isExcludeTagsEnabled());
            excludedTagsComposite.setInput(robotConfig.getExcludedTags());

            projectText.setText(projectName);
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

        robotConfig.setUsingInterpreterFromProject(interpretersComposite.isUsingProjectInterpreter());
        robotConfig.setExecutor(interpretersComposite.getChosenSystemExecutor());
        robotConfig.setProjectName(projectText.getText());
        robotConfig.setExecutorArguments(argumentsText.getText());
        robotConfig.setInterpeterArguments(interpreterArgumentsText.getText());
        
        robotConfig.setSuitePaths(suitesToRunComposite.extractSuitesToRun());
        
        robotConfig.setIsIncludeTagsEnabled(includeTagsBtn.getSelection());
        robotConfig.setIncludedTags(includedTagsComposite.getInput());
        robotConfig.setIsExcludeTagsEnabled(excludeTagsBtn.getSelection());
        robotConfig.setExcludedTags(excludedTagsComposite.getInput());
    }
    
    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);
        tagsSupport.switchTo("", new HashMap<IResource, List<String>>());

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        try {
            suitesToRunComposite.switchTo(robotConfig.getProjectName());
            new RobotLaunchConfigurationValidator().validate(robotConfig);

            return userDoNotWriteNewTagCurrently();
        } catch (final RobotLaunchConfigurationValidationException e) {
            setWarningMessage(e.getMessage());
            return userDoNotWriteNewTagCurrently();
        } catch (final RobotLaunchConfigurationValidationFatalException e) {
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
        return !projectText.getText().isEmpty();
    }

    @Override
    public String getMessage() {
        return "Please select a project";
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

    private void createExecutorGroup(final Composite topControl) {
        final Group executorGroup = new Group(topControl, SWT.NONE);
        executorGroup.setText("Executor");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executorGroup);
        GridLayoutFactory.fillDefaults().spacing(2, 2).margins(0, 3).applyTo(executorGroup);

        interpretersComposite = new IntepretersComposite(executorGroup, new InterpreterListener() {
            @Override
            public void interpreterChanged(final Optional<SuiteExecutor> newExecutor) {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().grab(true, false).applyTo(interpretersComposite);

        interpreterArgumentsText = createArgumentsFields(executorGroup, "Additional Python interpreter arguments:", 5);
        argumentsText = createArgumentsFields(executorGroup, "Additional Robot Framework arguments:", 0);
    }

    private Text createArgumentsFields(final Composite parent, final String label, final int vIndent) {
        final Label lblArgs = new Label(parent, SWT.NONE);
        lblArgs.setText(label);
        GridDataFactory.fillDefaults().grab(true, false).indent(0, vIndent).applyTo(lblArgs);

        final Text argText = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).indent(10, 0).applyTo(argText);
        argText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        return argText;
    }

    private void createTagsGroup(final Composite topControl) {
        final Group executorGroup = new Group(topControl, SWT.NONE);
        executorGroup.setText("Tags");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executorGroup);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(2, 1).applyTo(executorGroup);

        tagsSupport = new TagsProposalsSupport();
        includeTagsBtn = createTagsRadioButton(executorGroup, "Only run tests with these tags:");
        includedTagsComposite = createTagsComposite(executorGroup, tagsSupport);

        excludeTagsBtn = createTagsRadioButton(executorGroup, "Skip tests with these tags:");
        excludedTagsComposite = createTagsComposite(executorGroup, tagsSupport);
    }

    private Button createTagsRadioButton(final Composite parent, final String text) {
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

    private TagsComposite createTagsComposite(final Composite parent,
            final TagsProposalsSupport tagsSupport) {
        final TagsComposite composite = new TagsComposite(parent, tagsSupport, new TagsChangesListener());
        GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).grab(true, true).span(2, 1).applyTo(composite);
        return composite;
    }
    
    private void createProjectGroup(final Composite composite) {
        final Group projectGroup = new Group(composite, SWT.NONE);
        projectGroup.setText("Project");
        GridDataFactory.fillDefaults().applyTo(projectGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(2, 1).applyTo(projectGroup);

        projectText = new Text(projectGroup, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(projectText);
        projectText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        final Button browseProject = new Button(projectGroup, SWT.NONE);
        browseProject.setText("Browse...");
        browseProject.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setTitle("Select project");
                dialog.setMessage("Select the project hosting your test suites:");
                dialog.addFilter(new ViewerFilter() {
                    @Override
                    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                        return element instanceof IProject;
                    }
                });
                dialog.setAllowMultiple(false);
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    final IProject project = (IProject) dialog.getFirstResult();
                    projectText.setText(project.getName());
                    updateLaunchConfigurationDialog();
                }
            }
        });
    }

    private void createSuitesGroup(final Composite composite) {
        final Group projectGroup = new Group(composite, SWT.NONE);
        projectGroup.setText("Test Suite(s)");
        GridDataFactory.fillDefaults().grab(true, true).applyTo(projectGroup);
        GridLayoutFactory.fillDefaults().applyTo(projectGroup);

        suitesToRunComposite = new SuitesToRunComposite(projectGroup, new SuitesListener() {
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
