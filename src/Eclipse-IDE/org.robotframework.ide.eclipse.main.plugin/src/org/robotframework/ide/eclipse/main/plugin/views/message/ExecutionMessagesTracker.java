/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ExecutionMessagesTracker extends RobotDefaultAgentEventListener {

    private final RobotTestsLaunch testsLaunchContext;

    public ExecutionMessagesTracker(final RobotTestsLaunch testsLaunchContext) {
        this.testsLaunchContext = testsLaunchContext;
    }

    @Override
    public void handleAgentInitializing(final AgentInitializingEvent event) {
        testsLaunchContext.getExecutionData(ExecutionMessagesStore.class, ExecutionMessagesStore::new);
    }

    @Override
    public void handleLogMessage(final MessageEvent event) {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class,
                store -> store.append(
                        event.getTimestamp() + " : " + event.getLevel().name() + " : " + event.getMessage() + "\n"));
    }

    @Override
    public void handleTestStarted(final TestStartedEvent event) {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class,
                store -> store.append("Starting test: " + event.getLongName() + '\n'));
    }

    @Override
    public void handleTestEnded(final TestEndedEvent event) {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class,
                store -> store.append("Ending test: " + event.getLongName() + "\n\n"));
    }

    @Override
    public void eventsProcessingAboutToStart() {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class, ExecutionMessagesStore::open);
    }

    @Override
    public void eventsProcessingFinished() {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class, ExecutionMessagesStore::close);
    }
}
