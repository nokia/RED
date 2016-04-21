/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.ide.eclipse.main.plugin.project.build.fix.InstallRobotUsingPipFixer;
import org.robotframework.red.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class InstalledRobotsPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.installed";

    private ProgressBar progressBar;
    private CheckboxTableViewer viewer;
    private List<RobotRuntimeEnvironment> installations;
    private Button removeButton;
    private Button installButton;

    private boolean dirty = false;

    private Button addButton;

    public InstalledRobotsPreferencesPage() {
        super("Installed Robot Frameworks");
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return RedPlugin.getDefault().getPreferenceStore();
    }

    @Override
    public void init(final IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected Control createContents(final Composite parent) {
        noDefaultAndApplyButton();
        GridLayoutFactory.fillDefaults().applyTo(parent);

        createDescription(parent);

        final Composite tableParent = createParentForTable(parent);
        createViewer(tableParent);

        addButton = createButton(tableParent, "Add...", createAddListener());
        addButton.setEnabled(false);
        removeButton = createButton(tableParent, "Remove", createRemoveListener());
        removeButton.setEnabled(false);
        installButton = createButton(tableParent, "Install Robot", createInstallListener());
        installButton.setEnabled(false);
        createSpacer(tableParent);

        progressBar = createProgress(parent);

        initializeValues();
        return parent;
    }

    private ProgressBar createProgress(final Composite tableParent) {
        final ProgressBar pb = new ProgressBar(tableParent, SWT.SMOOTH | SWT.INDETERMINATE);
        pb.setToolTipText("Looking for Python interpreters");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(pb);
        return pb;
    }

    private void createDescription(final Composite parent) {
        final Label lbl = new Label(parent, SWT.WRAP);
        lbl.setText("Add or remove Robot frameworks environments (location of Python interpreter with Robot library "
                + "installed, currently " + Joiner.on(", ").join(SuiteExecutor.allExecutorNames())
                + " are supported). The selected environment will be used by project unless it is explicitly "
                + "overridden in project configuration.");
        GridDataFactory.fillDefaults().grab(true, false).hint(500, SWT.DEFAULT).applyTo(lbl);
    }

    private Composite createParentForTable(final Composite parent) {
        final Composite tableParent = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(tableParent);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(tableParent);
        return tableParent;
    }

    private void createViewer(final Composite tableParent) {
        final Table table = new Table(tableParent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL);
        viewer = new CheckboxTableViewer(table);
        GridDataFactory.fillDefaults().grab(true, true).span(1, 5).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        final ISelectionChangedListener selectionListener = new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final RobotRuntimeEnvironment selectedInstalation = getSelectedInstalation();
                removeButton.setEnabled(selectedInstalation != null);
                installButton
                        .setEnabled(selectedInstalation != null && selectedInstalation.isValidPythonInstallation());

                if (selectedInstalation != null && selectedInstalation.hasRobotInstalled()) {
                    installButton.setText("Update Robot");
                } else {
                    installButton.setText("Install Robot");
                }
            }
        };
        final ICheckStateListener checkListener = new ICheckStateListener() {
            @Override
            public void checkStateChanged(final CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    viewer.setCheckedElements(new Object[] { event.getElement() });
                    viewer.refresh();
                }
                dirty = true;
            }
        };
        viewer.addSelectionChangedListener(selectionListener);
        viewer.addCheckStateListener(checkListener);
        viewer.getTable().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewer.removeSelectionChangedListener(selectionListener);
                viewer.removeCheckStateListener(checkListener);
            }
        });
        ColumnViewerToolTipSupport.enableFor(viewer);

        viewer.setContentProvider(new InstalledRobotsContentProvider());
        ViewerColumnsFactory.newColumn("Name").withWidth(300)
            .labelsProvidedBy(new InstalledRobotsNamesLabelProvider(viewer))
            .createFor(viewer);
        ViewerColumnsFactory.newColumn("Path").withWidth(200)
                .labelsProvidedBy(new InstalledRobotsPathsLabelProvider(viewer)).createFor(viewer);
    }

    private Button createButton(final Composite tableParent, final String buttonLabel,
            final SelectionListener selectionListener) {
        final Button button = new Button(tableParent, SWT.PUSH);
        GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(button);
        button.setText(buttonLabel);
        button.addSelectionListener(selectionListener);
        return button;
    }

    private void createSpacer(final Composite tableParent) {
        new Label(tableParent, SWT.NONE);
    }

    private void initializeValues() {
        final QualifiedName key = new QualifiedName(RedPlugin.PLUGIN_ID, "result");
        final Job job = new Job("Looking for python installations") {
            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
                installations = InstalledRobotEnvironments.getAllRobotInstallation(preferences);
                final RobotRuntimeEnvironment active = InstalledRobotEnvironments
                        .getActiveRobotInstallation(preferences);
                setProperty(key, active);
                return Status.OK_STATUS;
            }
        };
        job.addJobChangeListener(new JobChangeAdapter() {
            @Override
            public void done(final IJobChangeEvent event) {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        final RobotRuntimeEnvironment active = (RobotRuntimeEnvironment) event.getJob()
                                .getProperty(key);
                        final Control control = InstalledRobotsPreferencesPage.this.getControl();
                        if (control == null || control.isDisposed()) {
                            return;
                        }
                        viewer.setInput(installations);
                        if (active != null) {
                            viewer.setChecked(active, true);
                            viewer.refresh();
                        }
                        addButton.setEnabled(true);
                        final Composite parent = progressBar.getParent();
                        progressBar.dispose();
                        progressBar = null;
                        parent.layout();
                    }
                });
            }
        });
        job.schedule();
    }

    private RobotRuntimeEnvironment getSelectedInstalation() {
        // multiselection is not possible
        final List<RobotRuntimeEnvironment> elements = Selections.getElements(
                (IStructuredSelection) viewer.getSelection(), RobotRuntimeEnvironment.class);
        return elements.isEmpty() ? null : elements.get(0);
    }

    private SelectionListener createAddListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                final DirectoryDialog dirDialog = new DirectoryDialog(InstalledRobotsPreferencesPage.this.getShell());
                dirDialog.setText("Browse for python installation");
                dirDialog.setMessage("Select location of python with robot framework installed");
                final String path = dirDialog.open();
                if (path != null) {
                    installations.add(RobotRuntimeEnvironment.create(path));

                    dirty = true;
                    viewer.setSelection(StructuredSelection.EMPTY);
                    viewer.refresh();
                }
            }
        };
    }

    private SelectionListener createRemoveListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                final RobotRuntimeEnvironment env = getSelectedInstalation();
                installations.remove(env);

                dirty = true;
                viewer.setSelection(StructuredSelection.EMPTY);
                viewer.refresh();
            }
        };
    }

    private SelectionListener createInstallListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                final RobotRuntimeEnvironment selectedInstalation = getSelectedInstalation();

                InstallRobotUsingPipFixer.updateRobotFramework(getShell(), selectedInstalation);
                dirty = true;

                viewer.setSelection(StructuredSelection.EMPTY);
                viewer.refresh();
            }
        };
    }

    @Override
    public boolean performOk() {
        if (dirty) {
            final Object[] checkedElement = viewer.getCheckedElements();
            final RobotRuntimeEnvironment checkedEnv = checkedElement.length == 0 ? null
                    : (RobotRuntimeEnvironment) checkedElement[0];

            final String activePath = checkedEnv == null ? "" : checkedEnv.getFile().getAbsolutePath();
            final String allPaths = Joiner.on(';').join(
                    Iterables.transform(installations, new Function<RobotRuntimeEnvironment, String>() {
                        @Override
                        public String apply(final RobotRuntimeEnvironment env) {
                            return env.getFile().getAbsolutePath();
                        }
                    }));
            getPreferenceStore().putValue(RedPreferences.ACTIVE_RUNTIME, activePath);
            getPreferenceStore().putValue(RedPreferences.OTHER_RUNTIMES, allPaths);
            
            MessageDialog.openInformation(getShell(), "Rebuild required",
                    "The changes you've made requires full workspace rebuild.");

            rebuildWorkspace();
            return true;
        } else {
            return true;
        }
    }

    private void rebuildWorkspace() {
        try {
            new ProgressMonitorDialog(getShell()).run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException,
                        InterruptedException {
                    for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects(0)) {
                        try {
                            if (project.exists() && project.isOpen()) {
                                project.deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
                                project.build(IncrementalProjectBuilder.FULL_BUILD, null);
                            }
                        } catch (final CoreException e) {
                            MessageDialog.openError(getShell(), "Workspace rebuild",
                                    "Problems occured during workspace build " + e.getMessage());
                        }
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            MessageDialog.openError(getShell(), "Workspace rebuild",
                    "Problems occured during workspace build " + e.getMessage());
        }
    }
}
