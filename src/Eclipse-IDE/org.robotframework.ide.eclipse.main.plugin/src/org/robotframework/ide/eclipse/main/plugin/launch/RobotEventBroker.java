/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.rf.ide.core.execution.ExecutionElement;

public class RobotEventBroker {

    private final IEventBroker broker;
    
    private static StringBuilder messageLogViewContent = new StringBuilder();
    private static List<ExecutionElement> executionViewContent = Collections.synchronizedList(new ArrayList<ExecutionElement>());

    public RobotEventBroker(final IEventBroker broker) {
        this.broker = broker;
    }

    public void sendAppendLineEventToMessageLogView(final String line) {
        broker.send("MessageLogView/AppendLine", line);
        messageLogViewContent.append(line);
    }

    public void sendClearEventToMessageLogView() {
        broker.send("MessageLogView/Clear", "");
        if(messageLogViewContent.length() > 0) {
            messageLogViewContent.setLength(0);
        }
    }

    public void sendExecutionEventToExecutionView(final ExecutionElement executionElement) {
        broker.send("ExecutionView/ExecutionEvent", executionElement);
        executionViewContent.add(executionElement);
    }
    
    public void sendClearEventToExecutionView() {
        broker.send("ExecutionView/ClearEvent", "");
        executionViewContent.clear();
    }
    
    public static String getMessageLogViewContent() {
        return messageLogViewContent.toString();
    }

    public static List<ExecutionElement> getExecutionViewContent() {
        return executionViewContent;
    }
}
