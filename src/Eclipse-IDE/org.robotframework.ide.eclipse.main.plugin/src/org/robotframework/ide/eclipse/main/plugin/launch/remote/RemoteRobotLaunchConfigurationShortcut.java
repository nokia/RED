/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCase;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.red.jface.dialogs.DetailedErrorDialog;
import org.robotframework.red.viewers.Selections;

public class RemoteRobotLaunchConfigurationShortcut implements ILaunchShortcut2 {

    @Override
    public void launch(final ISelection selection, final String mode) {
        if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
            final IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            final Optional<IProject> project = getProjectFromSelection(structuredSelection);
            if (project.isPresent()) {
                createAndLaunchConfiguration(project.get(), mode);
            } else {
                DetailedErrorDialog.openErrorDialog("Cannot generate Robot Launch Configuration",
                        "All selected elements need to be members of only one project.");
            }
        }
    }

    private void createAndLaunchConfiguration(final IProject project, final String mode) {
        try {
            final ILaunchConfigurationWorkingCopy defaultConfig = RemoteRobotLaunchConfiguration
                    .prepareDefault(project);
            final ILaunchConfigurationWorkingCopy sameConfig = RemoteRobotLaunchConfigurationFinder
                    .findSameAs(defaultConfig);
            if (sameConfig != null) {
                doLaunchConfiguration(sameConfig, mode);
            } else {
                doLaunchConfiguration(defaultConfig, mode);
            }
        } catch (final CoreException e) {
            DetailedErrorDialog.openErrorDialog("Cannot generate Remote Robot Launch Configuration",
                    "RED was unable to create Remote Robot Launch Configuration from selection.");
        }
    }

    private void doLaunchConfiguration(final ILaunchConfiguration config, final String mode) {
        final WorkspaceJob job = new WorkspaceJob("Launching Remote Robot Tests") {

            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException {
                config.launch(mode, monitor);

                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();
    }

    public static Optional<IProject> getProjectFromSelection(final IStructuredSelection selection) {
        final List<IResource> resources = Selections.getAdaptableElements(selection, IResource.class);
        final List<RobotCasesSection> sections = Selections.getElements(selection, RobotCasesSection.class);
        final List<RobotCase> cases = Selections.getElements(selection, RobotCase.class);
        if (sections.isEmpty() && cases.isEmpty() && resources.isEmpty()) {
            return Optional.empty();
        }
        final Set<IProject> projects = new HashSet<IProject>();
        for (final IResource resource : resources) {
            if (projects.add(resource.getProject()) && projects.size() > 1) {
                return Optional.empty();
            }
        }
        for (final RobotCasesSection section : sections) {
            if (projects.add(section.getSuiteFile().getProject().getProject()) && projects.size() > 1) {
                return Optional.empty();
            }
        }
        for (final RobotCase robotCase : cases) {
            if (projects.add(robotCase.getSuiteFile().getProject().getProject()) && projects.size() > 1) {
                return Optional.empty();
            }
        }
        return projects.stream().limit(1).findFirst();
    }

    @Override
    public void launch(final IEditorPart editor, final String mode) {
        final IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput) {
            final IResource file = ((FileEditorInput) input).getFile();
            createAndLaunchConfiguration(file.getProject(), mode);
        }
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(final ISelection selection) {
        return null;
    }

    @Override
    public ILaunchConfiguration[] getLaunchConfigurations(final IEditorPart editorpart) {
        return null;
    }

    @Override
    public IResource getLaunchableResource(final ISelection selection) {
        return null;
    }

    @Override
    public IResource getLaunchableResource(final IEditorPart editorpart) {
        return null;
    }
}
