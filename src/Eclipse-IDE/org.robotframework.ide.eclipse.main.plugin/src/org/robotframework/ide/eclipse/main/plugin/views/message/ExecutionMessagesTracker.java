/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import org.rf.ide.core.execution.LogLevel;
import org.rf.ide.core.execution.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ExecutionMessagesTracker extends RobotDefaultAgentEventListener {

    private final RobotTestsLaunch testsLaunchContext;

    public ExecutionMessagesTracker(final RobotTestsLaunch testsLaunchContext) {
        this.testsLaunchContext = testsLaunchContext;
    }

    @Override
    public void handleAgentInitializing() {
        testsLaunchContext.getExecutionData(ExecutionMessagesStore.class, ExecutionMessagesStore::new);
    }

    @Override
    public void handleLogMessage(final String msg, final LogLevel level, final String timestamp) {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class,
                store -> store.append(timestamp + " : " + level.name() + " : " + msg + "\n"));
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class,
                store -> store.append("Starting test: " + testCaseLongName + '\n'));
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class,
                store -> store.append("Ending test: " + testCaseLongName + "\n\n"));
    }
}
