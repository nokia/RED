/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.io.PrintWriter;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.rf.ide.core.execution.server.response.ServerResponse;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

import com.google.common.annotations.VisibleForTesting;

public class AgentClient {

    private final ExecutorService executor;

    private final int clientId;
    private final PrintWriter writer;

    AgentClient(final int clientId, final PrintWriter writer) {
        this.executor = Executors.newFixedThreadPool(2);
        this.clientId = clientId;
        this.writer = writer;
    }

    public int getId() {
        return clientId;
    }

    @VisibleForTesting
    ExecutorService getExecutorService() {
        return executor;
    }

    public synchronized void send(final ServerResponse response) throws ResponseException {
        writer.print(response.toMessage() + '\n');
        writer.flush();
    }

    public synchronized void sendAsync(final FutureTask<ServerResponse> futureResponse,
            final ServerResponse responseOnError) {
        executor.execute(futureResponse);
        executor.execute(() -> {
            try {
                send(futureResponse.get());
            } catch (InterruptedException | ExecutionException e) {
                send(responseOnError);
            }
        });
    }

    void dispose() {
        executor.shutdown();
    }
}