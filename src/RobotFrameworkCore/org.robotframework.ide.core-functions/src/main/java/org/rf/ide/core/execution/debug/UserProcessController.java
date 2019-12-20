/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;

import org.rf.ide.core.execution.agent.PausingPoint;
import org.rf.ide.core.execution.agent.event.ConditionEvaluatedEvent;
import org.rf.ide.core.execution.server.response.DisconnectExecution;
import org.rf.ide.core.execution.server.response.InterruptExecution;
import org.rf.ide.core.execution.server.response.PauseExecution;
import org.rf.ide.core.execution.server.response.ResumeExecution;
import org.rf.ide.core.execution.server.response.ServerResponse;
import org.rf.ide.core.execution.server.response.TerminateExecution;
import org.rf.ide.core.testdata.model.table.keywords.names.QualifiedKeywordName;

public class UserProcessController {

    // user responses queue containing at most 1 response from user
    final BlockingQueue<ResponseWithCallback> manualUserResponse = new LinkedBlockingQueue<>(1);

    public void executionPaused() {
        // nothing to do, override if needed
    }

    public void conditionEvaluated(@SuppressWarnings("unused") final ConditionEvaluatedEvent event) {
        // nothing to do, override if needed
    }

    @SuppressWarnings("unused")
    public Optional<ServerResponse> takeCurrentResponse(final PausingPoint pausingPoint,
            final QualifiedKeywordName currentlyFailedKeyword) {
        final Optional<ResponseWithCallback> currentResponse = takeQueuedResponse();

        currentResponse.ifPresent(response -> response.callback.run());
        return currentResponse.map(response -> response.response);
    }

    private Optional<ResponseWithCallback> takeQueuedResponse() {
        return Optional.ofNullable(manualUserResponse.poll());
    }

    public FutureTask<ServerResponse> takeFutureResponse() {
        return new FutureTask<>(() -> {
            final ResponseWithCallback currentResponse = manualUserResponse.take();

            currentResponse.callback.run();
            return currentResponse.response;
        });
    }

    public void disconnect(final Runnable whenResponseIsSent) {
        offer(new ResponseWithCallback(new DisconnectExecution(), whenResponseIsSent));
    }

    public void interrupt(final Runnable whenResponseIsSent) {
        offer(new ResponseWithCallback(new InterruptExecution(), whenResponseIsSent));
    }

    public void terminate(final Runnable whenResponseIsSent) {
        offer(new ResponseWithCallback(new TerminateExecution(), whenResponseIsSent));
    }

    public void pause(final Runnable whenResponseIsSent) {
        offer(new ResponseWithCallback(new PauseExecution(), whenResponseIsSent));
    }

    public void resume(final Runnable whenResponseIsSent) {
        offer(new ResponseWithCallback(new ResumeExecution(), whenResponseIsSent));
    }

    protected final void offer(final ResponseWithCallback response) {
        int attempt = 0;
        while (attempt < 10) {
            final boolean added = manualUserResponse.offer(response);
            if (added) {
                return;
            }
            attempt++;
        }
        throw new IllegalStateException("Unable to put reponse to agent in the responses queue");
    }

    static class ResponseWithCallback {

        private final ServerResponse response;

        private final Runnable callback;

        public ResponseWithCallback(final ServerResponse response, final Runnable callback) {
            this.response = response;
            this.callback = callback;
        }
    }
}
