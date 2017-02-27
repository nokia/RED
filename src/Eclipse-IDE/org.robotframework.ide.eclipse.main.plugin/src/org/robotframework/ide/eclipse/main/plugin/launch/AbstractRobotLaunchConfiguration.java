/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

public abstract class AbstractRobotLaunchConfiguration implements IRobotLaunchConfiguration {

    protected final ILaunchConfiguration configuration;

    protected AbstractRobotLaunchConfiguration(final ILaunchConfiguration config) {
        this.configuration = config;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public String getTypeName() {
        try {
            return configuration.getType().getName();
        } catch (final CoreException e) {
            return null;
        }
    }

    @Override
    public String getProjectName() throws CoreException {
        return configuration.getAttribute(PROJECT_NAME_ATTRIBUTE, "");
    }

    @Override
    public void setProjectName(final String projectName) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(PROJECT_NAME_ATTRIBUTE, projectName);
    }

    @Override
    public RobotProject getRobotProject() throws CoreException {
        final IProject project = getProject();
        return RedPlugin.getModelManager().getModel().createRobotProject(project);
    }

    protected IProject getProject() throws CoreException {
        final String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return null;
        }
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!project.exists()) {
            throw newCoreException("Project '" + projectName + "' cannot be found in workspace");
        }
        return project;
    }

    @Override
    public void fillDefaults() throws CoreException {
        setProjectName("");
        setProcessFactory(LaunchConfigurationsWrappers.FACTORY_ID);
    }

    private void setProcessFactory(final String id) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, id);
    }

    public ILaunchConfigurationWorkingCopy asWorkingCopy() throws CoreException {
        return configuration instanceof ILaunchConfigurationWorkingCopy
                ? (ILaunchConfigurationWorkingCopy) configuration : configuration.getWorkingCopy();
    }

    protected static CoreException newCoreException(final String message) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message));
    }

}
