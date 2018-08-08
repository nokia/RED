/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.message;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent.ExecutionMode;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ExecutionMessagesTracker extends RobotDefaultAgentEventListener {

    private final RobotTestsLaunch testsLaunchContext;

    private ExecutionMode mode;

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
    public void handleSuiteStarted(final SuiteStartedEvent event) {
        this.mode = event.getMode();
    }

    @Override
    public void handleTestStarted(final TestStartedEvent event) {
        final String caseLabel = this.mode == ExecutionMode.TASKS ? "task" : "test";
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class,
                store -> store.append("Starting " + caseLabel + ": " + event.getLongName() + '\n'));
    }

    @Override
    public void handleTestEnded(final TestEndedEvent event) {
        final String caseLabel = this.mode == ExecutionMode.TASKS ? "task" : "test";
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class,
                store -> store.append("Ending " + caseLabel + ": " + event.getLongName() + "\n\n"));
    }

    @Override
    public void eventsProcessingAboutToStart() {
        synchronized (testsLaunchContext) {
            testsLaunchContext.getExecutionData(ExecutionMessagesStore.class, ExecutionMessagesStore::new).open();
        }
    }

    @Override
    public void eventsProcessingFinished() {
        testsLaunchContext.performOnExecutionData(ExecutionMessagesStore.class, ExecutionMessagesStore::close);
    }
}
