/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.agent.event.OutputFileEvent;
import org.rf.ide.core.execution.agent.event.SuiteEndedEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;


public class ExecutionStatusTracker extends RobotDefaultAgentEventListener {

    private final RobotTestsLaunch testsLaunchContext;

    public ExecutionStatusTracker(final RobotTestsLaunch testsLaunchContext) {
        this.testsLaunchContext = testsLaunchContext;
    }

    @Override
    public void handleAgentInitializing(final AgentInitializingEvent event) {
        testsLaunchContext.getExecutionData(ExecutionStatusStore.class, ExecutionStatusStore::new);
    }

    @Override
    public void handleSuiteStarted(final SuiteStartedEvent event) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.suiteStarted(event.getName(), event.getPath(), event.getNumberOfTests(),
                        event.getChildrenSuites(), event.getChildrenTests()));
    }

    @Override
    public void handleSuiteEnded(final SuiteEndedEvent event) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.elementEnded(event.getElapsedTime(), event.getStatus(), event.getErrorMessage()));
    }

    @Override
    public void handleTestStarted(final TestStartedEvent event) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.testStarted());
    }

    @Override
    public void handleTestEnded(final TestEndedEvent event) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.elementEnded(event.getElapsedTime(), event.getStatus(), event.getErrorMessage()));
    }

    @Override
    public void handleOutputFile(final OutputFileEvent event) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.setOutputFilePath(event.getPath().orElse(null)));
    }

    @Override
    public void eventsProcessingAboutToStart() {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class, ExecutionStatusStore::open);
    }

    @Override
    public void eventsProcessingFinished() {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class, ExecutionStatusStore::close);
    }
}
