/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.project.build.BuildLogger;
import org.robotframework.ide.eclipse.main.plugin.project.build.libs.LibrariesBuilder;
import org.robotframework.red.swt.SwtThread;
import org.robotframework.red.viewers.Selections;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

public class ReloadLibraryAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public ReloadLibraryAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Reload");

        setImageDescriptor(RedImages.getRefreshImage());

        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(selection.size() > 0
                && Selections.getElements(selection, LibrarySpecification.class).size() == selection.size());
    }

    @Override
    public void run() {
        final Shell shell = page.getWorkbenchWindow().getShell();
        try {
            new ProgressMonitorDialog(shell).run(true, true, monitor -> rebuildLibraries(monitor));
        } catch (InvocationTargetException | InterruptedException e) {
            StatusManager.getManager()
                    .handle(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID,
                            "Problems occurred during library specification generation:\n", e.getCause()),
                            StatusManager.SHOW);
        }
    }

    private void rebuildLibraries(final IProgressMonitor monitor) {
        final Multimap<IProject, LibrarySpecification> groupedSpecifications = groupSpecificationsByProject();
        monitor.beginTask("Regenerating library specifications", 10);
        new LibrariesBuilder(new BuildLogger()).forceLibrariesRebuild(groupedSpecifications,
                SubMonitor.convert(monitor));
        for (final IProject project : groupedSpecifications.keySet()) {
            final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
            robotProject.clearDirtyLibSpecs(groupedSpecifications.values());
            robotProject.clearConfiguration();
            robotProject.clearKwSources();
        }

        SwtThread.asyncExec(() -> ((TreeViewer) selectionProvider).refresh());
    }

    private Multimap<IProject, LibrarySpecification> groupSpecificationsByProject() {
        final ITreeSelection selection = (ITreeSelection) selectionProvider.getSelection();

        final Multimap<IProject, LibrarySpecification> groupedSpecifications = LinkedHashMultimap.create();
        for (final TreePath path : selection.getPaths()) {
            final Object element = path.getLastSegment();
            if (element instanceof LibrarySpecification) {
                final IProject project = findProject(path);
                groupedSpecifications.put(project, (LibrarySpecification) element);
            }
        }
        return groupedSpecifications;
    }

    private IProject findProject(final TreePath path) {
        for (int i = 0; i < path.getSegmentCount(); i++) {
            if (path.getSegment(i) instanceof IProject) {
                return (IProject) path.getSegment(i);
            }
        }
        return null;
    }
}
