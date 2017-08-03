/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.debug;

import org.rf.ide.core.execution.agent.PausingPoint;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.ConditionEvaluatedEvent;
import org.rf.ide.core.execution.agent.event.PausedEvent;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent;
import org.rf.ide.core.execution.server.response.ContinueExecution;
import org.rf.ide.core.execution.server.response.ResumeExecution;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

public class ExecutionPauseContinueListener extends RobotDefaultAgentEventListener {

    private final UserProcessController controller;

    public ExecutionPauseContinueListener(final UserProcessController controller) {
        this.controller = controller;
    }

    @Override
    public void handleShouldContinue(final ShouldContinueEvent event) {
        // When agent is asking if execution should be continued we look if user manually set a
        // response (like clicked suspend/disconnect/terminate etc)
        try {
            final PausingPoint pausingPoint = event.getPausingPoint();
            event.responder().respond(controller.takeCurrentResponse(pausingPoint).orElse(new ContinueExecution()));
        } catch (final ResponseException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
    }

    @Override
    public void handlePaused(final PausedEvent event) {
        controller.executionPaused();
        // When agent has paused execution we need to take users action from future and respond
        // asynchronously to the client with appropriate response to pause request. In case there is
        // some error taking action from future, we simple say to the client that it should resume,
        // so that it will not wait forever because of exception on RED side.
        event.responder().respondAsynchronously(controller.takeFutureResponse(), new ResumeExecution());
    }

    @Override
    public void handleConditionEvaluated(final ConditionEvaluatedEvent event) {
        controller.conditionEvaluated(event);
    }
}
