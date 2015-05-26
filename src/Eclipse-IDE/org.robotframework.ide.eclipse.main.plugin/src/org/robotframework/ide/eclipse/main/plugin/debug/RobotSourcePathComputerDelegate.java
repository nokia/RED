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
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.RobotLaunchConfigurationMainTab;

public class RobotSourcePathComputerDelegate implements ISourcePathComputerDelegate {

    /*
     * (non-Javadoc)
     * @see
     * org.eclipse.debug.internal.core.sourcelookup.ISourcePathComputerDelegate#computeSourceContainers
     * (org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.core.runtime.IProgressMonitor)
     */
    public ISourceContainer[] computeSourceContainers(ILaunchConfiguration configuration, IProgressMonitor monitor)
            throws CoreException {

        String projectNameAttribute = configuration.getAttribute(
                RobotLaunchConfigurationMainTab.PROJECT_NAME_ATTRIBUTE, "");
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNameAttribute);

        if (project.exists()) {
            return new ISourceContainer[] { new ProjectSourceContainer(project, true) };
        }

        return new ISourceContainer[] { new WorkspaceSourceContainer() };
    }
}
