/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author mmarzec
 *
 */
public class DebugSocketManager implements Runnable {
    
    private static final int DEBUG_SERVER_DEFAULT_CONNECTION_TIMEOUT = 30000;
    public static final int WAIT_FOR_AGENT_TIME = 500;

    private final String host;
    private final int port;
    
    private final int connectionTimeoutInMilliseconds;

    private ServerSocket serverSocket = null;
    private Socket eventSocket = null;
    
    private boolean hasServerException;
    
    public DebugSocketManager(final String host, final int port) {
        this(host, port, DEBUG_SERVER_DEFAULT_CONNECTION_TIMEOUT);
    }

    public DebugSocketManager(final String host, final int port, final int connectionTimeout) {
        this.host = host;
        this.port = port;
        this.connectionTimeoutInMilliseconds = connectionTimeout;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(connectionTimeoutInMilliseconds);

            while (true) {
                eventSocket = serverSocket.accept();
            }
        } catch (final IOException e) {
            // TODO: check if socket exception was caused by close during accept, then some info log
            // should be printed without stack trace
            // e.printStackTrace();

            hasServerException = true;
        }
    }

    public boolean waitForDebugServerSocket() {
        boolean isListening = false;
        int retryCounter = 0;
        while (!isListening && retryCounter < 20) {
            try (Socket temporarySocket = new Socket(host, port)) {
                isListening = true;
            } catch (final IOException e) {
                if (hasServerException) {
                    return isListening;
                }
                try {
                    Thread.sleep(100);
                    retryCounter++;
                } catch (final InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return isListening;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Socket getEventSocket() {
        return eventSocket;
    }

    public int getRetryCounterMaxValue() {
        return connectionTimeoutInMilliseconds / WAIT_FOR_AGENT_TIME;
    }

    public void closeServerSocket() throws IOException {
        if (serverSocket != null) {
            serverSocket.close();
        }
    }
}
