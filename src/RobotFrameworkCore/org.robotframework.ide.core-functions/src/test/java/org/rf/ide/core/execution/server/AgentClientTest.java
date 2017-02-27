/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.Test;

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
}
