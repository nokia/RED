/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.local;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
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
import org.rf.ide.core.execution.server.TestsPidReader;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.executor.RobotRuntimeEnvironment.RobotEnvironmentException;
import org.rf.ide.core.executor.RunCommandLineCallBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.IRunCommandLineBuilder;
import org.rf.ide.core.executor.RunCommandLineCallBuilder.RunCommandLine;
import org.rf.ide.core.executor.SuiteExecutor;
import org.rf.ide.core.project.RobotProjectConfig;
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
import org.robotframework.ide.eclipse.main.plugin.project.RedEclipseProjectConfig;
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

                final ArrayList<AgentServerStatusListener> additionalServerListeners = newArrayList(
                        new ProcessConnectingInRunServerListener(launch));

                final List<RobotAgentEventListener> additionalAgentListeners = new ArrayList<>();
                additionalAgentListeners.add(new AgentServerVersionsChecker());
                additionalAgentListeners.add(testsStarter);

                launchExecution = doLaunch(robotConfig, launch, testsLaunchContext, host, port, timeout, userController,
                        additionalServerListeners, additionalAgentListeners);
                testsStarter.allowClientTestsStart();
            } else {
                final Stacktrace stacktrace = new Stacktrace();
                final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
                userController = new UserProcessDebugController(stacktrace,
                        new DebuggerPreferences(new DebuggerErrorDecider(preferences),
                                !preferences.shouldDebuggerOmitLibraryKeywords()));

                final RobotDebugTarget debugTarget = new RobotDebugTarget("Robot Test at " + host + ":" + port,
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
        final ConsoleData consoleData = ConsoleData.create(robotConfig, robotProject);

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

        final RunCommandLine cmdLine = prepareCommandLine(robotConfig, robotProject, port,
                RedPlugin.getDefault().getPreferences());
        final Process execProcess = DebugPlugin.exec(cmdLine.getCommandLine(),
                robotProject.getProject().getLocation().toFile(), robotConfig.getEnvironmentVariables());
        final IRobotProcess robotProcess = (IRobotProcess) DebugPlugin.newProcess(launch, execProcess,
                consoleData.getProcessLabel());
        final String projectInterpreterPath = robotProject.getRuntimeEnvironment() != null
                ? robotProject.getRuntimeEnvironment().getPythonExecutablePath()
                : null;
        robotProcess.setInterruptionData(projectInterpreterPath, pidReader::getPid);

        robotProcess.onTerminate(serverJob::stopServer);

        final RobotConsoleFacade redConsole = robotProcess.provideConsoleFacade(consoleData.getProcessLabel());
        redConsole.addHyperlinksSupport(new RobotConsolePatternsListener(robotProject));
        redConsole.writeLine("Command: " + DebugPlugin.renderArguments(cmdLine.getCommandLine(), null));
        redConsole.writeLine("Suite Executor: " + consoleData.getSuiteExecutorVersion());

        return new LaunchExecution(serverJob, execProcess, robotProcess);
    }

    @VisibleForTesting
    RunCommandLine prepareCommandLine(final RobotLaunchConfiguration robotConfig, final RobotProject robotProject,
            final int port, final RedPreferences preferences) throws CoreException, IOException {

        final IRunCommandLineBuilder builder;
        if (robotConfig.isUsingInterpreterFromProject()) {
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            if (runtimeEnvironment != null) {
                builder = RunCommandLineCallBuilder.forEnvironment(runtimeEnvironment, port);
            } else {
                builder = RunCommandLineCallBuilder.forDefault(port);
            }
        } else {
            builder = RunCommandLineCallBuilder.forExecutor(robotConfig.getInterpreter(), port);
        }

        builder.useArgumentFile(preferences.shouldLaunchUsingArgumentsFile());
        if (!robotConfig.getExecutableFilePath().isEmpty()) {
            builder.withExecutableFile(resolveExecutableFile(robotConfig.getExecutableFilePath()));
            builder.addUserArgumentsForExecutableFile(parseArguments(robotConfig.getExecutableFileArguments()));
            builder.useSingleRobotCommandLineArg(preferences.shouldUseSingleCommandLineArgument());
        }
        builder.addUserArgumentsForInterpreter(parseArguments(robotConfig.getInterpreterArguments()));
        builder.addUserArgumentsForRobot(parseArguments(robotConfig.getRobotArguments()));

        final RobotProjectConfig projectConfig = robotProject.getRobotProjectConfig();
        if (projectConfig != null) {
            final RedEclipseProjectConfig redConfig = new RedEclipseProjectConfig(robotConfig.getProject(),
                    projectConfig);
            final EnvironmentSearchPaths searchPaths = redConfig.createExecutionEnvironmentSearchPaths();
            builder.addLocationsToClassPath(searchPaths.getClassPaths());
            builder.addLocationsToPythonPath(searchPaths.getPythonPaths());
            builder.addVariableFiles(redConfig.getVariableFilePaths());
        }

        if (shouldUseSingleTestPathInCommandLine(robotConfig, preferences)) {
            builder.withProject(robotConfig.getSuiteResources().get(0).getLocation().toFile());
            builder.testsToRun(robotConfig.getSuitePaths().values().iterator().next());
        } else {
            builder.withProject(robotProject.getProject().getLocation().toFile());
            builder.suitesToRun(robotConfig.getSuitesToRun());
            builder.testsToRun(robotConfig.getTestsToRun());
        }

        if (robotConfig.isIncludeTagsEnabled()) {
            builder.includeTags(robotConfig.getIncludedTags());
        }
        if (robotConfig.isExcludeTagsEnabled()) {
            builder.excludeTags(robotConfig.getExcludedTags());
        }
        return builder.build();
    }

    private boolean shouldUseSingleTestPathInCommandLine(final RobotLaunchConfiguration robotConfig,
            final RedPreferences preferences) throws CoreException {
        // FIXME temporary fix for https://github.com/robotframework/robotframework/issues/2564
        return preferences.shouldUseSingleFileDataSource() && robotConfig.getSuiteResources().size() == 1
                && robotConfig.getSuiteResources().get(0) instanceof IFile;
    }

    private File resolveExecutableFile(final String path) throws CoreException {
        final IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
        final File executableFile = new File(variableManager.performStringSubstitution(path));
        if (!executableFile.exists()) {
            throw newCoreException("Executable file '" + executableFile.getAbsolutePath() + "' does not exist");
        }
        return executableFile;
    }

    private List<String> parseArguments(final String arguments) {
        final IStringVariableManager variableManager = VariablesPlugin.getDefault().getStringVariableManager();
        return Stream.of(DebugPlugin.parseArguments(arguments)).map(argument -> {
            try {
                return variableManager.performStringSubstitution(argument);
            } catch (final CoreException e) {
                return argument;
            }
        }).collect(toList());
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

        static ConsoleData create(final RobotLaunchConfiguration robotConfig, final RobotProject robotProject)
                throws CoreException {
            if (robotConfig.getExecutableFilePath().isEmpty()) {
                if (robotConfig.isUsingInterpreterFromProject()) {
                    return ConsoleData.create(robotProject.getRuntimeEnvironment(), robotProject.getName());
                }
                return ConsoleData.create(robotConfig.getInterpreter());
            }
            return new ConsoleData(robotConfig.getExecutableFilePath(), "<unknown>");
        }

        private static ConsoleData create(final RobotRuntimeEnvironment env, final String projectName)
                throws CoreException {
            if (env == null) {
                throw newCoreException("There is no active runtime environment for project '" + projectName + "'");
            }
            if (!env.hasRobotInstalled()) {
                throw newCoreException("The runtime environment " + env.getFile().getAbsolutePath()
                        + " is either not a python installation or it has no Robot installed");
            }
            return new ConsoleData(env.getPythonExecutablePath(), env.getVersion());
        }

        private static ConsoleData create(final SuiteExecutor interpreter) throws CoreException {
            try {
                return new ConsoleData(interpreter.executableName(), RobotRuntimeEnvironment.getVersion(interpreter));
            } catch (final RobotEnvironmentException e) {
                throw newCoreException(e.getMessage(), e.getCause());
            }
        }

    }
}
