/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.core.execution.ExecutionElement;

public class RobotEventBroker {

    private final IEventBroker broker;

    public RobotEventBroker(final IEventBroker broker) {
        this.broker = broker;
    }

    public void sendAppendLineEventToMessageLogView(final String line) {
        broker.send("MessageLogView/AppendLine", line);
    }

    public void sendClearEventToMessageLogView() {
        broker.send("MessageLogView/Clear", "");
    }

    public void sendExecutionEventToExecutionView(final ExecutionElement executionElement) {
        broker.send("ExecutionView/ExecutionEvent", executionElement);
    }

    public void sendClearEventToExecutionView() {
        broker.send("ExecutionView/ClearEvent", "");
    }
}
