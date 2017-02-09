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
import org.rf.ide.core.execution.ExecutionElement;
import org.rf.ide.core.execution.IExecutionHandler;
import org.rf.ide.core.executor.ILineHandler;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.rf.ide.core.executor.TestRunnerAgentHandler;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.base.Supplier;

class RobotLaunchInRunMode extends RobotLaunchInMode {

    private final RobotEventBroker robotEventBroker;

    RobotLaunchInRunMode(final RobotEventBroker robotEventBroker) {
        this.robotEventBroker = robotEventBroker;
    }

    @Override
    protected Process launchAndAttachToProcess(final RobotLaunchConfiguration robotConfig, final ILaunch launch,
            final IProgressMonitor monitor) throws CoreException, IOException {
        // FIXME : use monitor for progress reporting and cancellation
        // possibility

        final RobotProject robotProject = robotConfig.getRobotProject();

        final RunCommandLine cmdLine = prepareCommandLineBuilder(robotConfig).enableDebug(false).build();
        if (cmdLine.getPort() < 0) {
            throw newCoreException("Unable to find free port");
        }

        final RobotRuntimeEnvironment runtimeEnvironment = getRobotRuntimeEnvironment(robotProject);
        final TestRunnerAgentHandler agentHandler = runtimeEnvironment.startTestRunnerAgentHandler(cmdLine.getPort(), createMessagesLineHandler(),
                createExecutionEventsHandler());

        final String description = robotConfig.createConsoleDescription(runtimeEnvironment);
        final String version = robotConfig.createExecutorVersion(runtimeEnvironment);

        final Process process = execProcess(cmdLine, robotConfig);
        DebugPlugin.newProcess(launch, process, description);

        final RobotConsoleFacade consoleFacade = new RobotConsoleFacade();
        consoleFacade.connect(robotConfig, runtimeEnvironment, cmdLine, version);

        agentHandler.startTests(new Supplier<Boolean>() {

            @Override
            public Boolean get() {
                return !process.isAlive();
            }
        });
        return process;
    }

    private IExecutionHandler createExecutionEventsHandler() {
        return new IExecutionHandler() {

            @Override
            public void processExecutionElement(final ExecutionElement executionElement) {
                robotEventBroker.sendExecutionEventToExecutionView(executionElement);
            }
        };
    }

    private ILineHandler createMessagesLineHandler() {
        return new ILineHandler() {

            @Override
            public void processLine(final String line) {
                robotEventBroker.sendAppendLineEventToMessageLogView(line);
            }
        };
    }
}
