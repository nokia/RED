/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.robotframework.ide.eclipse.main.plugin.debug.model.RobotDebugTarget;

public class TestsExecutionTerminationSupport {

    public static void installTerminationSupport(final AgentConnectionServerJob serverJob,
            final IRobotProcess robotProcess) {

        serverJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(final IJobChangeEvent event) {
                robotProcess.terminated();
            }
        });
    }

    public static void installTerminationSupport(final AgentConnectionServerJob serverJob,
            final RobotDebugTarget debugTarget) {

        serverJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(final IJobChangeEvent event) {
                debugTarget.fireTerminateEvent();
            }
        });
    }
}
