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
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Supplier;

/**
 * @author mmarzec
 *
 */
public class TestRunnerAgentHandler implements Runnable {

    private static final int TIMEOUT = 60_000;

    private final List<ILineHandler> listeners;
    
    private final int port;

    private BufferedWriter agentWriter;
    
    private final Semaphore writerSemaphore = new Semaphore(0, true);

    public TestRunnerAgentHandler(final int port) {
        listeners = new ArrayList<>();
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            socket.setSoTimeout(TIMEOUT);
            final Socket client = socket.accept();

            agentWriter = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            final BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

            writerSemaphore.release();

            String line = reader.readLine();
            while (line != null) {
                for (final ILineHandler listener : listeners) {
                    listener.processLine(line);
                }
                line = reader.readLine();
            }
        } catch (final IOException e) {
        } finally {
            if (writerSemaphore.availablePermits() == 0) {
                writerSemaphore.release();
            }
            try {
                if (agentWriter != null) {
                    agentWriter.close();
                }
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
    
    public void startTests(final Supplier<Boolean> shouldNotWaitMoreCondition) throws IOException {
        try {
            while (!writerSemaphore.tryAcquire(1_000, TimeUnit.MILLISECONDS) && !shouldNotWaitMoreCondition.get()) {
                // do nothing, waiting is done in tryAcquire
            }
        } catch (final InterruptedException e) {
            throw new IllegalStateException("Interrupted when waiting for agent connection!", e);
        }
        if (agentWriter != null) {
            agentWriter.write("do start");
            agentWriter.flush();
        }
    }
}
