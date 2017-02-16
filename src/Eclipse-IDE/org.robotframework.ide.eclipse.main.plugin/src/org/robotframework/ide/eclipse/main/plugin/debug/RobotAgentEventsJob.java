/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import java.io.BufferedReader;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.rf.ide.core.execution.RobotAgentEventDispatcher;
import org.rf.ide.core.execution.RobotAgentEventListener;
import org.rf.ide.core.execution.RobotAgentEventListener.RobotAgentEventsListenerException;

import com.google.common.base.Supplier;


public class RobotAgentEventsJob extends Job {

    private final RobotAgentEventListener[] eventsListeners;

    private final Supplier<BufferedReader> eventsReader;

    public RobotAgentEventsJob(final Supplier<BufferedReader> eventsReader,
            final RobotAgentEventListener... eventsListeners) {
        super("Robot Event Dispatcher");
        this.eventsListeners = eventsListeners;
        this.eventsReader = eventsReader;
        setSystem(true);
    }

    @Override
    protected IStatus run(final IProgressMonitor monitor) {
        try {
            final RobotAgentEventDispatcher dispatcher = new RobotAgentEventDispatcher(eventsListeners);
            final BufferedReader eventReader = eventsReader.get();
            if (eventReader != null) {
                dispatcher.runEventsLoop(eventReader);
            }
        } catch (final IOException | RobotAgentEventsListenerException e) {
            for (final RobotAgentEventListener listener : eventsListeners) {
                listener.terminated();
            }
        }
        return Status.OK_STATUS;
    }
}
