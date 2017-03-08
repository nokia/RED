/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugException;
import org.robotframework.ide.eclipse.main.plugin.launch.AgentConnectionServerJob;
import org.robotframework.ide.eclipse.main.plugin.launch.IRobotProcess;

class TestsExecutionTerminationSupport {

    static void installTerminationSupport(final AgentConnectionServerJob serverJob,
            final IRobotProcess robotProcess) {

        serverJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(final IJobChangeEvent event) {
                try {
                    robotProcess.terminate();
                } catch (final DebugException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
