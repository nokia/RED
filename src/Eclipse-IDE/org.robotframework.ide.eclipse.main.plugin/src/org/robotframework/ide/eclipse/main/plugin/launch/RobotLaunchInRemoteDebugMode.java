/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.debug.utils.DebugSocketManager;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;


class RobotLaunchInRemoteDebugMode extends RobotLaunchInMode {

    private final RobotEventBroker robotEventBroker;

    public RobotLaunchInRemoteDebugMode(final RobotEventBroker robotEventBroker) {
        this.robotEventBroker = robotEventBroker;
    }

    @Override
    protected Process launchAndAttachToProcess(final RobotLaunchConfiguration robotConfig, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException, IOException {

        final RobotProject robotProject = robotConfig.getRobotProject();
        final RobotRuntimeEnvironment runtimeEnvironment = getRobotRuntimeEnvironment(robotProject);

        final int remoteDebugPort = robotConfig.getRemoteDebugPort().get();
        final RunCommandLine cmdLine = createRemoteModeCmd(runtimeEnvironment, remoteDebugPort);

        if (cmdLine.getPort() < 0) {
            throw newCoreException("Unable to find free port");
        }

        final String host = robotConfig.getRemoteDebugHost();
        final DebugSocketManager socketManager = new DebugSocketManager(host, cmdLine.getPort(),
                robotConfig.getRemoteDebugTimeout());
        new Thread(socketManager).start();
        final boolean isDebugServerSocketListening = socketManager.waitForDebugServerSocket();

        final String description = robotConfig.createConsoleDescription(runtimeEnvironment);
        final String version = robotConfig.isUsingInterpreterFromProject() ? runtimeEnvironment.getVersion()
                : RobotRuntimeEnvironment.getVersion(robotConfig.getExecutor());

        final Process process = execProcess(cmdLine, robotConfig);
        final IProcess eclipseProcess = DebugPlugin.newProcess(launch, process, description);

        final RobotConsoleFacade consoleFacade = new RobotConsoleFacade();
        consoleFacade.connect(robotConfig, runtimeEnvironment, cmdLine, version);
        if (isDebugServerSocketListening && socketManager.getServerSocket() != null) {
            consoleFacade.writeLine(
                    "Debug server is listening on " + host + ":" + remoteDebugPort + ", you can run a remote test");
        } else {
            if (eclipseProcess != null) {
                eclipseProcess.terminate();
            }
            throw newCoreException("Cannot run Debug server on " + host + ":" + remoteDebugPort + ".");
        }

        try {
            final RobotDebugTarget target = new RobotDebugTarget(launch, eclipseProcess, consoleFacade, true);
            target.connect(robotConfig.getResourcesUnderDebug(), robotEventBroker, socketManager);
            launch.addDebugTarget(target);
        } catch (final CoreException e) {
            socketManager.closeServerSocket();
        }

        return process;
    }

    private RunCommandLine createRemoteModeCmd(final RobotRuntimeEnvironment env, final int port) throws IOException {
        return RunCommandLineCallBuilder.forRemoteEnvironment(env, port).build();
    }
}
