/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.dryrun;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent;
import org.rf.ide.core.execution.server.response.ContinueExecution;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

public class RobotDryRunAlwaysContinueEventListener extends RobotDefaultAgentEventListener {

    @Override
    public void handleShouldContinue(final ShouldContinueEvent event) {
        try {
            event.responder().respond(new ContinueExecution());
        } catch (final ResponseException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
    }
}
