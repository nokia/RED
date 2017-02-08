/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.IRunCommandLineBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

abstract class RobotLaunchInMode {

    protected void launch(final RobotLaunchConfiguration robotConfig, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException, IOException {

        final Process process = launchAndAttachToProcess(robotConfig, launch, monitor);
        try {
            if (process != null) {
                // TODO : after migration to Java 1.8 this can be changed to a loop using
                // waitFor(timeout, unit) method in order to periodically check for monitor
                // cancellations
                process.waitFor();
            }
        } catch (final InterruptedException e) {
            throw newCoreException("Robot process was interrupted", e);
        }
    }

    protected abstract Process launchAndAttachToProcess(final RobotLaunchConfiguration robotConfig,
            final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException, IOException;

    protected Process execProcess(final RunCommandLine cmdLine, final RobotLaunchConfiguration robotConfig)
            throws CoreException {
        final RobotProject robotProject = robotConfig.getRobotProject();
        return DebugPlugin.exec(cmdLine.getCommandLine(),
                robotProject.getProject().getLocation().toFile(), robotConfig.getEnvironmentVariables());
    }

    protected final RobotRuntimeEnvironment getRobotRuntimeEnvironment(final RobotProject robotProject)
            throws CoreException {

        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        if (runtimeEnvironment == null) {
            throw newCoreException(
                    "There is no active runtime environment for project '" + robotProject.getName() + "'");
        }
        if (!runtimeEnvironment.hasRobotInstalled()) {
            throw newCoreException("The runtime environment " + runtimeEnvironment.getFile().getAbsolutePath()
                    + " is either not a python installation or it has no Robot installed");
        }
        return runtimeEnvironment;
    }

    protected final IRunCommandLineBuilder prepareCommandLineBuilder(final RobotLaunchConfiguration robotConfig)
            throws CoreException, IOException {

        final RobotProject robotProject = robotConfig.getRobotProject();

        final IRunCommandLineBuilder builder = robotConfig.isUsingInterpreterFromProject()
                ? RunCommandLineCallBuilder.forEnvironment(robotProject.getRuntimeEnvironment())
                : RunCommandLineCallBuilder.forExecutor(robotConfig.getExecutor());

        builder.withProject(robotProject.getProject().getLocation().toFile());
        builder.addLocationsToClassPath(robotProject.getClasspath());
        builder.addLocationsToPythonPath(robotProject.getPythonpath());
        builder.addUserArgumentsForInterpreter(robotConfig.getInterpeterArguments());
        builder.addUserArgumentsForRobot(robotConfig.getExecutorArguments());

        builder.addVariableFiles(robotProject.getVariableFilePaths());

        builder.suitesToRun(robotConfig.getSuitesToRun());
        builder.testsToRun(robotConfig.getTestsToRun());

        if (robotConfig.isIncludeTagsEnabled()) {
            builder.includeTags(robotConfig.getIncludedTags());
        }
        if (robotConfig.isExcludeTagsEnabled()) {
            builder.excludeTags(robotConfig.getExcludedTags());
        }
        return builder;
    }

    protected final CoreException newCoreException(final String message) {
        return newCoreException(message, null);
    }

    protected final CoreException newCoreException(final String message, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, cause));
    }
}
