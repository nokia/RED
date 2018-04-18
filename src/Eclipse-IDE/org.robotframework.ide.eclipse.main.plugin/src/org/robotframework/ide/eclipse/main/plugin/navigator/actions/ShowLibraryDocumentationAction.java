/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.navigator.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.statushandlers.StatusManager;
import org.rf.ide.core.libraries.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.navigator.RobotProjectDependencies.ErroneousLibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.views.documentation.Documentations;

public class ShowLibraryDocumentationAction extends Action implements IEnablementUpdatingAction {

    private final IWorkbenchPage page;
    private final ISelectionProvider selectionProvider;

    public ShowLibraryDocumentationAction(final IWorkbenchPage page, final ISelectionProvider selectionProvider) {
        super("Show library documentation");

        this.page = page;
        this.selectionProvider = selectionProvider;
    }

    @Override
    public void updateEnablement(final IStructuredSelection selection) {
        setEnabled(selection.size() == 1 && selection.getFirstElement() instanceof LibrarySpecification);
    }

    @Override
    public void run() {
        IProject project = null;
        LibrarySpecification librarySpecification = null;

        // action is only enabled when there is one element selected
        final ITreeSelection selection = (ITreeSelection) selectionProvider.getSelection();
        final TreePath theOnlyPath = selection.getPaths()[0];
        for (int i = 0; i < theOnlyPath.getSegmentCount(); i++) {
            if (theOnlyPath.getSegment(i) instanceof IProject) {
                project = (IProject) theOnlyPath.getSegment(i);

            } else if (RedPlugin.getAdapter(theOnlyPath.getSegment(i), IProject.class) != null) {
                project = RedPlugin.getAdapter(theOnlyPath.getSegment(i), IProject.class);

            } else if (theOnlyPath.getSegment(i) instanceof LibrarySpecification) {
                librarySpecification = (LibrarySpecification) theOnlyPath.getSegment(i);

            }
        }

        if (project != null && librarySpecification != null) {
            if (librarySpecification instanceof ErroneousLibrarySpecification) {
                final String message = String.format("Unable to open documentation for library '%s' from '%s'.",
                        librarySpecification.getName(), librarySpecification.getDescriptor().getPath());
                final Status status = new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, null);
                StatusManager.getManager().handle(status, StatusManager.SHOW);
            } else {
                final RobotProject robotProject = RedPlugin.getModelManager().createProject(project);
                Documentations.showDocForLibrarySpecification(page, robotProject, librarySpecification);
            }
        }
    }
}
