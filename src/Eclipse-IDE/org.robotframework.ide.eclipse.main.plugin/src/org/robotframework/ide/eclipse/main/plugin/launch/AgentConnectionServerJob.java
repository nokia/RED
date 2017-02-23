/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.rf.ide.core.execution.RobotAgentEventListener;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.rf.ide.core.execution.server.AgentServerStatusListener;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class AgentConnectionServerJob extends Job {

    private final String host;

    private final int port;

    private final int timeout;

    private final TimeUnit timeoutUnit;

    private final List<AgentServerStatusListener> serverListeners;

    private final List<RobotAgentEventListener> agentEventListeners;

    private AgentConnectionServer agentServer;

    private AgentConnectionServerJob(final String host, final int port, final int timeout, final TimeUnit timeoutUnit,
            final List<AgentServerStatusListener> serverStatusListeners,
            final List<RobotAgentEventListener> agentEventListeners) {
        super("Agent connection server");
        setSystem(true);

        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.serverListeners = serverStatusListeners;
        this.agentEventListeners = agentEventListeners;
    }

    public static AgentConnectionServerJobBuilder setupServerAt(final String host, final int port) {
        return new AgentConnectionServerJobBuilder(host, port);
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        try {
            agentServer = new AgentConnectionServer(host, port, timeout,
                    timeoutUnit);
            for (final AgentServerStatusListener serverStatusListener : serverListeners) {
                agentServer.addStatusListener(serverStatusListener);
            }
            agentServer.start(agentEventListeners.toArray(new RobotAgentEventListener[0]));
            return Status.OK_STATUS;
        } catch (final IOException e) {
            return new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, "Unable to start server for remote execution", e);
        }
    }

    public AgentConnectionServerJob waitForServer() throws InterruptedException {
        while (agentServer == null) {
            Thread.sleep(500);
        }
        agentServer.waitForServerToSetup();
        return this;
    }

    public void stopServer() throws IOException {
        if (agentServer != null) {
            agentServer.stop();
        }
    }

    public static class AgentConnectionServerJobBuilder {

        private final String host;

        private final int port;

        private int timeout;

        private TimeUnit timeoutUnit;

        private final List<AgentServerStatusListener> serverListeners = new ArrayList<>();

        private final List<RobotAgentEventListener> agentEventListeners = new ArrayList<>();

        public AgentConnectionServerJobBuilder(final String host, final int port) {
            this.host = host;
            this.port = port;
            this.timeout = AgentConnectionServer.CLIENT_CONNECTION_TIMEOUT;
            this.timeoutUnit = TimeUnit.MILLISECONDS;
        }

        public AgentConnectionServerJobBuilder withConnectionTimeout(final int timeout, final TimeUnit timeoutUnit) {
            this.timeout = timeout;
            this.timeoutUnit = timeoutUnit;
            return this;
        }

        public AgentConnectionServerJobBuilder serverStatusHandledBy(final AgentServerStatusListener listener) {
            serverListeners.add(listener);
            return this;
        }

        public AgentConnectionServerJobBuilder agentEventsListenedBy(final RobotAgentEventListener listener) {
            agentEventListeners.add(listener);
            return this;
        }

        public AgentConnectionServerJob createJob() {
            return new AgentConnectionServerJob(host, port, timeout, timeoutUnit, serverListeners, agentEventListeners);
        }

        public AgentConnectionServerJob start() {
            final AgentConnectionServerJob job = createJob();
            job.schedule();
            return job;
        }
    }
}
