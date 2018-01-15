/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.rf.ide.core.execution.agent.RobotAgentEventListener;
import org.rf.ide.core.execution.agent.RobotAgentEventListener.RobotAgentEventsListenerException;

import com.google.common.base.Charsets;

public class AgentConnectionServer {

    public static final int RED_AGENT_PROTOCOL_VERSION = 2;

    public static final String DEFAULT_CONNECTION_HOST = "127.0.0.1";

    public static final int DEFAULT_CONNECTION_PORT = 43_981; // 0xABCD

    public static final int DEFAULT_CONNECTION_TIMEOUT = 30;

    public static final int MIN_CONNECTION_PORT = 1;

    public static final int MIN_CONNECTION_TIMEOUT = 1;

    public static final int MAX_CONNECTION_PORT = 65_535;

    public static final int MAX_CONNECTION_TIMEOUT = 3_600;

    public static final int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            return -1;
        }
    }

    private final int port;

    private final String host;

    private final int timeoutInMillis;

    private final List<AgentServerStatusListener> listeners = new ArrayList<>();

    private final Semaphore serverSetupSemaphore = new Semaphore(0);

    private ServerSocket serverSocket;

    public AgentConnectionServer(final String host, final int port) {
        this(host, port, DEFAULT_CONNECTION_TIMEOUT, TimeUnit.SECONDS);
    }

    public AgentConnectionServer(final String host, final int port, final int timeout, final TimeUnit timeoutUnit) {
        this.host = host;
        this.port = port;
        this.timeoutInMillis = (int) timeoutUnit.toMillis(timeout);
    }

    public void addStatusListener(final AgentServerStatusListener listener) {
        listeners.add(listener);
    }

    public void removeStatusListener(final AgentServerStatusListener listener) {
        listeners.remove(listener);
    }

    public void start(final RobotAgentEventListener... eventsListeners) throws IOException {
        AgentClient client = null;
        try {
            serverSetupSemaphore.release();

            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(timeoutInMillis);

            listeners.forEach(listener -> listener.serverEstablished(host, port));
            try (Socket clientSocket = serverSocket.accept()) {

                final int clientId = clientSocket.hashCode();

                final BufferedReader eventsReader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream(), Charsets.UTF_8));
                final BufferedWriter eventsWriter = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream(), Charsets.UTF_8));

                client = new AgentClient(clientId, new PrintWriter(eventsWriter));
                final RobotAgentEventDispatcher eventsDispatcher = new RobotAgentEventDispatcher(
                        client, eventsListeners);

                listeners.forEach(listener -> listener.clientConnected(clientId));
                eventsDispatcher.runEventsLoop(eventsReader);
                listeners.forEach(listener -> listener.clientConnectionClosed(clientId));

            } catch (final RobotAgentEventsListenerException e) {
                listeners.forEach(listener -> listener.clientEventHandlingError(e));
            } catch (final SocketTimeoutException e) {
                listeners.forEach(listener -> listener.clientConnectionTimedOut(e));
            } catch (final IOException e) {
                listeners.forEach(listener -> listener.clientConnectionError(e));
            }
        } finally {
            if (client != null) {
                client.dispose();
            }
            stop();
        }
    }

    public void waitForServerToSetup() throws InterruptedException {
        serverSetupSemaphore.acquire();
    }

    public void stop() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
