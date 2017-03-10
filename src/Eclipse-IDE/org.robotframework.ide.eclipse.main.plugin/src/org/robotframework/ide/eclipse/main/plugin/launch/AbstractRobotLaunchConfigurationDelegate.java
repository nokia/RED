/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

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
import org.rf.ide.core.execution.TestsMode;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public abstract class AbstractRobotLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

    private final RobotTestExecutionService executionService;

    public AbstractRobotLaunchConfigurationDelegate() {
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

            doLaunch(configuration, getTestsMode(mode), launch, testsLaunchContext, monitor);
            saveConfiguration(configuration);
        } catch (final IOException e) {
            throw newCoreException("Unable to launch Robot", e);
        } finally {
            IRobotLaunchConfiguration.unlockConfigurationLaunches();
        }
    }

    private static TestsMode getTestsMode(final String mode) {
        return ILaunchManager.RUN_MODE.equals(mode) ? TestsMode.RUN : TestsMode.DEBUG;
    }

    protected abstract void doLaunch(final ILaunchConfiguration configuration, final TestsMode testsMode,
            final ILaunch launch, final RobotTestsLaunch testsLaunchContext, final IProgressMonitor monitor)
            throws CoreException, IOException;

    private void saveConfiguration(final ILaunchConfiguration configuration) throws CoreException {
        if (configuration.isWorkingCopy()) {
            // since 3.3 ILaunchConfigurationWorkingCopy'ies can be nested
            deepSaveConfigurationWorkingCopy((ILaunchConfigurationWorkingCopy) configuration);
        } else {
            configuration.getWorkingCopy().doSave();
        }
    }

    private void deepSaveConfigurationWorkingCopy(final ILaunchConfigurationWorkingCopy copy) throws CoreException {
        copy.doSave();
        final ILaunchConfigurationWorkingCopy parent = copy.getParent();
        if (parent != null) {
            deepSaveConfigurationWorkingCopy(parent);
        }
    }

}
