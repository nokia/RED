/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.rf.ide.core.dryrun.RobotDryRunAlwaysContinueEventListener;
import org.rf.ide.core.dryrun.RobotDryRunSuiteCountEventListener;
import org.rf.ide.core.execution.agent.RobotAgentEventListener;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.rf.ide.core.execution.server.AgentServerVersionsChecker;
import org.rf.ide.core.executor.EnvironmentSearchPaths;
import org.rf.ide.core.executor.RobotRuntimeEnvironment;
import org.rf.ide.core.testdata.model.table.variables.names.VariableNamesSupport;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

/**
 * @author bembenek
 */
public abstract class AbstractAutoDiscoverer {

    private static final AtomicBoolean IS_DRY_RUN_RUNNING = new AtomicBoolean(false);

    private static final int CONNECTION_TIMEOUT = 120;

    final RobotProject robotProject;

    private final IDryRunTargetsCollector dryRunTargetsCollector;

    private AgentConnectionServerJob serverJob;

    AbstractAutoDiscoverer(final RobotProject robotProject, final IDryRunTargetsCollector dryRunTargetsCollector) {
        this.robotProject = robotProject;
        this.dryRunTargetsCollector = dryRunTargetsCollector;
    }

    public abstract Job start();

    abstract EnvironmentSearchPaths collectLibrarySources(RobotRuntimeEnvironment runtimeEnvironment)
            throws CoreException;

    abstract RobotAgentEventListener createDryRunCollectorEventListener(Consumer<String> startSuiteHandler);

    final boolean lockDryRun() {
        return IS_DRY_RUN_RUNNING.compareAndSet(false, true);
    }

    final void unlockDryRun() {
        IS_DRY_RUN_RUNNING.set(false);
    }

    void stopDiscovering() {
        if (serverJob != null) {
            serverJob.stopServer();
            final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
            if (runtimeEnvironment != null) {
                runtimeEnvironment.stopLibraryAutoDiscovering();
            }
        }
    }

    void startDiscovering(final IProgressMonitor monitor) throws CoreException, InterruptedException {
        final RobotRuntimeEnvironment runtimeEnvironment = robotProject.getRuntimeEnvironment();
        if (runtimeEnvironment == null) {
            throw newCoreException(
                    "There is no active runtime environment for project '" + robotProject.getName() + "'");
        }

        final SubMonitor subMonitor = SubMonitor.convert(monitor);
        subMonitor.setWorkRemaining(10);

        subMonitor.subTask("Preparing Robot dry run execution...");
        final EnvironmentSearchPaths librarySourcePaths = collectLibrarySources(runtimeEnvironment);
        dryRunTargetsCollector.collectSuiteNamesAndDataSourcePaths(robotProject);
        subMonitor.worked(1);

        try {
            if (!monitor.isCanceled()) {
                executeDryRun(runtimeEnvironment, librarySourcePaths, subMonitor);
            }
            subMonitor.worked(1);
        } finally {
            subMonitor.done();
        }
    }

    private void executeDryRun(final RobotRuntimeEnvironment runtimeEnvironment,
            final EnvironmentSearchPaths librarySourcePaths, final SubMonitor subMonitor)
            throws InterruptedException, CoreException {
        final String host = AgentConnectionServer.DEFAULT_CONNECTION_HOST;
        final int port = AgentConnectionServer.findFreePort();
        final int timeout = CONNECTION_TIMEOUT;
        serverJob = startDryRunServer(host, port, timeout, subMonitor);

        subMonitor.subTask("Connecting to Robot dry run process...");
        startDryRunClient(runtimeEnvironment, port, librarySourcePaths);

        serverJob.join();
    }

    private AgentConnectionServerJob startDryRunServer(final String host, final int port, final int timeout,
            final SubMonitor subMonitor) throws InterruptedException {
        final AgentServerTestsStarter testsStarter = new AgentServerTestsStarter(TestsMode.RUN) {

            @Override
            public void handleAgentInitializing(final AgentInitializingEvent event) {
                super.handleAgentInitializing(event);
                subMonitor.worked(1);
                subMonitor.subTask("Starting Robot dry run execution...");
            }
        };
        final AgentConnectionServerJob serverJob = AgentConnectionServerJob.setupServerAt(host, port)
                .withConnectionTimeout(timeout, TimeUnit.SECONDS)
                .agentEventsListenedBy(new AgentServerVersionsChecker())
                .agentEventsListenedBy(testsStarter)
                .agentEventsListenedBy(new RobotDryRunAlwaysContinueEventListener())
                .agentEventsListenedBy(new RobotDryRunSuiteCountEventListener(suiteCount -> {
                    subMonitor.setWorkRemaining(suiteCount);
                }))
                .agentEventsListenedBy(createDryRunCollectorEventListener(suiteName -> {
                    subMonitor.worked(1);
                    subMonitor.subTask("Executing Robot dry run on suite: " + suiteName);
                }))
                .agentEventsListenedBy(new AgentServerKeepAlive())
                .start()
                .waitForServer();

        testsStarter.allowClientTestsStart();

        return serverJob;
    }

    private void startDryRunClient(final RobotRuntimeEnvironment runtimeEnvironment, final int port,
            final EnvironmentSearchPaths librarySourcePaths) throws CoreException {
        final List<String> variableMapping = robotProject.getRobotProjectHolder()
                .getVariableMappings()
                .entrySet()
                .stream()
                .map(e -> VariableNamesSupport.extractUnifiedVariableNameWithoutBrackets(e.getKey()) + ":"
                        + e.getValue())
                .collect(Collectors.toList());
        runtimeEnvironment.startLibraryAutoDiscovering(port, dryRunTargetsCollector.getSuiteNames(), variableMapping,
                dryRunTargetsCollector.getDataSourcePaths(), librarySourcePaths);
    }

    public interface IDryRunTargetsCollector {

        void collectSuiteNamesAndDataSourcePaths(RobotProject robotProject);

        List<String> getSuiteNames();

        List<String> getDataSourcePaths();
    }

    public static class AutoDiscovererException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public AutoDiscovererException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

}
