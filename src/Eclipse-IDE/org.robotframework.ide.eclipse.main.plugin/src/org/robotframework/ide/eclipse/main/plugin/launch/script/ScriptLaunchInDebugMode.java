/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.script;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.rf.ide.core.execution.TestsMode;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.launch.DebugExecutionEventsListener;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsolePatternsListener;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionElementsTracker;
import org.robotframework.ide.eclipse.main.plugin.views.message.ExecutionMessagesTracker;

class ScriptLaunchInDebugMode extends ScriptRobotLaunchInMode {

    private final RobotTestsLaunch testsLaunchContext;

    public ScriptLaunchInDebugMode(final RobotTestsLaunch testsLaunchContext) {
        this.testsLaunchContext = testsLaunchContext;
    }

    @Override
    protected Process launchAndAttachToProcess(final ScriptRobotLaunchConfiguration robotConfig, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException, IOException {

        final RobotProject robotProject = robotConfig.getRobotProject();

        final String host = robotConfig.getRemoteHost().orElse(AgentConnectionServer.DEFAULT_CLIENT_HOST);
        final int port = robotConfig.getRemotePort().orElseGet(AgentConnectionServer::findFreePort);
        if (port < 0) {
            throw newCoreException("Unable to find free port");
        }
        final int timeout = robotConfig.getRemoteTimeout()
                .orElse(AgentConnectionServer.DEFAULT_CLIENT_CONNECTION_TIMEOUT);

        final AgentServerKeepAlive keepAliveListener = new AgentServerKeepAlive();
        final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter(TestsMode.DEBUG);

        final RobotDebugTarget debugTarget = new RobotDebugTarget("Robot Test at " + host + ":" + port, launch);

        try {
            AgentConnectionServerJob.setupServerAt(host, port)
                    .withConnectionTimeout(timeout, TimeUnit.SECONDS)
                    .agentEventsListenedBy(keepAliveListener)
                    .agentEventsListenedBy(testsStarter)
                    .agentEventsListenedBy(
                            new DebugExecutionEventsListener(debugTarget, robotConfig.getResourcesUnderDebug()))
                    .agentEventsListenedBy(new ExecutionMessagesTracker(testsLaunchContext))
                    .agentEventsListenedBy(new ExecutionElementsTracker(testsLaunchContext))
                    .start()
                    .waitForServer();

            final String processLabel = robotConfig.getScriptPath();

            final RunCommandLine cmdLine = prepareCommandLine(robotConfig, port);

            final Process process = execProcess(cmdLine, robotConfig);
            final IRobotProcess robotProcess = (IRobotProcess) DebugPlugin.newProcess(launch, process, processLabel);

            final RobotConsoleFacade redConsole = robotProcess.provideConsoleFacade(processLabel);
            redConsole.addHyperlinksSupport(new RobotConsolePatternsListener(robotProject));
            redConsole.writeLine("Command: " + cmdLine.show());

            debugTarget.connectWith(robotProcess);

            testsStarter.allowClientTestsStart();
            return process;
        } catch (final InterruptedException e) {
            throw newCoreException("Interrupted when waiting for remote connection server", e);
        }
    }
}
