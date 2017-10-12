/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.rf.ide.core.execution.agent.TestsMode;
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
        return new IProject[] { LaunchConfigurationsWrappers.robotLaunchConfiguration(configuration).getProject() };
    }

    @Override
    public void launch(final ILaunchConfiguration configuration, final String mode, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException {

        final TestsMode testsMode = getTestsMode(mode);

        if (IRobotLaunchConfiguration.lockConfigurationLaunches()) {
            return;
        }

        RobotTestsLaunch testsLaunchContext = null;
        try {
            testsLaunchContext = executionService.testExecutionStarting(configuration);

            validateVersionAndProject(configuration);
            final LaunchExecution launchExecution = doLaunch(configuration, testsMode, launch, testsLaunchContext);

            // FIXME : don't need to wait when it would be possible to launch multiple
            // configurations
            launchExecution.waitFor(monitor);
        } finally {
            try {
                executionService.testExecutionEnded(testsLaunchContext);
            } finally {
                IRobotLaunchConfiguration.unlockConfigurationLaunches();
            }
        }
    }

    private static TestsMode getTestsMode(final String mode) throws CoreException {
        if (!ILaunchManager.RUN_MODE.equals(mode) && !ILaunchManager.DEBUG_MODE.equals(mode)) {
            throw newCoreException("Unrecognized launch mode: '" + mode + "'");
        }
        return ILaunchManager.RUN_MODE.equals(mode) ? TestsMode.RUN : TestsMode.DEBUG;
    }

    private void validateVersionAndProject(final ILaunchConfiguration configuration) throws CoreException {
        final IRobotLaunchConfiguration robotConfig = LaunchConfigurationsWrappers
                .robotLaunchConfiguration(configuration);
        if (!robotConfig.hasValidVersion()) {
            throw newCoreException("This configuration is incompatible with RED version you are currently using."
                    + "\nExpected: " + robotConfig.getCurrentConfigurationVersion() + ", but was: "
                    + robotConfig.getConfigurationVersion()
                    + "\n\nResolution: Delete old configurations manually and create the new ones.");
        }
        robotConfig.getProject();
    }

    @Override
    public ILaunch getLaunch(final ILaunchConfiguration configuration, final String mode) throws CoreException {
        final ILaunchConfiguration original = saveConfiguration(configuration);
        return new Launch(original, mode, null);
    }

    protected abstract LaunchExecution doLaunch(final ILaunchConfiguration configuration, final TestsMode testsMode,
            final ILaunch launch, final RobotTestsLaunch testsLaunchContext) throws CoreException;

    private ILaunchConfiguration saveConfiguration(final ILaunchConfiguration configuration) throws CoreException {
        if (configuration.isWorkingCopy()) {
            // since 3.3 ILaunchConfigurationWorkingCopy'ies can be nested
            return deepSaveConfigurationWorkingCopy((ILaunchConfigurationWorkingCopy) configuration);
        } else {
            return configuration;
        }
    }

    private ILaunchConfiguration deepSaveConfigurationWorkingCopy(final ILaunchConfigurationWorkingCopy copy)
            throws CoreException {
        final ILaunchConfiguration original = copy.doSave();
        final ILaunchConfigurationWorkingCopy parent = copy.getParent();
        return parent == null ? original : deepSaveConfigurationWorkingCopy(parent);
    }

    protected static class LaunchExecution {

        private final AgentConnectionServerJob serverJob;

        private final Process execProcess;

        private final IRobotProcess robotProcess;

        public LaunchExecution(final AgentConnectionServerJob serverJob, final Process execProcess,
                final IRobotProcess robotProcess) {
            this.serverJob = serverJob;
            this.execProcess = execProcess;
            this.robotProcess = robotProcess;
        }

        public void waitFor(final IProgressMonitor monitor) throws CoreException {
            try {
                if (execProcess != null) {
                    // TODO : after migration to Java 1.8 this can be changed to a loop using
                    // waitFor(timeout, unit) method in order to periodically check for monitor
                    // cancellations
                    execProcess.waitFor();
                } else {
                    serverJob.join();
                }
            } catch (final InterruptedException e) {
                throw newCoreException("Waiting for launch execution was interrupted", e);
            }
        }

        public IRobotProcess getRobotProcess() {
            return robotProcess;
        }

        public AgentConnectionServerJob getServerJob() {
            return serverJob;
        }
    }

}
