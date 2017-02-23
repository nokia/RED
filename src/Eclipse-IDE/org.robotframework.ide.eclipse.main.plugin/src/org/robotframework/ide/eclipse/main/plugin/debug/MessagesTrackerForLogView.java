/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.PlatformUI;
import org.rf.ide.core.execution.LogLevel;
import org.rf.ide.core.execution.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.Status;

public class MessagesTrackerForLogView extends RobotDefaultAgentEventListener {

    private final IEventBroker broker;

    private static StringBuilder messageLogViewContent = new StringBuilder();

    public MessagesTrackerForLogView() {
        this.broker = PlatformUI.getWorkbench().getService(IEventBroker.class);
    }

    @Override
    public void handlePid() {
        sendClearEventToMessageLogView();
    }

    @Override
    public void handleLogMessage(final String msg, final LogLevel level, final String timestamp) {
        sendAppendLineEventToMessageLogView(timestamp + " : " + level.name() + " : " + msg + '\n');
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        sendAppendLineEventToMessageLogView("Ending test: " + testCaseLongName + "\n\n");
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        sendAppendLineEventToMessageLogView("Starting test: " + testCaseLongName + '\n');
    }

    public void sendAppendLineEventToMessageLogView(final String line) {
        broker.send("MessageLogView/AppendLine", line);
        messageLogViewContent.append(line);
    }

    public void sendClearEventToMessageLogView() {
        broker.send("MessageLogView/Clear", "");
        if (messageLogViewContent.length() > 0) {
            messageLogViewContent.setLength(0);
        }
    }

    public static String getMessageLogViewContent() {
        return messageLogViewContent.toString();
    }
}
