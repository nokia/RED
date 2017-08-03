/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.agent.TestsMode;
import org.rf.ide.core.execution.agent.event.ReadyToStartEvent;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;
import org.rf.ide.core.execution.server.response.StartExecution;

public class AgentServerTestsStarterTest {

    @Test
    public void agentServerStarterNeverSendsStartResponseToClient_itIsNotAllowedAndInterrupted() throws Exception {
        final AtomicBoolean interruptedCaught = new AtomicBoolean(false);

        final AgentClient client = mock(AgentClient.class);

        final AgentServerTestsStarter starter = spy(new AgentServerTestsStarter(TestsMode.RUN));

        final Thread serverThread = new Thread(() -> {
            try {
                starter.handleAgentIsReadyToStart(ReadyToStartEvent.from(client));
            } catch (final RobotAgentEventsListenerException e) {
                interruptedCaught.set(true);
            }
        });
        serverThread.start();

        // wait a while, so that starter will acquire semaphore
        Thread.sleep(300);
        serverThread.interrupt();
        serverThread.join();

        assertThat(interruptedCaught.get()).isTrue();
        verify(starter).handleAgentIsReadyToStart(any(ReadyToStartEvent.class));
        verifyZeroInteractions(client);
    }

    @Test
    public void exceptionIsThrown_whenAgentServerHasProblemCreatingResponse() throws Exception {
        final AtomicBoolean responseExceptionCaught = new AtomicBoolean(false);

        final AgentClient client = mock(AgentClient.class);
        doThrow(ResponseException.class).when(client).send(any(StartExecution.class));

        final AgentServerTestsStarter starter = spy(new AgentServerTestsStarter(TestsMode.RUN));

        final Thread serverThread = new Thread(() -> {
            try {
                starter.handleAgentIsReadyToStart(ReadyToStartEvent.from(client));
            } catch (final RobotAgentEventsListenerException e) {
                responseExceptionCaught.set(true);
            }
        });
        serverThread.start();
        starter.allowClientTestsStart();
        serverThread.join();

        assertThat(responseExceptionCaught.get()).isTrue();
    }

    @Test
    public void agentServerStarterSendsStartResponseToClient_whenItIsAllowedToStartTests() throws Exception {
        final AgentClient client = mock(AgentClient.class);

        final AgentServerTestsStarter starter = spy(new AgentServerTestsStarter(TestsMode.RUN));

        final Thread serverThread = new Thread(() -> starter.handleAgentIsReadyToStart(ReadyToStartEvent.from(client)));
        serverThread.start();
        starter.allowClientTestsStart();
        serverThread.join();

        verify(client).send(any(StartExecution.class));
    }
}
