/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import org.rf.ide.core.execution.LogLevel;
import org.rf.ide.core.execution.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;

public class MessagesTrackerForLogView extends RobotDefaultAgentEventListener {

    private final RobotEventBroker robotEventBroker;

    public MessagesTrackerForLogView(final RobotEventBroker robotEventBroker) {
        this.robotEventBroker = robotEventBroker;
    }

    @Override
    public void handlePid() {
        robotEventBroker.sendClearEventToMessageLogView();
    }

    @Override
    public void handleLogMessage(final String msg, final LogLevel level, final String timestamp) {
        robotEventBroker.sendAppendLineEventToMessageLogView(timestamp + " : " + level.name() + " : " + msg + '\n');
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        robotEventBroker.sendAppendLineEventToMessageLogView("Ending test: " + testCaseLongName + "\n\n");
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        robotEventBroker.sendAppendLineEventToMessageLogView("Starting test: " + testCaseLongName + '\n');
    }
}
