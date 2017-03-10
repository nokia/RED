/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionElementsTracker;
import org.robotframework.ide.eclipse.main.plugin.views.message.ExecutionMessagesTracker;

public class RemoteRobotLaunchConfigurationDelegate extends AbstractRobotLaunchConfigurationDelegate {

    @Override
    protected void doLaunch(final ILaunchConfiguration configuration, final TestsMode testsMode, final ILaunch launch,
            final RobotTestsLaunch testsLaunchContext, final IProgressMonitor monitor)
            throws CoreException, IOException {

        final RemoteRobotLaunchConfiguration robotConfig = new RemoteRobotLaunchConfiguration(configuration);

        final String host = robotConfig.getAgentConnectionHost();
        final int port = robotConfig.getAgentConnectionPort();
        final int timeout = robotConfig.getAgentConnectionTimeout();

        try {
            final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter(testsMode);
            final ServerJobWithProcess serverWithProcess;
            if (testsMode == TestsMode.RUN) {
                serverWithProcess = launchServerAndProcess(launch, testsLaunchContext, host,
                        port, timeout, newArrayList(testsStarter));
            } else {
                final RobotDebugTarget debugTarget = new RobotDebugTarget("Remote Robot Test at " + host + ":" + port,
                        launch);
                final DebugExecutionEventsListener debugListener = new DebugExecutionEventsListener(debugTarget,
                        robotConfig.getResourcesUnderDebug());

                serverWithProcess = launchServerAndProcess(launch, testsLaunchContext, host,
                        port, timeout, newArrayList(testsStarter, debugListener));

                debugTarget.connectWith(serverWithProcess.process);
            }
            testsStarter.allowClientTestsStart();

            // FIXME : don't need to wait when it would be possible to launch multiple
            // configurations
            serverWithProcess.serverJob.join();
        } catch (final InterruptedException e) {
            throw newCoreException("Interrupted when waiting for remote connection server", e);
        }
    }

    private ServerJobWithProcess launchServerAndProcess(final ILaunch launch, final RobotTestsLaunch testsLaunchContext,
            final String host, final int port, final int timeout,
            final List<RobotAgentEventListener> additionalListeners) throws InterruptedException {

        final RemoteConnectionStatusTracker remoteConnectionStatusTracker = new RemoteConnectionStatusTracker();
        final AgentConnectionServerJob job = AgentConnectionServerJob.setupServerAt(host, port)
                .withConnectionTimeout(timeout, TimeUnit.SECONDS)
                .serverStatusHandledBy(remoteConnectionStatusTracker)
                .agentEventsListenedBy(remoteConnectionStatusTracker)
                .agentEventsListenedBy(additionalListeners)
                .agentEventsListenedBy(new ExecutionMessagesTracker(testsLaunchContext))
                .agentEventsListenedBy(new ExecutionElementsTracker(testsLaunchContext))
                .agentEventsListenedBy(new AgentServerKeepAlive())
                .start()
                .waitForServer();

        final String processLabel = "TCP connection using " + host + "@" + port;
        final IRobotProcess robotProcess = (IRobotProcess) DebugPlugin.newProcess(launch, null, processLabel);

        robotProcess.onTerminate(() -> job.stopServer());
        TestsExecutionTerminationSupport.installTerminationSupport(job, robotProcess);

        final RobotConsoleFacade redConsole = robotProcess.provideConsoleFacade(processLabel);
        remoteConnectionStatusTracker.startTrackingInto(redConsole);

        return new ServerJobWithProcess(job, robotProcess);
    }

    private static class ServerJobWithProcess {

        private final AgentConnectionServerJob serverJob;

        private final IRobotProcess process;

        public ServerJobWithProcess(final AgentConnectionServerJob serverJob, final IRobotProcess process) {
            this.serverJob = serverJob;
            this.process = process;
        }
    }
}
