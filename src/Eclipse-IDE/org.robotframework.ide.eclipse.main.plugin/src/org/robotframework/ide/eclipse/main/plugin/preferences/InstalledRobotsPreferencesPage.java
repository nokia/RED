/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.preferences;

import static org.robotframework.red.swt.Listeners.widgetSelectedAdapter;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerColumnsFactory;
import org.eclipse.jface.viewers.ViewersConfigurator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.PythonInstallationDirectory;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.project.build.RobotProblem;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.ListInputStructuredContentProvider;
import org.robotframework.red.viewers.Selections;

public class InstalledRobotsPreferencesPage extends RedPreferencePage {

    public static final String ID = "org.robotframework.ide.eclipse.main.plugin.preferences.installed";

    private List<RobotRuntimeEnvironment> installations;

    private Composite parent;
    private CheckboxTableViewer viewer;
    private ProgressBar progressBar;

    private Button addButton;
    private Button removeButton;
    private Button discoverButton;

    private boolean dirty = false;


    public InstalledRobotsPreferencesPage() {
        super("Installed Robot Frameworks");
    }

    @Override
    protected Control createContents(final Composite parent) {
        this.parent = parent;
        noDefaultAndApplyButton();
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);

        createDescription(parent);

        createViewer(parent);

        addButton = createButton(parent, "Add...", createAddListener());
        addButton.setEnabled(false);
        addButton.setToolTipText("Choose interpreter executable directory");

        removeButton = createButton(parent, "Remove", createRemoveListener());
        removeButton.setEnabled(false);
        removeButton.setToolTipText("Remove selected environments");

        discoverButton = createButton(parent, "Discover", createDiscoverListener());
        discoverButton.setEnabled(false);
        discoverButton.setToolTipText("Search again for Python interpreters");

        createSpacer(parent);

        progressBar = createProgress(parent);

