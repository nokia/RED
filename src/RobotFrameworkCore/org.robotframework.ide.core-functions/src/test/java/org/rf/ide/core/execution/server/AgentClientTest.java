/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.rf.ide.core.execution.server.response.ServerResponse;

public class AgentClientTest {

    @Test
    public void nullMessageIsSendToClientUsingProvidedWriter() throws Exception {
        try (final StringWriter stringWriter = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(stringWriter)) {
            final AgentClient client = new AgentClient(0, printWriter);
            client.send(() -> null);

            assertThat(client.getId()).isZero();
            assertThat(stringWriter.toString()).isEqualTo("null\n");
        }
    }

    @Test
    public void messageIsSendToClientUsingProvidedWriter() throws Exception {
        try (final StringWriter stringWriter = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(stringWriter)) {
            final AgentClient client = new AgentClient(1, printWriter);
            client.send(() -> "message");

            assertThat(client.getId()).isEqualTo(1);
            assertThat(stringWriter.toString()).isEqualTo("message\n");
        }
    }

    @Test
    public void messageIsSendAsynchronously() throws Exception {
        try (final StringWriter stringWriter = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(stringWriter)) {
            final AgentClient client = new AgentClient(1, printWriter);

            client.sendAsync(new FutureTask<>(() -> (ServerResponse) (() -> "message")), () -> "error happened");
            client.dispose();
            client.getExecutorService().awaitTermination(10, TimeUnit.SECONDS);

            assertThat(stringWriter.toString()).isEqualTo("message\n");
        }
    }

    @Test
    public void errorMessageIsSendAsynchronously_whenOriginalTaskThrowsException() throws Exception {
        try (final StringWriter stringWriter = new StringWriter();
                final PrintWriter printWriter = new PrintWriter(stringWriter)) {
            final AgentClient client = new AgentClient(1, printWriter);

            client.sendAsync(new FutureTask<>(() -> {
                throw new RuntimeException();
            }), () -> "error happened");
            client.dispose();
            client.getExecutorService().awaitTermination(10, TimeUnit.SECONDS);

            assertThat(stringWriter.toString()).isEqualTo("error happened\n");
        }
    }
}
