/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;


public class RemoteRobotLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

    private final RobotTestExecutionService executionService;

    public RemoteRobotLaunchConfigurationDelegate() {
        this.executionService = RedPlugin.getTestExecutionService();
    }

    @Override
    public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException {
        if (!ILaunchManager.RUN_MODE.equals(mode) && !ILaunchManager.DEBUG_MODE.equals(mode)) {
            throw newCoreException("Unrecognized launch mode: '" + mode + "'");
        }

        if (IRobotLaunchConfiguration.lockConfigurationLaunches()) {
            return;
        }
        try {
            final RobotTestsLaunch testsLaunchContext = executionService.testExecutionStarting();

            doLaunch(configuration, mode, launch, testsLaunchContext, monitor);
        } catch (final IOException e) {
            throw newCoreException("Unable to launch Robot", e);
        } finally {
            IRobotLaunchConfiguration.unlockConfigurationLaunches();
        }
    }

    private void doLaunch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final RobotTestsLaunch testsLaunchContext, final IProgressMonitor monitor)
            throws CoreException, IOException {

        final RemoteRobotLaunchConfiguration robotConfig = new RemoteRobotLaunchConfiguration(configuration);

        if (ILaunchManager.RUN_MODE.equals(mode)) {
            new RemoteLaunchInRunMode(testsLaunchContext).launch(robotConfig, launch);
        } else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            new RemoteLaunchInDebugMode(testsLaunchContext).launch(robotConfig, launch);
        }
    }

    @Override
    protected IProject[] getProjectsForProblemSearch(final ILaunchConfiguration configuration, final String mode)
            throws CoreException {
        return new IProject[] {
                LaunchConfigurationsWrappers.robotLaunchConfiguration(configuration).getRobotProject().getProject() };
    }

}
