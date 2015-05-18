package org.robotframework.ide.eclipse.main.plugin.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;
import org.robotframework.ide.core.executor.RobotRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RobotFramework;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.project.BuildpathFile;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectMetadata;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;


public class RobotProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

    private static final String PREFERENCES_LINK = "Preferences";

    private Button usePreferencesButton;

    private CheckboxTableViewer viewer;

    private RobotProjectMetadata metadata;

    @Override
    protected Control createContents(final Composite parent) {
        noDefaultAndApplyButton();
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(parent);

        final IProject project = (IProject) getElement();
        if (RobotProjectNature.hasRobotNature(project)) {
            usePreferencesButton = new Button(parent, SWT.CHECK);
            usePreferencesButton.addSelectionListener(createUseActiveCheckListener());

            final Link link = new Link(parent, SWT.NONE);
            link.setText("Use active execution environment as defined in <a>" + PREFERENCES_LINK + "</a>");
            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    if (PREFERENCES_LINK.equals(e.text)) {
                        PreferencesUtil.createPreferenceDialogOn(getShell(), InstalledRobotsPreferencesPage.ID,
                                new String[] { InstalledRobotsPreferencesPage.ID }, null).open();
                        setInput(project);
                    }
                }
            });

            final Label label = new Label(parent, SWT.WRAP);
            label.setText("Choose project-specific execution environment:");
            GridDataFactory.fillDefaults().span(2, 1).grab(true, false).hint(200, SWT.DEFAULT).applyTo(label);

            createViewer(parent);

            final Button button = createConfigurationButton(parent, project, true);
            GridDataFactory.fillDefaults().span(2, 1).grab(true, false).align(SWT.END, SWT.CENTER).applyTo(button);

            setInput(project);
        } else {
            final Label label = new Label(parent, SWT.WRAP);
            label.setText("Currently this project is not configured as Robot project. "
                    + "Use 'Add Robot nature' button in order to configure it properly.");
            GridDataFactory.fillDefaults().grab(true, false).hint(200, SWT.DEFAULT).applyTo(label);

            final Composite comp = new Composite(parent, SWT.NONE);
            GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(comp);

            final Button button = createConfigurationButton(parent, project, false);
            GridDataFactory.fillDefaults().span(2, 1).grab(true, false).align(SWT.END, SWT.CENTER).applyTo(button);
        }
        return parent;
    }

    private Button createConfigurationButton(final Composite parent, final IProject project, final boolean remove) {
        final String text = remove ? "Remove Robot nature" : "Add Robot nature";
        final String tooltip = remove ? "This project has Robot nature. Do you want to remove it?"
                : "This project is not a Robot Project. Do you want to add Robot nature to it?";

        final Button convertButton = new Button(parent, SWT.NONE);
        convertButton.setText(text);
        convertButton.setToolTipText(tooltip);
        convertButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                try {
                    if (remove) {
                        RobotProjectNature.removeRobotNature(project, new NullProgressMonitor());
                    } else {
                        RobotProjectNature.addRobotNature(project, new NullProgressMonitor());
                    }
                    getShell().close();
                } catch (final CoreException e) {
                    MessageDialog.openError(getShell(), "Conversion problem", "Unable to convert project. Reason: "
                            + e.getMessage());
                }
            }
        });
        return convertButton;
    }

    private void createViewer(final Composite tableParent) {
        final Table table = new Table(tableParent, SWT.CHECK | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL
                | SWT.V_SCROLL);
        viewer = new CheckboxTableViewer(table);
        GridDataFactory.fillDefaults().span(2, 1).grab(true, true).applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);
        final ICheckStateListener checkListener = new ICheckStateListener() {
            @Override
            public void checkStateChanged(final CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    viewer.setCheckedElements(new Object[] { event.getElement() });
                    viewer.refresh();
                }
            }
        };
        viewer.addCheckStateListener(checkListener);
        viewer.getTable().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(final DisposeEvent e) {
                viewer.removeCheckStateListener(checkListener);
            }
        });
        ColumnViewerToolTipSupport.enableFor(viewer);

        viewer.setContentProvider(new InstalledRobotsContentProvider());
        ViewerColumnsFactory.newColumn("Name").withWidth(300)
                .labelsProvidedBy(new InstalledRobotsNamesLabelProvider(viewer)).createFor(viewer);
        ViewerColumnsFactory.newColumn("Path").withWidth(200)
                .labelsProvidedBy(new InstalledRobotsPathsLabelProvider(viewer)).createFor(viewer);
    }

    private SelectionListener createUseActiveCheckListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                viewer.getTable().setEnabled(!usePreferencesButton.getSelection());
                setViewerInput(usePreferencesButton.getSelection());
            }
        };
    }

    private void setInput(final IProject project) {
        metadata = new BuildpathFile(project).read();
        if (metadata == null) {
            final String msg = "The robot build paths file does not exist. Please rebuild your project";
            MessageDialog.openError(getShell(), "Invalid project state", msg);
            throw new IllegalStateException(msg);
        }

        final boolean shouldUseActiveFromPreferences = metadata.getPythonLocation() == null;
        usePreferencesButton.setSelection(shouldUseActiveFromPreferences);

        setViewerInput(shouldUseActiveFromPreferences);
    }

    private void setViewerInput(final boolean shouldUseActiveFromPreferences) {
        viewer.getTable().setEnabled(!shouldUseActiveFromPreferences);
        viewer.setInput(RobotFramework.getDefault().getAllRuntimeEnvironments());
        if (!shouldUseActiveFromPreferences) {
            viewer.setChecked(RobotRuntimeEnvironment.create(metadata.getPythonLocation()), true);
        } else {
            viewer.setChecked(RobotFramework.getDefault().getActiveRobotInstallation(), true);
        }
        viewer.refresh();
    }

    @Override
    public boolean performOk() {
        final RobotProjectMetadata newMetadata = createNewMetadata();
        if (newMetadata == null) {
            MessageDialog.openError(getShell(), "Select environment",
                    "At least one execution environment has to be selected!");
            return false;
        }
        if (!newMetadata.equals(metadata)) {
            MessageDialog.openInformation(getShell(), "Rebuild required",
                    "The changes you've made requires project rebuild.");

            final IProject project = (IProject) getElement();
            try {
                new BuildpathFile(project).write(newMetadata);

                rebuildProject(project);
            } catch (final CoreException e) {
                MessageDialog.openError(getShell(), "File creating problem", "Unable to create build path file!");
            }
        }
        return true;
    }

    private RobotProjectMetadata createNewMetadata() {
        if (usePreferencesButton.getSelection()) {
            final RobotRuntimeEnvironment activeInstallation = RobotFramework.getDefault().getActiveRobotInstallation();
            return RobotProjectMetadata.create(null, activeInstallation.getVersion(),
                    activeInstallation.getStandardLibrariesNames());
        } else {
            final Object[] selectedEnvs = viewer.getCheckedElements();
            if (selectedEnvs.length == 1) {
                final RobotRuntimeEnvironment selectedEnv = (RobotRuntimeEnvironment) selectedEnvs[0];
                return RobotProjectMetadata.create(selectedEnv.getFile(), selectedEnv.getVersion(),
                        selectedEnv.getStandardLibrariesNames());
            } else {
                return null;
            }
        }
    }

    private void rebuildProject(final IProject project) {
        try {
            new ProgressMonitorDialog(getShell()).run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    try {
                        project.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
                        project.build(IncrementalProjectBuilder.FULL_BUILD, null);
                    } catch (final CoreException e) {
                        MessageDialog.openError(getShell(), "Workspace rebuild",
                                "Problems occured during workspace build " + e.getMessage());
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            MessageDialog.openError(getShell(), "Workspace rebuild",
                    "Problems occured during workspace build " + e.getMessage());
        }
    }
}