/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static com.google.common.collect.Lists.newArrayList;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfiguration;

public class RemoteRobotLaunchConfiguration extends AbstractRobotLaunchConfiguration {

    public static final String TYPE_ID = "org.robotframework.ide.remoteRobotLaunchConfiguration";

    public RemoteRobotLaunchConfiguration(final ILaunchConfiguration config) {
        super(config);
    }

    @Override
    public List<IResource> getResourcesUnderDebug() throws CoreException {
        return newArrayList(getProject());
    }

    @Override
    public boolean isRemoteAgent() throws CoreException {
        return true;
    }

    public static ILaunchConfigurationWorkingCopy prepareDefault(final IProject project) throws CoreException {
        final ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        final String namePrefix = project.getName();
        final String name = launchManager.generateLaunchConfigurationName(namePrefix);

        final ILaunchConfigurationWorkingCopy configuration = launchManager.getLaunchConfigurationType(TYPE_ID)
                .newInstance(null, name);
        final RemoteRobotLaunchConfiguration remoteConfig = new RemoteRobotLaunchConfiguration(configuration);
        remoteConfig.fillDefaults();
        remoteConfig.setProjectName(namePrefix);
        return configuration;
    }

}
