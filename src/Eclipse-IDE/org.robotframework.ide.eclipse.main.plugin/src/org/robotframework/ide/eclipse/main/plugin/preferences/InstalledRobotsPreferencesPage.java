package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.ProcessLineHandler;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.viewers.Selections;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class InstalledRobotsPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

    private CheckboxTableViewer viewer;
    private List<RobotRuntimeEnvironment> installations;
    private Button removeButton;
    private Button installButton;

    private boolean dirty = false;

    public InstalledRobotsPreferencesPage() {
        super("Installed Robot Frameworks");
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return RobotFramework.getDefault().getPreferenceStore();
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
        createButton(tableParent, "Add...", createAddListener());
        removeButton = createButton(tableParent, "Remove", createRemoveListener());
        removeButton.setEnabled(false);
        installButton = createButton(tableParent, "Install Robot", createInstallListener());
        installButton.setEnabled(false);
        createSpacer(tableParent);

        initializeValues();
        return parent;
    }

    private void createDescription(final Composite parent) {
        final Label lbl = new Label(parent, SWT.WRAP);
        lbl.setText("Add or remove Robot frameworks definitions. By default the checked "
                + "framework is addded to the build path of newly created Robot projects.");
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
                    dirty = true;
                }
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
            .resizable(true)
            .labelsProvidedBy(new InstalledRobotsNamesLabelProvider(viewer))
            .createFor(viewer);
        ViewerColumnsFactory.newColumn("Path").withWidth(200).resizable(true)
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
        final IPreferenceStore store = getPreferenceStore();
        installations = InstalledRobotEnvironments.readFromPreferences(store);

        viewer.setInput(installations);
        final RobotRuntimeEnvironment active = InstalledRobotEnvironments.getActiveRobotInstallation(store);
        if (active != null) {
            viewer.setChecked(active, true);
            viewer.refresh();
        }
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

                try {
                    final boolean downloadStableVersion = MessageDialog.openQuestion(getShell(),
                                    "Installing Robot Framework",
                            "Do you want to install/upgrade to current stable version? Choose "
                            + "'No' if you prefer preview version (which may be unstable).");
                    
                    final ProgressMonitorDialog progressDialog = new ProgressMonitorDialog(getShell());
                    progressDialog.getProgressMonitor().setTaskName("Installing Robot Framework");
                    progressDialog.run(true, false, new IRunnableWithProgress() {

                        @Override
                        public void run(final IProgressMonitor monitor) throws InvocationTargetException,
                                InterruptedException {
                            monitor.beginTask("Installing Robot Framework", IProgressMonitor.UNKNOWN);
                            try {
                                final ProcessLineHandler linesHandler = new ProcessLineHandler() {
                                    @Override
                                    public void processLine(final String line) {
                                        // pip is indenting some minor messages
                                        // with spaces, so we
                                        // will only show major ones in progress
                                        if (!line.startsWith(" ")) {
                                            monitor.subTask(line);
                                        }
                                    }
                                };
                                selectedInstalation.installRobotUsingPip(linesHandler, downloadStableVersion);
                                dirty = true;
                            } catch (final RobotEnvironmentException e) {
                                StatusManager.getManager().handle(
                                        new Status(IStatus.ERROR, RobotFramework.PLUGIN_ID, e.getMessage()),
                                        StatusManager.BLOCK);
                            }
                        }
                    });

                } catch (InvocationTargetException | InterruptedException e) {
                    StatusManager.getManager().handle(
                            new Status(IStatus.ERROR, RobotFramework.PLUGIN_ID, e.getMessage()), StatusManager.SHOW);
                }

                viewer.setSelection(StructuredSelection.EMPTY);
                viewer.refresh();
            }
        };
    }

    @Override
    public boolean performOk() {
        if (dirty) {
            final Object[] checkedElement = viewer.getCheckedElements();
            if (checkedElement.length != 1) {
                MessageDialog.openError(getShell(), "Installed frameworks",
                        "Please check framework which will be added to build path of new projects.");
                return false;
            }

            final String activePath = ((RobotRuntimeEnvironment) checkedElement[0]).getFile().getAbsolutePath();
            final String allPaths = Joiner.on(';').join(
                    Iterables.transform(installations, new Function<RobotRuntimeEnvironment, String>() {
                        @Override
                        public String apply(final RobotRuntimeEnvironment env) {
                            return env.getFile().getAbsolutePath();
                        }
                    }));
            getPreferenceStore().putValue(InstalledRobotEnvironments.ACTIVE_RUNTIME, activePath);
            getPreferenceStore().putValue(InstalledRobotEnvironments.OTHER_RUNTIMES, allPaths);
            
            MessageDialog.openInformation(getShell(), "Rebuild required",
                    "The changes you've made requires full workspace rebuild.");

            try {
                new ProgressMonitorDialog(getShell()).run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(final IProgressMonitor monitor) throws InvocationTargetException,
                            InterruptedException {
                        for (final IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects(0)) {
                            try {
                                project.build(IncrementalProjectBuilder.FULL_BUILD, null);
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
            return true;
        } else {
            return true;
        }
    }
}
