/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.agent.event.OutputFileEvent;
import org.rf.ide.core.execution.agent.event.SuiteEndedEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;

public class ExecutionStatusTrackerTest {

    @Test
    public void newStoreIsCreated_onAgentInitalization() {
        final RobotTestsLaunch context = new RobotTestsLaunch(null);
        final ExecutionStatusTracker tracker = new ExecutionStatusTracker(context);

        assertThat(context.getExecutionData(ExecutionStatusStore.class)).isEqualTo(Optional.empty());

        tracker.handleAgentInitializing(new AgentInitializingEvent(null));

        final Optional<ExecutionStatusStore> optionalStore = context.getExecutionData(ExecutionStatusStore.class);
        assertThat(optionalStore.isPresent()).isTrue();

        final ExecutionStatusStore store = optionalStore.get();

        for (int i = 0; i < 10; i++) {
            assertThat(context.getExecutionData(ExecutionStatusStore.class).get()).isSameAs(store);
        }
    }

    @Test
    public void noStoreIsCreated_onAgentInitalization_whenThereIsAStoreAlready() {
        final RobotTestsLaunch context = new RobotTestsLaunch(null);
        final ExecutionStatusStore existingStore = context.getExecutionData(ExecutionStatusStore.class,
                ExecutionStatusStore::new);

        final ExecutionStatusTracker tracker = new ExecutionStatusTracker(context);

        tracker.handleAgentInitializing(new AgentInitializingEvent(null));

        for (int i = 0; i < 10; i++) {
            assertThat(context.getExecutionData(ExecutionStatusStore.class).get()).isSameAs(existingStore);
        }
    }

    @Test
    public void storeIsNotified_whenSuiteStarted() throws Exception {
        final ExecutionStatusStore store = mock(ExecutionStatusStore.class);

        final RobotTestsLaunch context = new RobotTestsLaunch(null);
        context.getExecutionData(ExecutionStatusStore.class, () -> store);

        final ExecutionStatusTracker tracker = new ExecutionStatusTracker(context);
        tracker.handleSuiteStarted(new SuiteStartedEvent("suite", new URI("file:///suite.robot"), false, 2,
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        verify(store).suiteStarted("suite", new URI("file:///suite.robot"), 2, new ArrayList<>(), new ArrayList<>());
        verifyNoMoreInteractions(store);
    }

    @Test
    public void storeIsNotified_whenSuiteEnded() throws Exception {
        final ExecutionStatusStore store = mock(ExecutionStatusStore.class);

        final RobotTestsLaunch context = new RobotTestsLaunch(null);
        context.getExecutionData(ExecutionStatusStore.class, () -> store);

        final ExecutionStatusTracker tracker = new ExecutionStatusTracker(context);
        tracker.handleSuiteEnded(new SuiteEndedEvent("suite", 100, Status.PASS, ""));

        verify(store).elementEnded(100, Status.PASS, "");
        verifyNoMoreInteractions(store);
    }

    @Test
    public void storeIsNotified_whenTestStarted() throws Exception {
        final ExecutionStatusStore store = mock(ExecutionStatusStore.class);

        final RobotTestsLaunch context = new RobotTestsLaunch(null);
        context.getExecutionData(ExecutionStatusStore.class, () -> store);

        final ExecutionStatusTracker tracker = new ExecutionStatusTracker(context);
        tracker.handleTestStarted(new TestStartedEvent("test", "teeeeest", null, new ArrayList<>()));

        verify(store).testStarted();
        verifyNoMoreInteractions(store);
    }

    @Test
    public void storeIsNotified_whenTestEnded() throws Exception {
        final ExecutionStatusStore store = mock(ExecutionStatusStore.class);

        final RobotTestsLaunch context = new RobotTestsLaunch(null);
        context.getExecutionData(ExecutionStatusStore.class, () -> store);

        final ExecutionStatusTracker tracker = new ExecutionStatusTracker(context);
        tracker.handleTestEnded(new TestEndedEvent("test", "teeeeest", 100, Status.FAIL, "error"));

        verify(store).elementEnded(100, Status.FAIL, "error");
        verifyNoMoreInteractions(store);
    }

    @Test
    public void storeIsNotified_whenOutputFileIsGenerated() throws Exception {
        final ExecutionStatusStore store = mock(ExecutionStatusStore.class);

        final RobotTestsLaunch context = new RobotTestsLaunch(null);
        context.getExecutionData(ExecutionStatusStore.class, () -> store);

        final ExecutionStatusTracker tracker = new ExecutionStatusTracker(context);
        tracker.handleOutputFile(new OutputFileEvent(new URI("file:///output.xml")));

        verify(store).setOutputFilePath(new URI("file:///output.xml"));
        verifyNoMoreInteractions(store);

    }
}
