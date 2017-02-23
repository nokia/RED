/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import java.io.IOException;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.rf.ide.core.execution.server.AgentServerKeepAlive;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;

class RemoteExecutionTerminationSupport {

    static void installTerminationSupport(final AgentConnectionServerJob serverJob,
            final AgentServerKeepAlive keepAliveListener, final IRobotProcess robotProcess) {

        DebugPlugin.getDefault().addDebugEventListener(new DebugTerminationListener(serverJob, keepAliveListener, robotProcess));
        serverJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(final IJobChangeEvent event) {
                try {
                    robotProcess.terminate();
                } catch (final DebugException e) {
                    // FIXME : to be handled
                    e.printStackTrace();
                }
            }
        });
    }

    public static class DebugTerminationListener implements IDebugEventSetListener {

        private final AgentConnectionServerJob serverJob;

        private final AgentServerKeepAlive keepAliveListener;

        private final IRobotProcess robotProcess;

        public DebugTerminationListener(final AgentConnectionServerJob serverJob, final AgentServerKeepAlive keepAliveListener,
                final IRobotProcess robotProcess) {
            this.serverJob = serverJob;
            this.keepAliveListener = keepAliveListener;
            this.robotProcess = robotProcess;
        }

        @Override
        public void handleDebugEvents(final DebugEvent[] events) {
            for (final DebugEvent event : events) {
                if (event.getSource() == robotProcess && event.getKind() == DebugEvent.TERMINATE) {
                    keepAliveListener.stopHandlingEvents();
                    try {
                        serverJob.stopServer();
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                    DebugPlugin.getDefault().removeDebugEventListener(this);
                    return;
                }
            }
        }
    }
}
