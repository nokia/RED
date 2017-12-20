/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.project.dryrun;

import java.io.File;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.rf.ide.core.dryrun.RobotDryRunAlwaysContinueEventListener;
import org.rf.ide.core.dryrun.RobotDryRunTemporarySuites;
import org.rf.ide.core.execution.agent.RobotAgentEventListener;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.rf.ide.core.execution.server.AgentServerTestsStarter;
import org.rf.ide.core.execution.server.AgentServerVersionsChecker;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author bembenek
 */
public abstract class AbstractAutoDiscoverer {

    private static final AtomicBoolean IS_DRY_RUN_RUNNING = new AtomicBoolean(false);

    private static final int CONNECTION_TIMEOUT = 120;

    final RobotProject robotProject;

    private AgentConnectionServerJob serverJob;

    AbstractAutoDiscoverer(final RobotProject robotProject) {
        this.robotProject = robotProject;
        if (robotProject.getRuntimeEnvironment() == null) {
            throw new AutoDiscovererException(
                    "There is no active runtime environment for project '" + robotProject.getName() + "'");
        }
    }

    public abstract Job start();

    abstract void startDiscovering(final IProgressMonitor monitor) throws InterruptedException, CoreException;

    abstract RobotAgentEventListener createDryRunCollectorEventListener(Consumer<String> libNameHandler);

    abstract void startDryRunClient(int port, String dataSourcePath) throws CoreException;

    final boolean lockDryRun() {
        return IS_DRY_RUN_RUNNING.compareAndSet(false, true);
    }

    final void unlockDryRun() {
        IS_DRY_RUN_RUNNING.set(false);
    }

    void stopDiscovering() {
        if (serverJob != null) {
            serverJob.stopServer();
            robotProject.getRuntimeEnvironment().stopLibraryAutoDiscovering();
        }
    }

    @VisibleForTesting
    public void startDryRunDiscovering(final IProgressMonitor monitor, final Set<String> libraryNames)
            throws InterruptedException, CoreException {
        final Optional<File> tempSuite = RobotDryRunTemporarySuites.createLibraryImportFile(libraryNames);
        if (tempSuite.isPresent()) {
            final SubMonitor subMonitor = SubMonitor.convert(monitor);
            subMonitor.setWorkRemaining(libraryNames.size() + 3);
            subMonitor.subTask("Preparing Robot dry run execution...");
            try {
                executeDryRun(tempSuite.get().getAbsolutePath(), subMonitor);
                subMonitor.worked(1);
            } finally {
                subMonitor.done();
            }
        }
    }

    private void executeDryRun(final String dataSourcePath, final SubMonitor subMonitor)
            throws InterruptedException, CoreException {
        final String host = AgentConnectionServer.DEFAULT_CONNECTION_HOST;
        final int port = AgentConnectionServer.findFreePort();
        final int timeout = CONNECTION_TIMEOUT;
        serverJob = startDryRunServer(host, port, timeout, subMonitor);

        startDryRunClient(port, dataSourcePath);

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
                .agentEventsListenedBy(createDryRunCollectorEventListener(libName -> {
                    subMonitor.worked(1);
                    subMonitor.subTask("Discovering library: " + libName);
                    if (subMonitor.isCanceled()) {
                        stopDiscovering();
                    }
                }))
                .agentEventsListenedBy(new AgentServerKeepAlive())
                .start()
                .waitForServer();

        testsStarter.allowClientTestsStart();

        return serverJob;
    }

    public static class AutoDiscovererException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public AutoDiscovererException(final String message) {
            super(message);
        }

        public AutoDiscovererException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

}
