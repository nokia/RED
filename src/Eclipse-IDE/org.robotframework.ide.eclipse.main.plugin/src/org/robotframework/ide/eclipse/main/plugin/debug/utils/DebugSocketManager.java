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

import com.google.common.base.Optional;

/**
 * @author mmarzec
 *
 */
public class DebugSocketManager implements Runnable {
    
    private static final int DEBUG_SERVER_DEFAULT_CONNECTION_TIMEOUT = 30000;
    
    public static final int WAIT_FOR_AGENT_TIME = 500;

    private ServerSocket serverSocket = null;

    private Socket eventSocket = null;

    private int port = 0;
    
    private String host = "";
    
    private int connectionTimeoutInMilliseconds = 0;
    
    private boolean hasServerException;
    
    public DebugSocketManager(final String host, final int port, final Optional<Integer> connectionTimeout) {
        this.host = host;
        this.port = port;
        this.connectionTimeoutInMilliseconds = getTimeout(connectionTimeout);
    }

    private int getTimeout(final Optional<Integer> connectionTimeout) {
        if (connectionTimeout.isPresent() && connectionTimeout.get() > 0) {
            return connectionTimeout.get();
        }
        return DEBUG_SERVER_DEFAULT_CONNECTION_TIMEOUT;
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
            //e.printStackTrace();
            
            hasServerException = true;
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Socket getEventSocket() {
        return eventSocket;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public boolean hasServerException() {
        return hasServerException;
    }
    
    public int getRetryCounterMaxValue() {
        return connectionTimeoutInMilliseconds / WAIT_FOR_AGENT_TIME;
    }

}
