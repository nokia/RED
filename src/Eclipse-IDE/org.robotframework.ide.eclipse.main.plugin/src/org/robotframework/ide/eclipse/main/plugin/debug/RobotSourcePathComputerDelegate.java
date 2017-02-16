/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;

public class RobotSourcePathComputerDelegate implements ISourcePathComputerDelegate {

    @Override
    public ISourceContainer[] computeSourceContainers(final ILaunchConfiguration configuration, final IProgressMonitor monitor)
            throws CoreException {

        final String projectName = LaunchConfigurationsWrappers.robotLaunchConfiguration(configuration)
                .getProjectName();
        if (projectName.isEmpty()) {
            return new ISourceContainer[0];
        }

        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        final ISourceContainer container = project.exists() ? new ProjectSourceContainer(project, true)
                : new WorkspaceSourceContainer();
        return new ISourceContainer[] { container };
    }
}
