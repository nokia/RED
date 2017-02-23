/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.ExecutionTrackerForExecutionView;
import org.robotframework.ide.eclipse.main.plugin.debug.MessagesTrackerForLogView;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RemoteRobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationsValidator;


class RemoteLaunchInDebugMode {

    private final RobotEventBroker robotEventBroker;

    RemoteLaunchInDebugMode(final RobotEventBroker robotEventBroker) {
        this.robotEventBroker = robotEventBroker;
    }

    void launch(final RemoteRobotLaunchConfiguration robotConfig, final ILaunch launch) throws CoreException {

        new LaunchConfigurationsValidator().validate(robotConfig);
        
        final String host = robotConfig.getRemoteDebugHost();
        final int port = robotConfig.getRemoteDebugPort();
        final int timeout = robotConfig.getRemoteDebugTimeout();
        
        final AgentServerKeepAlive keepAliveListener = new AgentServerKeepAlive();
        final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter();
        final RemoteConnectionStatusTracker remoteConnectionStatusTracker = new RemoteConnectionStatusTracker();

        try {
            final AgentConnectionServerJob job = AgentConnectionServerJob.setupServerAt(host, port)
                    .withConnectionTimeout(timeout, TimeUnit.MILLISECONDS)
                    .serverStatusHandledBy(remoteConnectionStatusTracker)
                    .agentEventsListenedBy(keepAliveListener)
                    .agentEventsListenedBy(testsStarter)
                    .agentEventsListenedBy(remoteConnectionStatusTracker)
                    .agentEventsListenedBy(new MessagesTrackerForLogView())
                    .agentEventsListenedBy(new ExecutionTrackerForExecutionView(robotEventBroker))
                    .start()
                    .waitForServer();

            final String processLabel = "TCP connection using " + host + "@" + port;
            final IRobotProcess robotProcess = (IRobotProcess) DebugPlugin.newProcess(launch, null, processLabel);

            RemoteExecutionTerminationSupport.installTerminationSupport(job, keepAliveListener, robotProcess);

            final RobotConsoleFacade redConsole = robotProcess.provideConsoleFacade(processLabel);
            remoteConnectionStatusTracker.startTrackingInto(redConsole);

            final RemoteRobotDebugTarget debugTarget = new RemoteRobotDebugTarget("foo", launch, robotProcess);
            launch.addDebugTarget(debugTarget);

            testsStarter.allowClientTestsStart();
            job.join();
        } catch (final InterruptedException e) {
            throw newCoreException("Interrupted when waiting for remote connection server", e);
        }

    }

    private CoreException newCoreException(final String msg, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, msg, cause));
    }
}
