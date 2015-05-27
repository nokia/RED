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
import org.robotframework.ide.eclipse.main.plugin.launch.RobotLaunchConfiguration;

public class RobotSourcePathComputerDelegate implements ISourcePathComputerDelegate {

    @Override
    public ISourceContainer[] computeSourceContainers(final ILaunchConfiguration configuration, final IProgressMonitor monitor)
            throws CoreException {

        final String projectName = new RobotLaunchConfiguration(configuration).getProjectName();
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (project.exists()) {
            return new ISourceContainer[] { new ProjectSourceContainer(project, true) };
        }
        return new ISourceContainer[] { new WorkspaceSourceContainer() };
    }
}
