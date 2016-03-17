/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.SuiteLaunchElement;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Collections2;

/**
 * @author mmarzec
 *
 */
public class RobotLaunchConfigurationMainTab extends AbstractLaunchConfigurationTab implements ILaunchConfigurationTab {

    private Button useProjectExecutorButton;
    private Button useSystemExecutorButton;
    private Combo comboExecutorName;
    private Button checkEnvironmentBtn;
    private Text argumentsText;
    private Text interpreterArgumentsText;
    private Text projectText;
    private SuitesToRunComposite launchElementsTreeViewer;
    
    private Button includeTagsBtn;
    private Button excludeTagsBtn;
    private ScrolledComposite excludedTagsScrolledComposite;
    private ScrolledComposite includedTagsScrolledComposite;
    private Composite excludedTagsComposite;
    private Composite includedTagsComposite;
    private List<String> includedTags = newArrayList();
    private List<String> excludedTags = newArrayList();

    @Override
    public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
        RobotLaunchConfiguration.fillDefaults(configuration);
    }

    @Override
    public void initializeFrom(final ILaunchConfiguration configuration) {
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
            final String projectName = robotConfig.getProjectName();
            
            final boolean usesProjectInterpreter = robotConfig.isUsingInterpreterFromProject();
            useProjectExecutorButton.setSelection(usesProjectInterpreter);
            useSystemExecutorButton.setSelection(!usesProjectInterpreter);
            comboExecutorName.setEnabled(!usesProjectInterpreter);
            checkEnvironmentBtn.setEnabled(!usesProjectInterpreter);

            final List<String> executorNames = getSuiteExecutorNames();
            comboExecutorName.setItems(executorNames.toArray(new String[0]));
            comboExecutorName.select(executorNames.indexOf(robotConfig.getExecutor().name()));

            projectText.setText(projectName);
            argumentsText.setText(robotConfig.getExecutorArguments());
            interpreterArgumentsText.setText(robotConfig.getInterpeterArguments());
            
            TagsProposalsSupport.clearTagProposals();
            launchElementsTreeViewer.initLaunchElements(projectName, robotConfig.getSuitePaths(),
                    robotConfig.getTestCasesNames());

            clearNotSavedTags();
            includeTagsBtn.setSelection(robotConfig.isIncludeTagsEnabled());
            for (String tag : robotConfig.getIncludedTags()) {
                createTag(includedTagsScrolledComposite, includedTagsComposite, includedTags, tag);
            }
            excludeTagsBtn.setSelection(robotConfig.isExcludeTagsEnabled());
            for (String tag : robotConfig.getExcludedTags()) {
                createTag(excludedTagsScrolledComposite, excludedTagsComposite, excludedTags, tag);
            }
        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    private List<String> getSuiteExecutorNames() {
        final EnumSet<SuiteExecutor> executors = EnumSet.allOf(SuiteExecutor.class);
        return newArrayList(Collections2.transform(executors, new Function<SuiteExecutor, String>() {
            @Override
            public String apply(final SuiteExecutor executor) {
                return executor.name();
            }
        }));
    }

    @Override
    public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);
        
        robotConfig.setUsingInterpreterFromProject(useProjectExecutorButton.getSelection());
        if (comboExecutorName.getSelectionIndex() >= 0) {
            robotConfig.setExecutor(SuiteExecutor.fromName(comboExecutorName.getItem(comboExecutorName.getSelectionIndex())));
        }
        robotConfig.setProjectName(projectText.getText());
        robotConfig.setExecutorArguments(argumentsText.getText());
        robotConfig.setInterpeterArguments(interpreterArgumentsText.getText());
        
        robotConfig.setSuitePaths(launchElementsTreeViewer.extractCheckedSuitesPaths());
        robotConfig.setTestCasesNames(launchElementsTreeViewer.extractCheckedTestCasesNames());
        
        robotConfig.setIsIncludeTagsEnabled(includeTagsBtn.getSelection());
        robotConfig.setIncludedTags(includedTags);
        robotConfig.setIsExcludeTagsEnabled(excludeTagsBtn.getSelection());
        robotConfig.setExcludedTags(excludedTags);
    }
    
    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

            final String projectName = robotConfig.getProjectName();
            if (projectName.isEmpty()) {
                setErrorMessage("Invalid project specified");
                return false;
            }
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (!project.exists()) {
                setErrorMessage("Project '" + projectName + "' does not exist in workspace");
                return false;
            }

            final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
            final SuiteExecutor selectedExecutor = SuiteExecutor.fromName(comboExecutorName.getText());
            final SuiteExecutor projectInterpreter = robotProject.getRuntimeEnvironment().getInterpreter();
            if (selectedExecutor != projectInterpreter) {
                setWarningMessage("The selected '" + comboExecutorName.getText() + "' interpreter is different "
                        + "than the interpreter used by '" + projectName + "' (" + projectInterpreter + "). The test "
                        + "will  be launched using " + comboExecutorName.getText()
                        + " interpreter as defined in PATH environment variable");
            }
            final List<String> suitePaths = robotConfig.getSuitePaths();

            if (suitePaths.isEmpty()) {
                setWarningMessage("There are no suites specified");
            }

            final List<String> problematic = newArrayList();
            for (final String path : suitePaths) {
                final IResource member = project.findMember(Path.fromPortableString(path));
                if (member == null) {
                    problematic.add(path);
                }
            }
            if (!problematic.isEmpty()) {
                setErrorMessage("Following suites does not exist: " + Joiner.on(',').join(problematic));
                return false;
            }
            return true;
        } catch (final Exception e) {
            setErrorMessage("Invalid file selected");
            return false;
        }
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
    public void dispose() {
        super.dispose();
    }
    
    @Override
    public void createControl(final Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().margins(3, 3).applyTo(composite);

        createExecutorGroup(composite);
        createTagsGroup(composite);
        createProjectGroup(composite);
        createTestSuitesGroup(composite);

        setControl(composite);
    }

    private void createExecutorGroup(final Composite topControl) {
        final Group executorGroup = new Group(topControl, SWT.NONE);
        executorGroup.setText("Executor");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executorGroup);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(2, 1).applyTo(executorGroup);

        useProjectExecutorButton = new Button(executorGroup, SWT.RADIO);
        useProjectExecutorButton.setText("Use interpreter as defined in project configuration");
        GridDataFactory.fillDefaults().grab(true, false).span(4, 1).applyTo(useProjectExecutorButton);
        useProjectExecutorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                checkEnvironmentBtn.setEnabled(false);
                comboExecutorName.setEnabled(false);
                updateLaunchConfigurationDialog();
            }
        });

        useSystemExecutorButton = new Button(executorGroup, SWT.RADIO);
        useSystemExecutorButton.setText("Use");
        GridDataFactory.fillDefaults().grab(false, false).applyTo(useSystemExecutorButton);
        useSystemExecutorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                checkEnvironmentBtn.setEnabled(true);
                comboExecutorName.setEnabled(true);
                updateLaunchConfigurationDialog();
            }
        });
        
        comboExecutorName = new Combo(executorGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        comboExecutorName.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().applyTo(comboExecutorName);
        final Label systemExecutorLbl = new Label(executorGroup, SWT.NONE);
        systemExecutorLbl.setText("interpreter taken from sytem PATH environment variable");

        checkEnvironmentBtn = new Button(executorGroup, SWT.PUSH);
        checkEnvironmentBtn.setText("Check interpreter");
        GridDataFactory.fillDefaults().grab(false, false).align(SWT.END, SWT.FILL).applyTo(checkEnvironmentBtn);
        checkEnvironmentBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                try {
                    new ProgressMonitorDialog(checkEnvironmentBtn.getShell()).run(false, false,
                            new IRunnableWithProgress() {
                                @Override
                                public void run(final IProgressMonitor monitor)
                                        throws InvocationTargetException, InterruptedException {
                                    final SuiteExecutor executor = SuiteExecutor
                                            .fromName(comboExecutorName.getItem(comboExecutorName.getSelectionIndex()));
                                    final String version = RobotRuntimeEnvironment.getVersion(executor);
                                    if (version == null) {
                                        throw new IllegalStateException(
                                                "The " + executor.name() + " interpreter has no Robot installed");
                                    } else {
                                        MessageDialog.openInformation(getShell(), "Interpreter checked", "The "
                                                + executor.name() + " interpreter has " + version + " installed");
                                    }
                                }
                            });
                } catch (InvocationTargetException | InterruptedException e) {
                    StatusManager.getManager().handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, e.getMessage()),
                            StatusManager.BLOCK);
                }
            }
        });
        
        interpreterArgumentsText = createArgumentsFields(executorGroup, "Additional Python interpreter arguments:", 10);
        argumentsText = createArgumentsFields(executorGroup, "Additional Robot Framework arguments:", 0);
    }
    
    private Text createArgumentsFields(final Composite parent, final String label, final int vIndent) {
        final Label lblArgs = new Label(parent, SWT.NONE);
        lblArgs.setText(label);
        GridDataFactory.fillDefaults().grab(true, false).span(4, 1).indent(0, vIndent).applyTo(lblArgs);

        final Text argText = new Text(parent, SWT.BORDER);
        GridDataFactory.fillDefaults()
                .grab(true, false)
                .span(4, 1)
                .indent(10, 0)
                .hint(450, SWT.DEFAULT)
                .applyTo(argText);
        argText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                scheduleUpdateJob();
            }
        });
        return argText;
    }
    
    private void createTagsGroup(final Composite topControl) {
        final Group executorGroup = new Group(topControl, SWT.NONE);
        executorGroup.setText("Tags");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executorGroup);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(2, 1).applyTo(executorGroup);
        
        includeTagsBtn = new Button(executorGroup, SWT.CHECK);
        includeTagsBtn.setText("Only run tests with these tags:");
        includeTagsBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(includeTagsBtn);
        includedTagsScrolledComposite = new ScrolledComposite(executorGroup, SWT.H_SCROLL);
        includedTagsComposite = new Composite(includedTagsScrolledComposite, SWT.NONE);
        initScrolledComposite(includedTagsScrolledComposite, includedTagsComposite, includedTags);
        
        excludeTagsBtn = new Button(executorGroup, SWT.CHECK);
        excludeTagsBtn.setText("Skip tests with these tags:");
        excludeTagsBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateLaunchConfigurationDialog();
            }
        });
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(excludeTagsBtn);
        excludedTagsScrolledComposite = new ScrolledComposite(executorGroup, SWT.H_SCROLL);
        excludedTagsComposite = new Composite(excludedTagsScrolledComposite, SWT.NONE);
        initScrolledComposite(excludedTagsScrolledComposite, excludedTagsComposite, excludedTags);
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
                    final RobotProject robotProject = RedPlugin.getModelManager().getModel()
                            .createRobotProject(project);
                    final SuiteExecutor interpreter = robotProject.getRuntimeEnvironment().getInterpreter();
                    if (interpreter != null) {
                        final int index = newArrayList(comboExecutorName.getItems()).indexOf(interpreter.name());
                        comboExecutorName.select(index);
                    }
                    projectText.setText(project.getName());
                    TagsProposalsSupport.clearProjectTagProposals();
                    TagsProposalsSupport.setProject(project);
                    updateLaunchConfigurationDialog();
                }
            }
        });
    }

    private void createTestSuitesGroup(final Composite composite) {
        final Group projectGroup = new Group(composite, SWT.NONE);
        projectGroup.setText("Test Suite(s)");
        GridDataFactory.fillDefaults().applyTo(projectGroup);
        GridLayoutFactory.fillDefaults().numColumns(2).margins(2, 1).applyTo(projectGroup);

        launchElementsTreeViewer = new SuitesToRunComposite();
        launchElementsTreeViewer.createCheckboxTreeViewer(projectGroup);
        launchElementsTreeViewer.getViewer().addCheckStateListener(new ICheckStateListener() {
            
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                launchElementsTreeViewer.updateCheckState(event.getElement(), event.getChecked());
                updateLaunchConfigurationDialog();
            }
        });

        final Button browseSuites = new Button(projectGroup, SWT.PUSH);
        browseSuites.setText("Browse...");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(browseSuites);
        browseSuites.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
                        new WorkbenchLabelProvider(), new BaseWorkbenchContentProvider());
                dialog.setAllowMultiple(true);
                dialog.setTitle("Select test suite");
                dialog.setMessage("Select the test suite to execute:");
                dialog.addFilter(new ViewerFilter() {
                    @Override
                    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
                        return element instanceof IResource
                                && ((IResource) element).getProject().getName().equals(projectText.getText());
                    }
                });
                dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
                if (dialog.open() == Window.OK) {
                    for (final Object obj : dialog.getResult()) {
                        final IPath pathToAdd = ((IResource) obj).getProjectRelativePath();
                        final String suiteName = RobotLaunchConfigurationDelegate.createSuiteName(((IResource) obj));
                        launchElementsTreeViewer.addSuiteElement(obj, pathToAdd.toPortableString(), suiteName);
                    }
                    updateLaunchConfigurationDialog();
                }
            }
        });

        final Button removeSuite = new Button(projectGroup, SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(removeSuite);
        removeSuite.setText("Remove");
        removeSuite.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                final List<SuiteLaunchElement> selectedElements = Selections.getElements(
                        (IStructuredSelection) launchElementsTreeViewer.getViewer().getSelection(), SuiteLaunchElement.class);
                if (!selectedElements.isEmpty()) {
                    launchElementsTreeViewer.removeSuiteElements(selectedElements);
                    updateLaunchConfigurationDialog();
                }
            }
        });
        final Button selectAll = new Button(projectGroup, SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).indent(0, 10).applyTo(selectAll);
        selectAll.setText("Select All");
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                launchElementsTreeViewer.setLaunchElementsChecked(true);
                updateLaunchConfigurationDialog();
            }
        });
        final Button deselectAll = new Button(projectGroup, SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(deselectAll);
        deselectAll.setText("Deselect All");
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(final SelectionEvent e) {
                launchElementsTreeViewer.setLaunchElementsChecked(false);
                updateLaunchConfigurationDialog();
            }
        });
    }
    
    private void initScrolledComposite(final ScrolledComposite scrolledComposite, final Composite tagsComposite, final List<String> tags) {
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setAlwaysShowScrollBars(false);
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(0, 24).applyTo(scrolledComposite);
        scrolledComposite.setContent(tagsComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tagsComposite);
        GridLayoutFactory.fillDefaults().numColumns(1).spacing(3, 0).applyTo(tagsComposite);
        createAddTagComposite(scrolledComposite, tagsComposite, tags);
    }
    
    private Composite createAddTagComposite(final ScrolledComposite parent, final Composite tagsComposite, final List<String> tags) {
        final Composite addTagComposite = new Composite(tagsComposite, SWT.NONE);
        GridDataFactory.fillDefaults().applyTo(addTagComposite);
        GridLayoutFactory.fillDefaults().numColumns(2).spacing(1, 0).applyTo(addTagComposite);

        final Text tagNameTxt = new Text(addTagComposite, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(false, false).hint(60, SWT.DEFAULT).applyTo(tagNameTxt);
        TagsProposalsSupport.install(tagNameTxt);
        
        final Button addTagBtn = new Button(addTagComposite, SWT.PUSH);
        addTagBtn.setImage(ImagesManager.getImage(RedImages.getAddImage()));
        addTagBtn.setToolTipText("Add new tag");
        GridDataFactory.fillDefaults().hint(22, 18).applyTo(addTagBtn);
        addTagBtn.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent e) {
                createTag(parent, tagsComposite, tags, tagNameTxt.getText().trim());
                tagNameTxt.setText("");
            }
        });
        
        parent.setMinSize(tagsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return tagsComposite;
    }
    
    private void createTag(final ScrolledComposite scrolledComposite, final Composite tagsComposite, final List<String> tags, final String text) {
        
        if (!text.equals("") && !tags.contains(text)) {
            tags.add(text);
            updateLaunchConfigurationDialog();

            final GridLayout tagsCompositeLayout = (GridLayout) tagsComposite.getLayout();
            tagsCompositeLayout.numColumns = tagsCompositeLayout.numColumns + 1;
            final int childrenLengthBeforeAdding = tagsComposite.getChildren().length;

            final Composite newTag = new Composite(tagsComposite, SWT.BORDER);
            newTag.setBackground(tagsComposite.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
            newTag.setBackgroundMode(SWT.INHERIT_FORCE);
            GridDataFactory.fillDefaults().grab(false, false).applyTo(newTag);
            GridLayoutFactory.fillDefaults().numColumns(2).spacing(1, 0).applyTo(newTag);
            
            final CLabel newTagLabel = new CLabel(newTag, SWT.NONE);
            newTagLabel.setImage(ImagesManager.getImage(RedImages.getTagImage()));
            newTagLabel.setText(text);
            GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 20).applyTo(newTagLabel);
            
            final Button newTagRemoveBtn = new Button(newTag, SWT.PUSH);
            newTagRemoveBtn.setImage(ImagesManager.getImage(RedImages.getRemoveTagImage()));
            newTagRemoveBtn.setToolTipText("Remove tag");
            GridDataFactory.fillDefaults().hint(18, 16).applyTo(newTagRemoveBtn);
            newTagRemoveBtn.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    tags.remove(newTagLabel.getText());
                    newTag.dispose();
                    tagsComposite.layout();
                    updateLaunchConfigurationDialog();
                }
            });
            
            final Control[] children = tagsComposite.getChildren();
            newTag.moveAbove(children[childrenLengthBeforeAdding-1]);

            scrolledComposite.setMinSize(tagsComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            tagsComposite.layout();
            scrolledComposite.layout();
        }
    }
    
    private void clearNotSavedTags() {
        includedTags.clear();
        excludedTags.clear();
        Control[] children = includedTagsComposite.getChildren();
        for (int i = 0; i < children.length-1; i++) {
            children[i].dispose();
        }
        children = excludedTagsComposite.getChildren();
        for (int i = 0; i < children.length-1; i++) {
            children[i].dispose();
        }
    }

}
