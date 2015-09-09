/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.robotframework.ide.core.execution.ExecutionElement;

public class RobotEventBroker {

    private IEventBroker broker;

    public RobotEventBroker(final IEventBroker broker) {
        this.broker = broker;
    }

    public void sendHighlightLineEventToTextEditor(final String file, final int line,
            final Map<String, Object> variables) {

        final Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("file", file);
        eventMap.put("line", String.valueOf(line));
        eventMap.put("vars", variables);
        broker.send("TextEditor/HighlightLine", eventMap);
    }

    public void sendClearEventToTextEditor(final String file) {

        broker.send("TextEditor/ClearHighlightedLine", file);
    }

    public void sendClearAllEventToTextEditor() {

        broker.send("TextEditor/ClearHighlightedLine", "");
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
