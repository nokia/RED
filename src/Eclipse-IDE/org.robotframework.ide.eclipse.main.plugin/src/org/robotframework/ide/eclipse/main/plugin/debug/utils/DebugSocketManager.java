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

public class DebugSocketManager implements Runnable {

    private ServerSocket serverSocket = null;

    private Socket eventSocket = null;

    private int port = 0;
    
    private String host = "";
    
    private boolean hasServerException;
    
    public DebugSocketManager(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(30000);

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

}
