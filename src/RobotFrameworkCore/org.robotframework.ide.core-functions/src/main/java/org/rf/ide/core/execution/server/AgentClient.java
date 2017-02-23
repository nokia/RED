/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import java.io.IOException;
import java.io.PrintWriter;

import org.rf.ide.core.execution.server.response.ServerResponse;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

public class AgentClient {

    private final int clientId;
    private final PrintWriter writer;

    // TODO : this should not be public
    public AgentClient(final int clientId, final PrintWriter writer) {
        this.clientId = clientId;
        this.writer = writer;
    }

    public int getId() {
        return clientId;
    }

    public synchronized void send(final ServerResponse response) throws IOException, ResponseException {
        writer.print(response.toMessage());
        writer.flush();
    }
}