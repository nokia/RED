/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class RobotLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

    private final RobotTestExecutionService executionService;

    public RobotLaunchConfigurationDelegate() {
        this.executionService = RedPlugin.getTestExecutionService();
    }

    @Override
    protected IProject[] getProjectsForProblemSearch(final ILaunchConfiguration configuration, final String mode)
            throws CoreException {
        return new IProject[] {
                LaunchConfigurationsWrappers.robotLaunchConfiguration(configuration).getRobotProject().getProject() };
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
            saveConfiguration(configuration);
        } catch (final IOException e) {
            throw newCoreException("Unable to launch Robot", e);
        } finally {
            IRobotLaunchConfiguration.unlockConfigurationLaunches();
        }
    }

    private static void saveConfiguration(final ILaunchConfiguration configuration) throws CoreException {
        if (configuration.isWorkingCopy()) {
            // since 3.3 ILaunchConfigurationWorkingCopy'ies can be nested
            deepSaveConfigurationWorkingCopy((ILaunchConfigurationWorkingCopy) configuration);
        } else {
            configuration.getWorkingCopy().doSave();
        }
    }

    private static void deepSaveConfigurationWorkingCopy(final ILaunchConfigurationWorkingCopy copy)
            throws CoreException {
        copy.doSave();
        final ILaunchConfigurationWorkingCopy parent = copy.getParent();
        if (parent != null) {
            deepSaveConfigurationWorkingCopy(parent);
        }
    }

    private void doLaunch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final RobotTestsLaunch testsLaunchContext, final IProgressMonitor monitor)
            throws CoreException, IOException {

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

        if (ILaunchManager.RUN_MODE.equals(mode)) {
            new RobotLaunchInRunMode(testsLaunchContext).launch(robotConfig, launch, monitor);
        } else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            new RobotLaunchInDebugMode(testsLaunchContext).launch(robotConfig, launch, monitor);
        }
    }

}
