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
    
    private static List<ExecutionElement> executionViewContent = Collections.synchronizedList(new ArrayList<ExecutionElement>());

    public RobotEventBroker(final IEventBroker broker) {
        this.broker = broker;
    }

    public void sendExecutionEventToExecutionView(final ExecutionElement executionElement) {
        broker.send("ExecutionView/ExecutionEvent", executionElement);
        executionViewContent.add(executionElement);
    }
    
    public void sendClearEventToExecutionView() {
        broker.send("ExecutionView/ClearEvent", "");
        executionViewContent.clear();
    }

    public static List<ExecutionElement> getExecutionViewContent() {
        return executionViewContent;
    }
}
