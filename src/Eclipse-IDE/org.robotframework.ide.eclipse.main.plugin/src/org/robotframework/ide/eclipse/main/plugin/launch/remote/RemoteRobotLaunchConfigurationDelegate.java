/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.rf.ide.core.execution.agent.RobotAgentEventListener;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.debug.ExecutionPauseContinueListener;
import org.rf.ide.core.execution.debug.Stacktrace;
import org.rf.ide.core.execution.debug.StacktraceBuilder;
import org.rf.ide.core.execution.debug.UserProcessController;
import org.rf.ide.core.execution.debug.UserProcessDebugController;
import org.rf.ide.core.execution.debug.UserProcessDebugController.DebuggerPreferences;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerStatusListener;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.rf.ide.core.execution.server.AgentServerVersionsChecker;
import org.rf.ide.core.execution.server.AgentServerVersionsDebugChecker;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotBreakpoints;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.launch.AbstractRobotLaunchConfigurationDelegate;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.launch.BreakpointsEnabler;
import org.robotframework.ide.eclipse.main.plugin.launch.DebuggerErrorDecider;
import org.robotframework.ide.eclipse.main.plugin.launch.EclipseElementsLocator;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.ProcessConnectingInDebugServerListener;
import org.robotframework.ide.eclipse.main.plugin.launch.ProcessConnectingInRunServerListener;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEvaluationErrorsHandler;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.TestsExecutionTerminationSupport;
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
            final UserProcessController userController;
            final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter(testsMode);

            final LaunchExecution launchExecution;
            if (testsMode == TestsMode.RUN) {
                userController = new UserProcessController();

                final List<AgentServerStatusListener> additionalServerListeners = newArrayList(
                        new ProcessConnectingInRunServerListener(launch));

                final List<RobotAgentEventListener> additionalAgentListeners = new ArrayList<>();
                additionalAgentListeners.add(new AgentServerVersionsChecker());
                additionalAgentListeners.add(testsStarter);

                launchExecution = doLaunch(launch, testsLaunchContext, host, port, timeout, userController,
                        additionalServerListeners, additionalAgentListeners);
                testsStarter.allowClientTestsStart();

            } else {
                final Stacktrace stacktrace = new Stacktrace();
                final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
                userController = new UserProcessDebugController(stacktrace,
                        new DebuggerPreferences(new DebuggerErrorDecider(preferences),
                                !preferences.shouldDebuggerOmitLibraryKeywords()));

                final RobotDebugTarget debugTarget = new RobotDebugTarget("Remote Robot Test at " + host + ":" + port,
                        launch, stacktrace, (UserProcessDebugController) userController);

                final EclipseElementsLocator elementsLocator = new EclipseElementsLocator(robotConfig.getProject());

                final List<AgentServerStatusListener> additionalServerListeners = newArrayList(
                        new ProcessConnectingInDebugServerListener(launch), new BreakpointsEnabler());
                
                final List<RobotAgentEventListener> additionalAgentListeners = new ArrayList<>();
                additionalAgentListeners.add(new AgentServerVersionsDebugChecker());
                additionalAgentListeners.add(testsStarter);
                additionalAgentListeners.add(new StacktraceBuilder(stacktrace, elementsLocator,
                        (uri, line) -> new RobotBreakpoints().getBreakpointAtLine(line, uri)));
                additionalAgentListeners.add(new RobotEvaluationErrorsHandler());

                launchExecution = doLaunch(launch, testsLaunchContext, host, port, timeout, userController,
                        additionalServerListeners, additionalAgentListeners);
                TestsExecutionTerminationSupport.installTerminationSupport(launchExecution.getServerJob(), debugTarget);
                testsStarter.allowClientTestsStart();

                launch.addDebugTarget(debugTarget);
                debugTarget.setProcess(launchExecution.getRobotProcess());
            }
            if (launchExecution.getRobotProcess() != null) {
                launchExecution.getRobotProcess().setUserProcessController(userController);
            }
            return launchExecution;
        } catch (final InterruptedException e) {
            throw newCoreException("Interrupted when waiting for remote connection server", e);
        }
    }

    private LaunchExecution doLaunch(final ILaunch launch, final RobotTestsLaunch testsLaunchContext, final String host,
            final int port, final int timeout, final UserProcessController controller,
            final List<AgentServerStatusListener> additionalServerListeners,
            final List<RobotAgentEventListener> additionalAgentListeners)
            throws InterruptedException, CoreException {

        final RemoteConnectionStatusTracker remoteConnectionStatusTracker = new RemoteConnectionStatusTracker();
        final AgentConnectionServerJob serverJob = AgentConnectionServerJob.setupServerAt(host, port)
                .withConnectionTimeout(timeout, TimeUnit.SECONDS)
                .serverStatusHandledBy(remoteConnectionStatusTracker)
                .serverStatusHandledBy(additionalServerListeners)
                .agentEventsListenedBy(remoteConnectionStatusTracker)
                .agentEventsListenedBy(additionalAgentListeners)
                .agentEventsListenedBy(new ExecutionPauseContinueListener(controller))
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
