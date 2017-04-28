/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.rf.ide.core.execution.RobotAgentEventListener;
import org.rf.ide.core.execution.TestsMode;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.launch.DebugExecutionEventsListener;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusTracker;
import org.robotframework.ide.eclipse.main.plugin.views.message.ExecutionMessagesTracker;

public class RemoteRobotLaunchConfigurationDelegate extends AbstractRobotLaunchConfigurationDelegate {

    @Override
    protected LaunchExecution doLaunch(final ILaunchConfiguration configuration, final TestsMode testsMode,
            final ILaunch launch, final RobotTestsLaunch testsLaunchContext) throws CoreException {

        final RemoteRobotLaunchConfiguration robotConfig = new RemoteRobotLaunchConfiguration(configuration);

        final String host = robotConfig.getAgentConnectionHost();
        final int port = robotConfig.getAgentConnectionPort();
        final int timeout = robotConfig.getAgentConnectionTimeout();

        try {
            final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter(testsMode);
            final LaunchExecution launchExecution;
            if (testsMode == TestsMode.RUN) {
                launchExecution = doLaunch(launch, testsLaunchContext, host, port, timeout,
                        Arrays.asList(testsStarter));
            } else {
                final RobotDebugTarget debugTarget = new RobotDebugTarget("Remote Robot Test at " + host + ":" + port,
                        launch);
                final DebugExecutionEventsListener debugListener = new DebugExecutionEventsListener(debugTarget,
                        robotConfig.getResourcesUnderDebug());

                launchExecution = doLaunch(launch, testsLaunchContext, host, port, timeout,
                        Arrays.asList(testsStarter, debugListener));

                if (launchExecution.getRobotProcess() != null) {
                    debugTarget.connectWith(launchExecution.getRobotProcess());
                }
            }
            testsStarter.allowClientTestsStart();

            return launchExecution;
        } catch (final InterruptedException e) {
            throw newCoreException("Interrupted when waiting for remote connection server", e);
        }
    }

    private LaunchExecution doLaunch(final ILaunch launch, final RobotTestsLaunch testsLaunchContext, final String host,
            final int port, final int timeout, final List<RobotAgentEventListener> additionalListeners)
            throws InterruptedException, CoreException {

        final RemoteConnectionStatusTracker remoteConnectionStatusTracker = new RemoteConnectionStatusTracker();
        final AgentConnectionServerJob serverJob = AgentConnectionServerJob.setupServerAt(host, port)
                .withConnectionTimeout(timeout, TimeUnit.SECONDS)
                .serverStatusHandledBy(remoteConnectionStatusTracker)
                .agentEventsListenedBy(remoteConnectionStatusTracker)
                .agentEventsListenedBy(additionalListeners)
                .agentEventsListenedBy(new ExecutionMessagesTracker(testsLaunchContext))
                .agentEventsListenedBy(new ExecutionStatusTracker(testsLaunchContext))
                .agentEventsListenedBy(new AgentServerKeepAlive())
                .start()
                .waitForServer();

        if (serverJob.getResult() != null && !serverJob.getResult().isOK()) {
            return new LaunchExecution(serverJob, null, null);
        }

        final String processLabel = "TCP connection using " + host + "@" + port;
        final IRobotProcess robotProcess = (IRobotProcess) DebugPlugin.newProcess(launch, null, processLabel);

        robotProcess.onTerminate(serverJob::stopServer);
        TestsExecutionTerminationSupport.installTerminationSupport(serverJob, robotProcess);

        final RobotConsoleFacade redConsole = robotProcess.provideConsoleFacade(processLabel);
        remoteConnectionStatusTracker.startTrackingInto(redConsole);

        return new LaunchExecution(serverJob, null, robotProcess);
    }
}
