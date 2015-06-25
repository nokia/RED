package org.robotframework.ide.eclipse.main.plugin.debug.utils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DebugSocketManager implements Runnable {

    private ServerSocket serverSocket = null;

    private Socket eventSocket = null;

    private int port = 0;
    
    public DebugSocketManager(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(10000);

            while (true) {
                eventSocket = serverSocket.accept();
            }
        } catch (final IOException e) {
            // TODO: check if socket exception was caused by close during accept, then some info log
            // should be printed without stack trace
            e.printStackTrace();
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Socket getEventSocket() {
        return eventSocket;
    }

}
