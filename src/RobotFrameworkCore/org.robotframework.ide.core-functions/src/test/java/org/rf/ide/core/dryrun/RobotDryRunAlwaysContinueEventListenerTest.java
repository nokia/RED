/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.rf.ide.core.dryrun;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent.ShouldContinueEventResponder;
import org.rf.ide.core.execution.server.response.ContinueExecution;

public class RobotDryRunAlwaysContinueEventListenerTest {

    @Test
    public void shouldContinueEventIsHandled() throws Exception {
        final RobotDryRunAlwaysContinueEventListener listener = new RobotDryRunAlwaysContinueEventListener();
        final ShouldContinueEventResponder responder = mock(ShouldContinueEventResponder.class);
        final ShouldContinueEvent event = new ShouldContinueEvent(responder, null);

        listener.handleShouldContinue(event);

        verify(responder).respond(isA(ContinueExecution.class));
        verifyNoMoreInteractions(responder);
    }
}
