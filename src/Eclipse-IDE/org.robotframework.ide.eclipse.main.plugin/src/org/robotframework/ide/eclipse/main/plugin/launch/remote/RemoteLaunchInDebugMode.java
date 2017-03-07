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
import org.rf.ide.core.execution.TestsMode;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.launch.DebugExecutionEventsListener;
import org.robotframework.ide.eclipse.main.plugin.launch.ExecutionTrackerForExecutionView;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.tabs.LaunchConfigurationsValidator;
import org.robotframework.ide.eclipse.main.plugin.views.message.ExecutionMessagesTracker;


class RemoteLaunchInDebugMode {

    private final RobotEventBroker robotEventBroker;

    private final RobotTestsLaunch testsLaunchContext;

    RemoteLaunchInDebugMode(final RobotEventBroker robotEventBroker, final RobotTestsLaunch testsLaunchContext) {
        this.robotEventBroker = robotEventBroker;
        this.testsLaunchContext = testsLaunchContext;
    }

    void launch(final RemoteRobotLaunchConfiguration robotConfig, final ILaunch launch) throws CoreException {

        new LaunchConfigurationsValidator().validate(robotConfig);

        final String host = robotConfig.getRemoteHost().orElse(AgentConnectionServer.DEFAULT_CLIENT_HOST);
        final int port = robotConfig.getRemotePort().orElseGet(AgentConnectionServer::findFreePort);
        if (port < 0) {
            throw newCoreException("Unable to find free port");
        }
        final int timeout = robotConfig.getRemoteTimeout()
                .orElse(AgentConnectionServer.DEFAULT_CLIENT_CONNECTION_TIMEOUT);

        final AgentServerKeepAlive keepAliveListener = new AgentServerKeepAlive();
        final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter(TestsMode.DEBUG);
        final RemoteConnectionStatusTracker remoteConnectionStatusTracker = new RemoteConnectionStatusTracker();

        final RobotDebugTarget debugTarget = new RobotDebugTarget("Remote Robot Test at " + host + ":" + port, launch);

        try {
            final AgentConnectionServerJob job = AgentConnectionServerJob.setupServerAt(host, port)
                    .withConnectionTimeout(timeout, TimeUnit.SECONDS)
                    .serverStatusHandledBy(remoteConnectionStatusTracker)
                    .agentEventsListenedBy(keepAliveListener)
                    .agentEventsListenedBy(testsStarter)
                    .agentEventsListenedBy(remoteConnectionStatusTracker)
                    .agentEventsListenedBy(
                            new DebugExecutionEventsListener(debugTarget, robotConfig.getResourcesUnderDebug()))
                    .agentEventsListenedBy(new ExecutionMessagesTracker(testsLaunchContext))
                    .agentEventsListenedBy(new ExecutionTrackerForExecutionView(robotEventBroker))
                    .start()
                    .waitForServer();

            final String processLabel = "TCP connection using " + host + "@" + port;
            final IRobotProcess robotProcess = (IRobotProcess) DebugPlugin.newProcess(launch, null, processLabel);

            TestsExecutionTerminationSupport.installTerminationSupport(job, keepAliveListener, robotProcess);

            final RobotConsoleFacade redConsole = robotProcess.provideConsoleFacade(processLabel);
            remoteConnectionStatusTracker.startTrackingInto(redConsole);

            debugTarget.connectWith(robotProcess);

            testsStarter.allowClientTestsStart();
            job.join();
        } catch (final InterruptedException e) {
            throw newCoreException("Interrupted when waiting for remote connection server", e);
        }

    }

    private CoreException newCoreException(final String message) {
        return newCoreException(message, null);
    }

    private CoreException newCoreException(final String msg, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, msg, cause));
    }
}
