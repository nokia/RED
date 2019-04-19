/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.rf.ide.core.environment.IRuntimeEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotEnvironments;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsNamesLabelProvider;
import org.robotframework.ide.eclipse.main.plugin.preferences.InstalledRobotsEnvironmentsLabelProvider.InstalledRobotsPathsLabelProvider;
import org.robotframework.red.jface.viewers.ViewerColumnsFactory;
import org.robotframework.red.jface.viewers.ViewersConfigurator;
import org.robotframework.red.jface.wizards.JobWizardPage;
import org.robotframework.red.viewers.ListInputStructuredContentProvider;
import org.robotframework.red.viewers.Selections;

public class NewRedPyDevConfigInterpreterPage extends JobWizardPage {

    private final IProject project;
    private final NewRedPyDevConfigWizardData debuggingSessionSetup;

    private TableViewer redEnvsViewer;

    protected NewRedPyDevConfigInterpreterPage(final IProject project,
            final NewRedPyDevConfigWizardData debuggingSessionSetup) {
        super("Choose interpreter", "Create launch configuration for RED and PyDev debugging session", null);
        this.project = project;
        setDescription("Choose interpreter");

        this.debuggingSessionSetup = debuggingSessionSetup;
    }

    @Override
    protected void create(final Composite parent) {
        setPageComplete(false);

        final Composite gridParent = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(gridParent);
        GridLayoutFactory.fillDefaults().extendedMargins(10, 10, 0, 0).applyTo(gridParent);

        redEnvsViewer = createEnvsViewer(gridParent);
    }

    private TableViewer createEnvsViewer(final Composite parent) {
        final TableViewer viewer = new TableViewer(parent,
                SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        ViewersConfigurator.enableDeselectionPossibility(viewer);
        GridDataFactory.fillDefaults()
                .indent(20, 0)
                .grab(true, true)
                .hint(SWT.DEFAULT, 150)
                .applyTo(viewer.getTable());
        viewer.getTable().setLinesVisible(true);
        viewer.getTable().setHeaderVisible(true);

        final ISelectionChangedListener selectionListener = e -> checkPageCompletion();
        viewer.addSelectionChangedListener(selectionListener);
        
        final IDoubleClickListener dlbClickListener = e -> {
            checkPageCompletion();
            if (isPageComplete()) {
                getContainer().showPage(getWizard().getNextPage(this));
            }
        };
        viewer.addDoubleClickListener(dlbClickListener);

        viewer.getTable().addDisposeListener(e -> {
            viewer.removeSelectionChangedListener(selectionListener);
            viewer.removeDoubleClickListener(dlbClickListener);
        });
        
        ColumnViewerToolTipSupport.enableFor(viewer);

        viewer.setContentProvider(new ListInputStructuredContentProvider());
        ViewerColumnsFactory.newColumn("Name")
                .withWidth(320)
                .labelsProvidedBy(new InstalledRobotsNamesLabelProvider())
                .createFor(viewer);
        ViewerColumnsFactory.newColumn("Path")
                .withWidth(200)
                .shouldShowLastVerticalSeparator(false)
                .shouldGrabAllTheSpaceLeft(true)
                .labelsProvidedBy(new InstalledRobotsPathsLabelProvider())
                .createFor(viewer);
        return viewer;
    }

    private void checkPageCompletion() {
        final IStructuredSelection selection = (IStructuredSelection) redEnvsViewer.getSelection();
        if (selection.isEmpty()) {
            debuggingSessionSetup.setRedEnvironment(null);

            setErrorMessage("No environment is chosen");
            setPageComplete(false);

        } else {
            final IRuntimeEnvironment selectedEnv = Selections.getSingleElement(selection, IRuntimeEnvironment.class);
            debuggingSessionSetup.setRedEnvironment(selectedEnv);

            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    @Override
    public void setVisible(final boolean visible) {
        if (visible) {
            if (debuggingSessionSetup.isInterpreterChosen()) {
                redEnvsViewer.setSelection(new StructuredSelection(debuggingSessionSetup.getRedEnvironment()));
            } else {
                initializeValues();
            }
        }
        super.setVisible(visible);
    }

    private void initializeValues() {
        redEnvsViewer.setInput(new ArrayList<>());
        redEnvsViewer.getTable().setEnabled(false);

        scheduleOperation(Environments.class, new EnvInstallationsLoader(), new EnvInstallationsLoadListener());
    }

    private class EnvInstallationsLoader implements MonitoredJobFunction<Environments> {

        @Override
        public Environments run(final IProgressMonitor monitor) {
            final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
            final List<IRuntimeEnvironment> envs = InstalledRobotEnvironments.getAllRobotInstallation(preferences)
                    .stream()
                    .filter(IRuntimeEnvironment::isValidPythonInstallation)
                    .collect(toList());
            final int index = Optional.ofNullable(project)
                    .map(RedPlugin.getModelManager()::createProject)
                    .map(RobotProject::getRuntimeEnvironment)
                    .map(e -> envs.indexOf(e))
                    .orElse(-1);
            return new Environments(envs, index);
        }
    }

    private class EnvInstallationsLoadListener implements JobFinishListener<Environments> {

        @Override
        public void jobFinished(final Environments result) {
            final Control control = NewRedPyDevConfigInterpreterPage.this.getControl();
            if (control == null || control.isDisposed()) {
                return;
            }
            redEnvsViewer.getTable().setEnabled(true);
            redEnvsViewer.setInput(result.envs);
            final IRuntimeEnvironment selected = result.getSelected();
            redEnvsViewer
                    .setSelection(selected != null ? new StructuredSelection(selected) : StructuredSelection.EMPTY);
            redEnvsViewer.getTable().setFocus();

            checkPageCompletion();
        }
    }

    private static class Environments {

        private final List<IRuntimeEnvironment> envs;

        private final int toSelect;

        public Environments(final List<IRuntimeEnvironment> envs, final int toSelect) {
            this.envs = envs;
            this.toSelect = toSelect;
        }

        private IRuntimeEnvironment getSelected() {
            return 0 <= toSelect && toSelect < envs.size() ? envs.get(toSelect) : null;
        }
    }
}
