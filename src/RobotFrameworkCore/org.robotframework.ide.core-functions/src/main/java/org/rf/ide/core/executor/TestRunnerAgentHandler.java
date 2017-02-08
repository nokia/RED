/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mmarzec
 *
 */
public class TestRunnerAgentHandler implements Runnable {

    private final List<ILineHandler> listeners;
    
    private final int port;

    private BufferedWriter agentWriter;
    
    public TestRunnerAgentHandler(final int port) {
        listeners = new ArrayList<>();
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket socket = new ServerSocket(port)) {
            final BufferedReader reader;
            synchronized (this) {
                socket.setReuseAddress(true);
                final Socket client = socket.accept();

                agentWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            }
            
            String line;
            while ((line = reader.readLine()) != null) {
                for (final ILineHandler listener : listeners) {
                    listener.processLine(line);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            try {
                agentWriter.close();
            } catch (final IOException e) {
            }
        }
    }

    public void addListener(final ILineHandler listener) {
        listeners.add(listener);
    }

    public void removeListener(final ILineHandler listener) {
        listeners.remove(listener);
    }
    
    public synchronized void startTests() throws IOException {
        agentWriter.write("do start");
        agentWriter.flush();
    }
}
