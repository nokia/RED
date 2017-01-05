/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.wizards;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.robotframework.ide.eclipse.main.plugin.project.RobotProjectNature;

public class NewRobotProjectWizard extends BasicNewResourceWizard {

    private WizardNewProjectCreationPage mainPage;

	@Override
	public void init(final IWorkbench workbench,
			final IStructuredSelection currentSelection) {
		super.init(workbench, currentSelection);
		setNeedsProgressMonitor(true);
		setWindowTitle("New Robot project");
	}

	@Override
	public void addPages() {
		super.addPages();

        mainPage = new WizardNewProjectCreationPage("New Robot Project");
        mainPage.setWizard(this);
        mainPage.setTitle("Robot Project");
        mainPage.setDescription("Create new Robot project");

        this.addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
        selectAndReveal(createNewProject());

		return true;
	}

    private IResource createNewProject() {
        final IProject newProjectHandle = mainPage.getProjectHandle();

        // get a project descriptor
        URI location = null;
        if (!mainPage.useDefaults()) {
            location = mainPage.getLocationURI();
        }

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
        description.setLocationURI(location);

        final IRunnableWithProgress op = new CreateNewRobotProjectRunnable(newProjectHandle, description);

        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException | InvocationTargetException e) {
            throw new ProjectCreatingException("Problem occurred when trying to create new Robot project.", e);
        }
        return newProjectHandle;
    }

    private static class ProjectCreatingException extends RuntimeException {

        public ProjectCreatingException(final String msg, final Exception cause) {
            super(msg, cause);
        }
    }

    private class CreateNewRobotProjectRunnable implements IRunnableWithProgress {

        private final IProject newProjectHandle;

        private final IProjectDescription description;

        public CreateNewRobotProjectRunnable(final IProject newProjectHandle, final IProjectDescription description) {
            this.newProjectHandle = newProjectHandle;
            this.description = description;
        }

        @Override
        public void run(final IProgressMonitor monitor) throws InvocationTargetException {
            final CreateProjectOperation operation = new CreateProjectOperation(description,
                    "Creating new Robot project");
            try {
                PlatformUI.getWorkbench().getOperationSupport().getOperationHistory()
                        .execute(operation, monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));

                RobotProjectNature.addRobotNature(newProjectHandle, monitor);
			} catch (final ExecutionException | CoreException e) {
                throw new InvocationTargetException(e);
            }
        }
	}
}
