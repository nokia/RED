/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;
import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.rf.ide.core.execution.RunCommandLineCallBuilder.RunCommandLine;
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
import org.rf.ide.core.execution.server.TestsPidReader;
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
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsolePatternsListener;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEvaluationErrorsHandler;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;
import org.robotframework.ide.eclipse.main.plugin.launch.TestsExecutionTerminationSupport;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusTracker;
import org.robotframework.ide.eclipse.main.plugin.views.message.ExecutionMessagesTracker;

import com.google.common.annotations.VisibleForTesting;

public class RobotLaunchConfigurationDelegate extends AbstractRobotLaunchConfigurationDelegate {

    @Override
    protected LaunchExecution doLaunch(final ILaunchConfiguration configuration, final TestsMode testsMode,
            final ILaunch launch, final RobotTestsLaunch testsLaunchContext) throws CoreException {

        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(configuration);

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

                final List<RobotAgentEventListener> additionalAgentListeners = newArrayList(
                        new AgentServerVersionsChecker(), testsStarter);

                launchExecution = doLaunch(robotConfig, launch, testsLaunchContext, host, port, timeout, userController,
                        additionalServerListeners, additionalAgentListeners);
                testsStarter.allowClientTestsStart();
            } else {
                final Stacktrace stacktrace = new Stacktrace();
                final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
                userController = new UserProcessDebugController(stacktrace, new DebuggerPreferences(
                        new DebuggerErrorDecider(preferences), !preferences.shouldDebuggerOmitLibraryKeywords()));

                final RobotDebugTarget debugTarget = new RobotDebugTarget("Robot Test at " + host + ":" + port, launch,
                        stacktrace, (UserProcessDebugController) userController);

                final StacktraceBuilder stacktraceBuilder = new StacktraceBuilder(stacktrace,
                        new EclipseElementsLocator(robotConfig.getProject()),
                        (uri, line) -> new RobotBreakpoints().getBreakpointAtLine(line, uri));

                final List<AgentServerStatusListener> additionalServerListeners = newArrayList(
                        new ProcessConnectingInDebugServerListener(launch), new BreakpointsEnabler());

                final List<RobotAgentEventListener> additionalAgentListeners = newArrayList(
                        new AgentServerVersionsDebugChecker(), testsStarter, stacktraceBuilder,
                        new RobotEvaluationErrorsHandler());

                launchExecution = doLaunch(robotConfig, launch, testsLaunchContext, host, port, timeout, userController,
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
        } catch (final IOException e) {
            throw newCoreException("Unable to launch Robot", e);
        }
    }

    private LaunchExecution doLaunch(final RobotLaunchConfiguration robotConfig, final ILaunch launch,
            final RobotTestsLaunch testsLaunchContext, final String host, final int port, final int timeout,
            final UserProcessController userController, final List<AgentServerStatusListener> additionalServerListeners,
            final List<RobotAgentEventListener> additionalAgentListeners)
            throws InterruptedException, CoreException, IOException {

        final RobotModel model = RedPlugin.getModelManager().getModel();
        final RobotProject robotProject = model.createRobotProject(robotConfig.getProject());
        final LocalProcessInterpreter interpreter = LocalProcessInterpreter.create(robotConfig, robotProject);

        final TestsPidReader pidReader = new TestsPidReader();
        final AgentConnectionServerJob serverJob = AgentConnectionServerJob.setupServerAt(host, port)
                .withConnectionTimeout(timeout, TimeUnit.SECONDS)
                .serverStatusHandledBy(new ServerProblemsHandler())
                .serverStatusHandledBy(additionalServerListeners)
                .agentEventsListenedBy(additionalAgentListeners)
                .agentEventsListenedBy(pidReader)
                .agentEventsListenedBy(new ExecutionPauseContinueListener(userController))
                .agentEventsListenedBy(new ExecutionMessagesTracker(testsLaunchContext))
                .agentEventsListenedBy(new ExecutionStatusTracker(testsLaunchContext))
                .agentEventsListenedBy(new AgentServerKeepAlive())
                .start()
                .waitForServer();

        if (serverJob.getResult() != null && !serverJob.getResult().isOK()) {
            return new LaunchExecution(serverJob, null, null);
        }

        final ConsoleData consoleData = ConsoleData.create(robotConfig, interpreter);
        final RunCommandLine cmdLine = new LocalProcessCommandLineBuilder(interpreter, robotConfig, robotProject)
                .createRunCommandLine(port, RedPlugin.getDefault().getPreferences());
        final Process execProcess = DebugPlugin.exec(cmdLine.getCommandLine(),
                robotProject.getProject().getLocation().toFile(), robotConfig.getEnvironmentVariables());
        final IRobotProcess robotProcess = (IRobotProcess) DebugPlugin.newProcess(launch, execProcess,
                consoleData.getProcessLabel());
        robotProcess.setInterruptionData(interpreter.getPath(), pidReader::getPid);

        robotProcess.onTerminate(serverJob::stopServer);

        final RobotConsoleFacade redConsole = robotProcess.provideConsoleFacade(consoleData.getProcessLabel());
        redConsole.addHyperlinksSupport(new RobotConsolePatternsListener(robotProject));
        redConsole.writeLine("Command: " + DebugPlugin.renderArguments(cmdLine.getCommandLine(), null));
        redConsole.writeLine("Suite Executor: " + consoleData.getSuiteExecutorVersion());

        return new LaunchExecution(serverJob, execProcess, robotProcess);
    }

    @VisibleForTesting
    static class ConsoleData {

        private final String processLabel;

        private final String suiteExecutorVersion;

        private ConsoleData(final String processLabel, final String suiteExecutorVersion) {
            this.processLabel = processLabel;
            this.suiteExecutorVersion = suiteExecutorVersion;
        }

        String getProcessLabel() {
            return processLabel;
        }

        String getSuiteExecutorVersion() {
            return suiteExecutorVersion;
        }

        static ConsoleData create(final RobotLaunchConfiguration robotConfig, final LocalProcessInterpreter interpreter)
                throws CoreException {
            if (robotConfig.getExecutableFilePath().isEmpty()) {
                return new ConsoleData(interpreter.getPath(), interpreter.getVersion());
            }
            return new ConsoleData(robotConfig.getExecutableFilePath(), "<unknown>");
        }
    }
}
