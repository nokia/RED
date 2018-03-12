/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.execution.debug;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import java.util.concurrent.FutureTask;

import org.junit.Test;
import org.rf.ide.core.execution.agent.PausingPoint;
import org.rf.ide.core.execution.agent.event.ConditionEvaluatedEvent;
import org.rf.ide.core.execution.debug.UserProcessController.ResponseWithCallback;
import org.rf.ide.core.execution.server.response.DisconnectExecution;
import org.rf.ide.core.execution.server.response.PauseExecution;
import org.rf.ide.core.execution.server.response.ResumeExecution;
import org.rf.ide.core.execution.server.response.ServerResponse;
import org.rf.ide.core.execution.server.response.TerminateExecution;

public class UserProcessControllerTest {

    @Test
    public void nothingHappens_whenExecutionPauses() {
        final UserProcessController controller = spy(new UserProcessController());

        controller.executionPaused();

        assertThat(controller.manualUserResponse).isEmpty();
        verify(controller).executionPaused();
        verifyNoMoreInteractions(controller);
    }

    @Test
    public void nothingHappens_whenConditionIsEvaluated() {
        final UserProcessController controller = spy(new UserProcessController());

        final ConditionEvaluatedEvent event = new ConditionEvaluatedEvent("");
        controller.conditionEvaluated(event);

        assertThat(controller.manualUserResponse).isEmpty();
        verify(controller).conditionEvaluated(event);
        verifyNoMoreInteractions(controller);
    }

    @Test
    public void thereIsNoResponse_forNewlyCreatedController() {
        final UserProcessController controller = new UserProcessController();

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);
        assertThat(response).isEmpty();
    }

    @Test
    public void whenDisconnectWasOrdered_callbackRunsAndResponseIsProperlyReturned() {
        final Runnable callback = mock(Runnable.class);

        final UserProcessController controller = new UserProcessController();
        controller.disconnect(callback);

        assertThat(controller.manualUserResponse).hasSize(1);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(DisconnectExecution.class);
        verify(callback).run();
    }

    @Test
    public void whenTerminateWasOrdered_callbackRunsAndResponseIsProperlyReturned() {
        final Runnable callback = mock(Runnable.class);

        final UserProcessController controller = new UserProcessController();
        controller.terminate(callback);

        assertThat(controller.manualUserResponse).hasSize(1);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(TerminateExecution.class);
        verify(callback).run();
    }

    @Test
    public void whenPauseWasOrdered_callbackRunsAndResponseIsProperlyReturned() {
        final Runnable callback = mock(Runnable.class);

        final UserProcessController controller = new UserProcessController();
        controller.pause(callback);

        assertThat(controller.manualUserResponse).hasSize(1);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(PauseExecution.class);
        verify(callback).run();
    }

    @Test
    public void whenResumeWasOrdered_callbackRunsAndResponseIsProperlyReturned() {
        final Runnable callback = mock(Runnable.class);

        final UserProcessController controller = new UserProcessController();
        controller.resume(callback);

        assertThat(controller.manualUserResponse).hasSize(1);

        final Optional<ServerResponse> response = controller.takeCurrentResponse(PausingPoint.START_KEYWORD);

        assertThat(controller.manualUserResponse).isEmpty();
        assertThat(response).containsInstanceOf(ResumeExecution.class);
        verify(callback).run();
    }

    @Test
    public void whenFutureResponseWasOrdered_itIsReturnedAsFutureTask() throws Exception {
        final Runnable callback = mock(Runnable.class);

        final UserProcessController controller = new UserProcessController();

        final ServerResponse response = () -> "response";
        controller.manualUserResponse.put(new ResponseWithCallback(response, callback));
        
        final FutureTask<ServerResponse> futureResponse = controller.takeFutureResponse();

        assertThat(controller.manualUserResponse).hasSize(1);

        futureResponse.run();

        assertThat(controller.manualUserResponse).isEmpty();
        verify(callback).run();
    }
}
