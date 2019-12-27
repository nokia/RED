/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.rf.ide.core.execution.agent.RobotAgentEventListener;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;
import org.rf.ide.core.execution.agent.event.ReadyToStartEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

@Disabled("see RED-976")
public class AgentConnectionServerTest {

    @Test
    public void connectionTimeoutErrorIsHandledByListener_whenNoClientConnects() throws Exception {
        final String host = "127.0.0.1";
        final int port = findFreePort();

        final AgentServerStatusListener serverStatusListener = mock(AgentServerStatusListener.class);

        final AgentConnectionServer server = new AgentConnectionServer(host, port, 100, TimeUnit.MILLISECONDS);
        server.addStatusListener(serverStatusListener);
        server.start();

        verify(serverStatusListener).serverEstablished(host, port);
        verify(serverStatusListener).clientConnectionTimedOut(any(SocketTimeoutException.class));
        verifyNoMoreInteractions(serverStatusListener);
    }

    @Test
    public void eventHandlingErrorIsHandledByListener_whenEventsListenerCannotHandleEvent() throws Exception {
        final String host = "127.0.0.1";
        final int port = findFreePort();

        final AgentServerStatusListener serverStatusListener = mock(AgentServerStatusListener.class);
        final RobotAgentEventListener robotEventListener = mock(RobotAgentEventListener.class);
        when(robotEventListener.isHandlingEvents()).thenReturn(true);
        doThrow(RobotAgentEventsListenerException.class).when(robotEventListener)
                .handleAgentIsReadyToStart(any(ReadyToStartEvent.class));

        final AgentConnectionServer server = new AgentConnectionServer(host, port);
        server.addStatusListener(serverStatusListener);

        final Thread serverThread = new Thread(() -> {
            try {
                server.start(robotEventListener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try {
                writeToSocket(host, port, ImmutableMap.of("ready_to_start", 0));
            } catch (final IOException e) {
            }
        });
        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(serverStatusListener).serverEstablished(host, port);
        verify(serverStatusListener).clientConnected(anyInt());
        verify(serverStatusListener).clientEventHandlingError(any(RobotAgentEventsListenerException.class));
        verifyNoMoreInteractions(serverStatusListener);
    }

    @Test
    public void connectionErrorIsHandledByListener_whenInvalidMessageComesFromClient() throws Exception {
        final String host = "127.0.0.1";
        final int port = findFreePort();

        final AgentServerStatusListener serverStatusListener = mock(AgentServerStatusListener.class);
        final RobotAgentEventListener robotEventListener = mock(RobotAgentEventListener.class);
        when(robotEventListener.isHandlingEvents()).thenReturn(true);

        final AgentConnectionServer server = new AgentConnectionServer(host, port);
        server.addStatusListener(serverStatusListener);

        final Thread serverThread = new Thread(() -> {
            try {
                server.start(robotEventListener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try {
                writeToSocket(host, port, "invalid_messge_not_a_json_mapping");
            } catch (final IOException e) {
            }
        });
        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(serverStatusListener).serverEstablished(host, port);
        verify(serverStatusListener).clientConnected(anyInt());
        verify(serverStatusListener).clientConnectionError(any(IOException.class));
        verifyNoMoreInteractions(serverStatusListener);
    }

    @Test
    public void connectionIsProperlyHandled_whenEventListenerHandlesIncomingEvent() throws Exception {
        final String host = "127.0.0.1";
        final int port = findFreePort();

        final AgentServerStatusListener serverStatusListener = mock(AgentServerStatusListener.class);
        final RobotAgentEventListener robotEventListener = mock(RobotAgentEventListener.class);
        when(robotEventListener.isHandlingEvents()).thenReturn(true);

        final AgentConnectionServer server = new AgentConnectionServer(host, port);
        server.addStatusListener(serverStatusListener);

        final Thread serverThread = new Thread(() -> {
            try {
                server.start(robotEventListener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try {
                writeToSocket(host, port, ImmutableMap.of("ready_to_start", 0));
            } catch (final IOException e) {
            }
        });
        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(serverStatusListener).serverEstablished(host, port);
        verify(serverStatusListener).clientConnected(anyInt());
        verify(serverStatusListener).clientConnectionClosed(anyInt());
        verifyNoMoreInteractions(serverStatusListener);
    }

    @Test
    public void exceptionIsThrown_whenHostCannotBeReached() throws Exception {
        final String host = "123456789";
        final int port = findFreePort();

        final AgentServerStatusListener serverStatusListener = mock(AgentServerStatusListener.class);

        final AgentConnectionServer server = new AgentConnectionServer(host, port, 100, TimeUnit.MILLISECONDS);
        server.addStatusListener(serverStatusListener);

        assertThatExceptionOfType(BindException.class).isThrownBy(server::start);
    }

    @Test
    public void exceptionIsThrown_whenPortIsOutOfRange() throws Exception {
        final String host = "127.0.0.1";
        final int port = 65536;

        final AgentServerStatusListener serverStatusListener = mock(AgentServerStatusListener.class);

        final AgentConnectionServer server = new AgentConnectionServer(host, port, 100, TimeUnit.MILLISECONDS);
        server.addStatusListener(serverStatusListener);

        assertThatIllegalArgumentException().isThrownBy(server::start);
    }

    @Test
    @Timeout(5)
    public void deadLockDoesNotOccur_whenWaitingForServerWhichThrewExceptionDuringStarting() throws Exception {
        final String host = "123456789";
        final int port = findFreePort();

        final AgentServerStatusListener serverStatusListener = mock(AgentServerStatusListener.class);

        final AgentConnectionServer server = new AgentConnectionServer(host, port, 100, TimeUnit.MILLISECONDS);
        server.addStatusListener(serverStatusListener);

        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (final Exception e) {
            }
        });

        serverThread.start();
        serverThread.join();

        server.waitForServerToSetup();

        verifyZeroInteractions(serverStatusListener);
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static void writeToSocket(final String host, final int port, final Object message) throws IOException {
        try (final Socket clientSocket = new Socket(host, port);) {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
                writer.write(new ObjectMapper().writeValueAsString(message));
            }
        }
    }
}
