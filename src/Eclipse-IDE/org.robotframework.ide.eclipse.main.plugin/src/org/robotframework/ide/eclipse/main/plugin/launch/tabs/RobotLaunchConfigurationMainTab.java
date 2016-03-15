/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.tabs;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.SuitesToRunComposite.SuitesListener;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.TagsComposite.TagsListener;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModelManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
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
    
    private Button includeTagsBtn;
    private Button excludeTagsBtn;
    private TagsComposite includedTagsComposite;
    private TagsComposite excludedTagsComposite;
    private final Set<String> includedTags = newLinkedHashSet();
    private final Set<String> excludedTags = newLinkedHashSet();

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

            final boolean usesProjectInterpreter = robotConfig.isUsingInterpreterFromProject();
            useProjectExecutorButton.setSelection(usesProjectInterpreter);
            useSystemExecutorButton.setSelection(!usesProjectInterpreter);
            comboExecutorName.setEnabled(!usesProjectInterpreter);
            checkEnvironmentBtn.setEnabled(!usesProjectInterpreter);

            final List<String> executorNames = getSuiteExecutorNames();
            comboExecutorName.setItems(executorNames.toArray(new String[0]));
            comboExecutorName.select(executorNames.indexOf(robotConfig.getExecutor().name()));
            argumentsText.setText(robotConfig.getExecutorArguments());
            interpreterArgumentsText.setText(robotConfig.getInterpeterArguments());

            final Map<IResource, List<String>> suitesToRun = collectSuitesToRun(robotConfig);
            includeTagsBtn.setSelection(robotConfig.isIncludeTagsEnabled());
            includedTags.clear();
            includedTags.addAll(robotConfig.getIncludedTags());
            includedTagsComposite.setInput(includedTags);
            includedTagsComposite.installTagsProposalsSupport(suitesToRun);

            excludeTagsBtn.setSelection(robotConfig.isExcludeTagsEnabled());
            excludedTags.clear();
            excludedTags.addAll(robotConfig.getExcludedTags());
            excludedTagsComposite.setInput(excludedTags);
            excludedTagsComposite.installTagsProposalsSupport(suitesToRun);

            projectText.setText(projectName);
            suitesToRunComposite.initialize(projectName, suitesToRun);

        } catch (final CoreException e) {
            setErrorMessage("Invalid launch configuration: " + e.getMessage());
        }
    }

    private Map<IResource, List<String>> collectSuitesToRun(final RobotLaunchConfiguration robotConfig)
            throws CoreException {
        final IProject project = robotConfig.getRobotProject().getProject();
        
        final Map<IResource, List<String>> suitesToRun = new HashMap<>();

        final Map<String, List<String>> suitePaths = robotConfig.getSuitePaths();
        for (final Entry<String, List<String>> entry : suitePaths.entrySet()) {
            final IPath path = Path.fromPortableString(entry.getKey());
            final IResource resource = path.getFileExtension() == null ? project.getFolder(path)
                    : project.getFile(path);
            suitesToRun.put(resource, entry.getValue());
        }
        return suitesToRun;
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
        
        robotConfig.setSuitePaths(suitesToRunComposite.extractSuitesToRun());
        
        robotConfig.setIsIncludeTagsEnabled(includeTagsBtn.getSelection());
        robotConfig.setIncludedTags(newArrayList(includedTags));
        robotConfig.setIsExcludeTagsEnabled(excludeTagsBtn.getSelection());
        robotConfig.setExcludedTags(newArrayList(excludedTags));
    }
    
    @Override
    public boolean isValid(final ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setWarningMessage(null);
        try {
            final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

            final String projectName = robotConfig.getProjectName();
            suitesToRunComposite.switchTo(projectName);
            if (projectName.isEmpty()) {
                setErrorMessage("Project '' does not exist in workspace");
                return false;
            }
            final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (!project.exists()) {
                setErrorMessage("Project '" + projectName + "' does not exist in workspace");
                return false;
            }
            if (!project.isOpen()) {
                setErrorMessage("Project '" + projectName + "' is currently closed");
                return false;
            }

            final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
            final RobotRuntimeEnvironment env = robotProject.getRuntimeEnvironment();

            if (env == null || !env.isValidPythonInstallation() || !env.hasRobotInstalled()) {
                final String additional = env.hasRobotInstalled() ? "" : " (missing Robot Framework)";
                setErrorMessage("Project '" + projectName + "' is using invalid Python environment" + additional);
                return false;
            }
            
            final Map<IResource, List<String>> suitesToRun = collectSuitesToRun(robotConfig);
            if (suitesToRun.isEmpty()) {
                setWarningMessage("There are no suites specified");
            }

            includedTagsComposite.installTagsProposalsSupport(suitesToRun);
            excludedTagsComposite.installTagsProposalsSupport(suitesToRun);

            final List<String> problematicSuites = new ArrayList<>();
            final List<String> problematicTests = new ArrayList<>();
            for (final IResource resource : suitesToRun.keySet()) {
                if (!resource.exists()) {
                    problematicSuites.add(resource.getFullPath().toString());
                } else if (resource.getType() == IResource.FILE) {
                    final RobotSuiteFile suiteModel = RobotModelManager.getInstance().createSuiteFile((IFile) resource);
                    final List<RobotCase> cases = new ArrayList<>();
                    final Optional<RobotCasesSection> section = suiteModel.findSection(RobotCasesSection.class);
                    if (section.isPresent()) {
                        cases.addAll(section.get().getChildren());
                    }

                    for (final String caseName : suitesToRun.get(resource)) {
                        boolean exist = false;
                        for (final RobotCase test : cases) {
                            if (test.getName().equalsIgnoreCase(caseName)) {
                                exist = true;
                                break;
                            }
                        }
                        if (!exist) {
                            problematicTests.add(caseName);
                        }
                    }
                }
            }
            if (!problematicSuites.isEmpty()) {
                setErrorMessage("Following suites does not exist: " + Joiner.on(", ").join(problematicSuites));
                return false;
            }
            if (!problematicTests.isEmpty()) {
                setErrorMessage("Following tests does not exist: " + Joiner.on(", ").join(problematicTests));
                return false;
            }

            // we don't want to Enter key launch whole configuration when user is editing tags
            return !excludedTagsComposite.userIsFocusingOnNewTab() && !includedTagsComposite.userIsFocusingOnNewTab();
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
        createSuitesGroup(composite);

        setControl(composite);
    }

    private void createExecutorGroup(final Composite topControl) {
        final Group executorGroup = new Group(topControl, SWT.NONE);
        executorGroup.setText("Executor");
        GridDataFactory.fillDefaults().grab(true, false).applyTo(executorGroup);
        GridLayoutFactory.fillDefaults().numColumns(4).spacing(2, 2).margins(5, 5).applyTo(executorGroup);

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
                scheduleUpdateJob();
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

        createTagsToIncludeControls(executorGroup);
        createTagsToExcludeControls(executorGroup);
    }

    private void createTagsToIncludeControls(final Group executorGroup) {
        includeTagsBtn = createTagsRadioButton(executorGroup, "Only run tests with these tags:");

        includedTagsComposite = new TagsComposite(executorGroup);
        GridDataFactory.fillDefaults()
                .hint(200, SWT.DEFAULT)
                .grab(true, true)
                .span(2, 1)
                .applyTo(includedTagsComposite);
        includedTagsComposite.addTagsListener(new TagsListener() {
            @Override
            public void addTagRequested(final String tag) {
                includedTags.add(tag);
                includedTagsComposite.setInput(includedTags);
                updateLaunchConfigurationDialog();
            }

            @Override
            public void removeTagRequested(final String tag) {
                includedTags.remove(tag);
                includedTagsComposite.setInput(includedTags);
                updateLaunchConfigurationDialog();
            }

            @Override
            public void newTagIsEdited() {
                scheduleUpdateJob();
            }
        });
    }

    private void createTagsToExcludeControls(final Group executorGroup) {
        excludeTagsBtn = createTagsRadioButton(executorGroup, "Skip tests with these tags:");

        excludedTagsComposite = new TagsComposite(executorGroup);
        GridDataFactory.fillDefaults()
                .hint(200, SWT.DEFAULT)
                .grab(true, true)
                .span(2, 1)
                .applyTo(excludedTagsComposite);
        excludedTagsComposite.addTagsListener(new TagsListener() {
            @Override
            public void addTagRequested(final String tag) {
                excludedTags.add(tag);
                excludedTagsComposite.setInput(excludedTags);
                updateLaunchConfigurationDialog();
            }

            @Override
            public void removeTagRequested(final String tag) {
                excludedTags.remove(tag);
                excludedTagsComposite.setInput(excludedTags);
                updateLaunchConfigurationDialog();
            }

            @Override
            public void newTagIsEdited() {
                scheduleUpdateJob();
            }
        });
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
                includedTagsComposite.markProposalsToRebuild();
                excludedTagsComposite.markProposalsToRebuild();
                scheduleUpdateJob();
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

                    includedTagsComposite.markProposalsToRebuild();
                    excludedTagsComposite.markProposalsToRebuild();
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

        suitesToRunComposite = new SuitesToRunComposite(projectGroup);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(suitesToRunComposite);
        suitesToRunComposite.addSuitesListener(new SuitesListener() {
            @Override
            public void suitesChanged() {
                includedTagsComposite.markProposalsToRebuild();
                excludedTagsComposite.markProposalsToRebuild();
                updateLaunchConfigurationDialog();
            }
        });
    }
}