        initializeValues();
        return parent;
    }

    private SelectionListener createDiscoverListener() {
        return widgetSelectedAdapter(e -> {
            disableControls();
            progressBar = createProgress(parent);

            final QualifiedName key = new QualifiedName(RedPlugin.PLUGIN_ID, "result");
            final Job job = new Job("Looking for python installations") {

                @Override
                protected IStatus run(final IProgressMonitor monitor) {
                    addOnlyNonExisting(RobotRuntimeEnvironment.whereArePythonInterpreters());

                    setProperty(key, null);
                    return Status.OK_STATUS;
                }
            };
            job.addJobChangeListener(new EnvironmentFoundJobListener(key));
            job.schedule();
        });
    }

    private boolean addOnlyNonExisting(final Collection<PythonInstallationDirectory> locations) {
        boolean added = false;
        for (final PythonInstallationDirectory directory : locations) {
            boolean contains = false;
            for (final RobotRuntimeEnvironment environment : installations) {
                if (environment.getFile().equals(directory)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                added = true;
                installations.add(RobotRuntimeEnvironment.create(directory, directory.getInterpreter()));
                dirty = true;
            }
        }
        return added;
    }

    private ProgressBar createProgress(final Composite parent) {
        final ProgressBar pb = new ProgressBar(parent, SWT.SMOOTH | SWT.INDETERMINATE);
        pb.setToolTipText("Looking for Python interpreters");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(pb);
        parent.layout();
        return pb;
    }

    private void destroyProgress(final ProgressBar pb) {
        final Composite parent = pb.getParent();
        pb.dispose();
        parent.layout();
    }

    private void createDescription(final Composite parent) {
        final Label lbl = new Label(parent, SWT.WRAP);
        lbl.setText("Add or remove Robot frameworks environments (location of Python interpreter with Robot library "
                + "installed, currently " + String.join(", ", SuiteExecutor.allExecutorNames())
                + " are supported). The selected environment will be used by project unless it is explicitly "
                + "overridden in project configuration.");
        GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(600, SWT.DEFAULT).applyTo(lbl);
    }

    private void createViewer(final Composite tableParent) {
        final Table table = new Table(tableParent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL);
        viewer = new CheckboxTableViewer(table);
        ViewersConfigurator.enableDeselectionPossibility(viewer);
        GridDataFactory.fillDefaults().grab(true, true).span(1, 5).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        final ISelectionChangedListener selectionListener = event -> removeButton
                .setEnabled(getSelectedInstallation() != null);
        final ICheckStateListener checkListener = event -> {
            if (event.getChecked()) {
                viewer.setCheckedElements(new Object[] { event.getElement() });
                viewer.refresh();
            }
            dirty = true;
        };
        viewer.addSelectionChangedListener(selectionListener);
        viewer.addCheckStateListener(checkListener);
        viewer.getTable().addDisposeListener(e -> {
            viewer.removeSelectionChangedListener(selectionListener);
            viewer.removeCheckStateListener(checkListener);
        });
        ColumnViewerToolTipSupport.enableFor(viewer);

        viewer.setContentProvider(new ListInputStructuredContentProvider());
        ViewerColumnsFactory.newColumn("Name")
                .withWidth(300)
                .labelsProvidedBy(new InstalledRobotsNamesLabelProvider(viewer))
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Path")
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new InstalledRobotsPathsLabelProvider(viewer))
                .createFor(viewer);
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

    private void disableControls() {
        viewer.getTable().setEnabled(false);
        addButton.setEnabled(false);
        removeButton.setEnabled(false);
        discoverButton.setEnabled(false);
    }

    private void enableControls() {
        viewer.getTable().setEnabled(true);
        addButton.setEnabled(true);
        removeButton.setEnabled(true);
        discoverButton.setEnabled(true);
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
        job.addJobChangeListener(new EnvironmentFoundJobListener(key));
        job.schedule();
    }

    private RobotRuntimeEnvironment getSelectedInstallation() {
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
                    final List<PythonInstallationDirectory> possibleExecutors = RobotRuntimeEnvironment
                            .possibleInstallationsFor(new File(path));

                    boolean changed = false;
                    if (possibleExecutors.size() > 1) {
                        final List<PythonInstallationDirectory> toAdd = askUsersWhatShouldBeAdded(path,
                                possibleExecutors);
                        changed = addOnlyNonExisting(toAdd);
                    } else {
                        changed = true;
                        installations.add(RobotRuntimeEnvironment.create(path));
                    }
                    if (changed) {
                        dirty = true;
                        viewer.setSelection(StructuredSelection.EMPTY);
                        viewer.refresh();
                    }
                }
            }

            private List<PythonInstallationDirectory> askUsersWhatShouldBeAdded(final String path,
                    final List<PythonInstallationDirectory> possibleExecutors) {
                final List<PythonInstallationDirectory> result = new ArrayList<>();
                final ListSelectionDialog dialog = new ListSelectionDialog(getShell(), possibleExecutors,
                        new ListInputStructuredContentProvider(), new InterpretersExecutablesLabelProvider(),
                        "Select which of following interpreters detected inside '" + path + "' should be added:");
                if (dialog.open() == Window.OK) {
                    for (final Object object : dialog.getResult()) {
                        final PythonInstallationDirectory executor = (PythonInstallationDirectory) object;
                        result.add(executor);
                    }
                }
                return result;
            }
        };
    }

    private SelectionListener createRemoveListener() {
        return widgetSelectedAdapter(e -> {
            final RobotRuntimeEnvironment env = getSelectedInstallation();
            installations.remove(env);

            dirty = true;
            viewer.setSelection(StructuredSelection.EMPTY);
            viewer.refresh();
        });
    }

    @Override
    public boolean performOk() {
        if (dirty) {
            final Object[] checkedElement = viewer.getCheckedElements();
            final RobotRuntimeEnvironment checkedEnv = checkedElement.length == 0 ? null
                    : (RobotRuntimeEnvironment) checkedElement[0];

            final List<String> allPathsList = new ArrayList<>();
            final List<String> allExecsList = new ArrayList<>();

            for (final RobotRuntimeEnvironment installation : installations) {
                allPathsList.add(installation.getFile().getAbsolutePath());
                allExecsList.add(getExecOf(installation));
            }

            final String activePath = checkedEnv == null ? "" : checkedEnv.getFile().getAbsolutePath();
            final String activeExec = checkedEnv == null ? "" : getExecOf(checkedEnv);
            final String allPaths = String.join(";", allPathsList);
            final String allExecs = String.join(";", allExecsList);

            // The execs has to be stored first, because we're listening on ACTIVE_RUNTIMES
            // and OTHER_RUNTIMES changes and inside we need actaul value of corresponding
            // execs preference. This may seem a bit weird to have separated ACTIVE_RUNTIME and
            // ACTIVE_RUNTIME_EXEC pair, but implementing it this way gives us both directions
            // versions compatibility.
            getPreferenceStore().putValue(RedPreferences.ACTIVE_RUNTIME_EXEC, activeExec);
            getPreferenceStore().putValue(RedPreferences.OTHER_RUNTIMES_EXECS, allExecs);

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

    private String getExecOf(final RobotRuntimeEnvironment installation) {
        return installation.getFile() instanceof PythonInstallationDirectory ? installation.getInterpreter().name()
                : "";
    }

    private void rebuildWorkspace() {
        try {
            new ProgressMonitorDialog(getShell()).run(true, true, monitor -> {
                for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects(0)) {
                    try {
                        if (project.exists() && project.isOpen()) {
                            project.deleteMarkers(RobotProblem.TYPE_ID, true, IResource.DEPTH_INFINITE);
                            project.build(IncrementalProjectBuilder.FULL_BUILD, null);
                        }
                    } catch (final CoreException e) {
                        MessageDialog.openError(getShell(), "Workspace rebuild",
                                "Problems occurred during workspace build " + e.getMessage());
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            MessageDialog.openError(getShell(), "Workspace rebuild",
                    "Problems occurred during workspace build " + e.getMessage());
        }
    }

    private final class EnvironmentFoundJobListener extends JobChangeAdapter {

        private final QualifiedName key;

        private EnvironmentFoundJobListener(final QualifiedName key) {
            this.key = key;
        }

        @Override
        public void done(final IJobChangeEvent event) {
            SwtThread.asyncExec(() -> {
                final RobotRuntimeEnvironment active = (RobotRuntimeEnvironment) event.getJob().getProperty(key);
                final Control control = InstalledRobotsPreferencesPage.this.getControl();
                if (control == null || control.isDisposed()) {
                    return;
                }
                enableControls();
                viewer.setInput(installations);
                if (active != null) {
                    viewer.setChecked(active, true);
                    viewer.refresh();
                }
                viewer.setSelection(StructuredSelection.EMPTY);

                destroyProgress(progressBar);
                progressBar = null;
            });
        }
    }

    private static class InterpretersExecutablesLabelProvider extends LabelProvider {

        @Override
        public String getText(final Object element) {
            return ((PythonInstallationDirectory) element).getInterpreter().executableName();
        }
    }
}
