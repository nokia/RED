/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.rf.ide.core.dryrun.IAgentMessageHandler;
import org.rf.ide.core.execution.server.AgentClient;

/**
 * @author mmarzec
 *
 */
public class TestRunnerAgentHandler implements Runnable {

    private static final int TIMEOUT = 60_000;

    private final List<IAgentMessageHandler> listeners;

    private final int port;

    private final Semaphore writerSemaphore = new Semaphore(0, true);

    public TestRunnerAgentHandler(final int port) {
        this.listeners = new ArrayList<>();
        this.port = port;
    }

    @Override
    public void run() {
        PrintWriter eventsWriter = null;
        BufferedReader eventsReader = null;
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            socket.setSoTimeout(TIMEOUT);
            final Socket client = socket.accept();

            eventsWriter = new PrintWriter(client.getOutputStream());
            eventsReader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            writerSemaphore.release();

            String line = eventsReader.readLine();
            while (line != null) {
                for (final IAgentMessageHandler listener : listeners) {
                    listener.processMessage(line, new AgentClient(0, eventsWriter));
                }
                line = eventsReader.readLine();
            }
        } catch (final IOException e) {
        } finally {
            if (writerSemaphore.availablePermits() == 0) {
                writerSemaphore.release();
            }
            try {
                if (eventsWriter != null) {
                    eventsWriter.close();
                }
                if (eventsReader != null) {
                    eventsReader.close();
                }
            } catch (final IOException e) {
            }
        }
    }

    public void addListener(final IAgentMessageHandler listener) {
        listeners.add(listener);
    }

    public void removeListener(final IAgentMessageHandler listener) {
        listeners.remove(listener);
    }
}
