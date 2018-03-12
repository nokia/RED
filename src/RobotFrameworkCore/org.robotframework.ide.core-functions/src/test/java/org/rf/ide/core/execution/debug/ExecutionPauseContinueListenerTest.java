/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.FutureTask;

import org.junit.Test;
import org.rf.ide.core.execution.agent.PausingPoint;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.agent.event.ConditionEvaluatedEvent;
import org.rf.ide.core.execution.agent.event.PausedEvent;
import org.rf.ide.core.execution.agent.event.PausedEvent.PausedEventResponder;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent.ShouldContinueEventResponder;
import org.rf.ide.core.execution.server.response.ContinueExecution;
import org.rf.ide.core.execution.server.response.PauseExecution;
import org.rf.ide.core.execution.server.response.ResumeExecution;
import org.rf.ide.core.execution.server.response.ServerResponse;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

public class ExecutionPauseContinueListenerTest {

    @Test
    public void listenerRespondsWithControllerResponse_whenAskedForContinuation() {
        final PauseExecution controllerResponse = new PauseExecution();
        
        final UserProcessController controller = mock(UserProcessController.class);
        when(controller.takeCurrentResponse(PausingPoint.START_KEYWORD)).thenReturn(Optional.of(controllerResponse));

        final ShouldContinueEventResponder responder = mock(ShouldContinueEventResponder.class);

        final ShouldContinueEvent event = new ShouldContinueEvent(responder, PausingPoint.START_KEYWORD);
        final ExecutionPauseContinueListener listener = new ExecutionPauseContinueListener(controller);
        listener.handleShouldContinue(event);

        verify(responder).respond(controllerResponse);
        verifyNoMoreInteractions(responder);
    }

    @Test
    public void listenerRespondsWithContinueResponse_whenControllerHasNoResponse() {
        final UserProcessController controller = mock(UserProcessController.class);
        when(controller.takeCurrentResponse(PausingPoint.START_KEYWORD)).thenReturn(Optional.empty());

        final ShouldContinueEventResponder responder = mock(ShouldContinueEventResponder.class);

        final ShouldContinueEvent event = new ShouldContinueEvent(responder, PausingPoint.START_KEYWORD);
        final ExecutionPauseContinueListener listener = new ExecutionPauseContinueListener(controller);
        listener.handleShouldContinue(event);

        verify(responder).respond(any(ContinueExecution.class));
        verifyNoMoreInteractions(responder);
    }

    @Test(expected = RobotAgentEventsListenerException.class)
    public void exceptionIsThrown_whenResponderIsUnableToResponse() {
        final UserProcessController controller = mock(UserProcessController.class);
        when(controller.takeCurrentResponse(PausingPoint.START_KEYWORD)).thenReturn(Optional.empty());

        final ShouldContinueEventResponder responder = mock(ShouldContinueEventResponder.class);
        doThrow(ResponseException.class).when(responder).respond(any(ServerResponse.class));

        final ShouldContinueEvent event = new ShouldContinueEvent(responder, PausingPoint.START_KEYWORD);
        final ExecutionPauseContinueListener listener = new ExecutionPauseContinueListener(controller);
        listener.handleShouldContinue(event);
    }

    @Test
    public void controllerIsNotifiedAndFutureResponseIsScheduled_whenAgentPauseOccurs() {
        final FutureTask<ServerResponse> task = new FutureTask<>(() -> null);

        final UserProcessController controller = mock(UserProcessController.class);
        when(controller.takeFutureResponse()).thenReturn(task);

        final PausedEventResponder responder = mock(PausedEventResponder.class);

        final PausedEvent event = new PausedEvent(responder);
        final ExecutionPauseContinueListener listener = new ExecutionPauseContinueListener(controller);
        listener.handlePaused(event);

        verify(controller).executionPaused();
        verify(responder).respondAsynchronously(same(task), any(ResumeExecution.class));
    }

    @Test
    public void controllerIsNotified_whenConditionHasBeenEvaluated() {
        final UserProcessController controller = mock(UserProcessController.class);

        final ConditionEvaluatedEvent event = new ConditionEvaluatedEvent(true);

        final ExecutionPauseContinueListener listener = new ExecutionPauseContinueListener(controller);
        listener.handleConditionEvaluated(event);

        verify(controller).conditionEvaluated(event);
        verifyNoMoreInteractions(controller);
    }
}
