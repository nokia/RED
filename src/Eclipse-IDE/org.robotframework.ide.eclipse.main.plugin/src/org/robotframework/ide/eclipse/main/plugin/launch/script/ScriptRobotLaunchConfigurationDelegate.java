/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.script;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.LaunchConfigurationsWrappers;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ScriptRobotLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

    private final RobotEventBroker robotEventBroker;

    private final RobotTestExecutionService executionService;

    public ScriptRobotLaunchConfigurationDelegate() {
        this.robotEventBroker = new RobotEventBroker(PlatformUI.getWorkbench().getService(IEventBroker.class));
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

            robotEventBroker.sendClearEventToExecutionView();
            doLaunch(configuration, mode, launch, testsLaunchContext, monitor);
            saveConfiguration(configuration);
        } catch (final IOException e) {
            throw newCoreException("Unable to launch Robot", e);
        } finally {
            IRobotLaunchConfiguration.unlockConfigurationLaunches();
        }
    }

    private static void saveConfiguration(final ILaunchConfiguration configuration) throws CoreException {
        ILaunchConfigurationWorkingCopy toSave = null;
        if (configuration.isWorkingCopy()) {
            // since 3.3 ILaunchConfigurationWorkingCopy'ies can be nested
            final ILaunchConfiguration original = ((ILaunchConfigurationWorkingCopy) configuration).getOriginal();
            if (original != null) {
                toSave = original.getWorkingCopy();
            } else {
                toSave = (ILaunchConfigurationWorkingCopy) configuration;
            }
        } else {
            toSave = configuration.getWorkingCopy();
        }
        toSave.doSave();
    }

    private void doLaunch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final RobotTestsLaunch testsLaunchContext, final IProgressMonitor monitor)
            throws CoreException, IOException {

        final ScriptRobotLaunchConfiguration robotConfig = new ScriptRobotLaunchConfiguration(configuration);

        ScriptRobotLaunchInMode launchMode = null;
        if (ILaunchManager.RUN_MODE.equals(mode)) {
            launchMode = new ScriptLaunchInRunMode(robotEventBroker, testsLaunchContext);

        }
        else if (ILaunchManager.DEBUG_MODE.equals(mode)) {
            launchMode = new ScriptLaunchInDebugMode(robotEventBroker, testsLaunchContext);
        }
        launchMode.launch(robotConfig, launch, monitor);
    }

    private static CoreException newCoreException(final String message) {
        return newCoreException(message, null);
    }

    private static CoreException newCoreException(final String message, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, cause));
    }

}
