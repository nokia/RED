/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.rf.ide.core.execution.RobotAgentEventDispatcher;
import org.rf.ide.core.execution.RobotAgentEventListener;
import org.rf.ide.core.execution.RobotAgentEventListener.RobotAgentEventsListenerException;

public class AgentConnectionServer {

    public static final int CLIENT_CONNECTION_TIMEOUT = 30_000;

    private final int port;

    private final String host;

    private final int timeoutInMillis;

    private final List<AgentServerStatusListener> listeners = new ArrayList<>();

    private final Semaphore serverSetupSemaphore = new Semaphore(0);

    private ServerSocket serverSocket;

    public AgentConnectionServer(final String host, final int port) {
        this(host, port, CLIENT_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
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
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(timeoutInMillis);

            serverSetupSemaphore.release();
            listeners.stream().forEach(listener -> listener.serverEstablished(host, port));
            try (Socket clientSocket = serverSocket.accept()) {

                final int clientId = clientSocket.hashCode();

                final BufferedReader eventsReader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                final PrintWriter eventsWriter = new PrintWriter(clientSocket.getOutputStream());

                final RobotAgentEventDispatcher eventsDispatcher = new RobotAgentEventDispatcher(
                        new AgentClient(clientId, eventsWriter), eventsListeners);

                listeners.stream().forEach(listener -> listener.clientConnected(clientId));
                eventsDispatcher.runEventsLoop(eventsReader);
                listeners.stream().forEach(listener -> listener.clientConnectionClosed(clientId));

            } catch (final RobotAgentEventsListenerException e) {
                listeners.stream().forEach(listener -> listener.clientEventHandlingError(e));
            } catch (final SocketTimeoutException e) {
                listeners.stream().forEach(listener -> listener.clientConnectionTimedOut(e));
            } catch (final IOException e) {
                listeners.stream().forEach(listener -> listener.clientConnectionError(e));
            }
        } finally {
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
