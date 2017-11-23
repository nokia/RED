/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.rflint;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

@Ignore("see RED-976")
public class RfLintIntegrationServerTest {

    @Test
    public void byDefaultTheServerRunsOnLocalhostWithSomePort() {
        final RfLintIntegrationServer server = new RfLintIntegrationServer(e -> {});

        assertThat(server.getHost()).isEqualTo("127.0.0.1");
        assertThat(server.getPort()).isPositive();
    }

    @Test
    public void connectionTimeoutIsHandledByGivenListener() throws IOException {
        @SuppressWarnings("unchecked")
        final Consumer<Exception> onServerException = mock(Consumer.class);
        final RfLintIntegrationServer server = new RfLintIntegrationServer(100, TimeUnit.MILLISECONDS,
                onServerException);

        server.start();
        verify(onServerException).accept(any(SocketTimeoutException.class));
    }

    @Test
    public void nothingIsThrown_whenServerWasStoppedByUserWhenWaitingForConnection() throws Exception {
        @SuppressWarnings("unchecked")
        final Consumer<Exception> onServerException = mock(Consumer.class);
        final RfLintIntegrationServer server = new RfLintIntegrationServer(onServerException);

        final Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (final IOException e) {
            }
        });
        serverThread.start();
        server.waitForServerToSetup();
        server.stop();
        serverThread.join();

        verifyZeroInteractions(onServerException);
    }

    @Test
    public void filesToProcessEventIsHandledByListener() throws Exception {
        final RfLintClientEventsListener listener = mock(RfLintClientEventsListener.class);

        final RfLintIntegrationServer server = new RfLintIntegrationServer(e -> {});
        final Thread serverThread = new Thread(() -> {
            try {
                server.start(listener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try (final Socket clientSocket = new Socket(server.getHost(), server.getPort())) {
                server.waitForServerToSetup();
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()))) {
                    final Object msgObject = ImmutableMap.of("files_to_process", newArrayList(42));
                    writer.write(new ObjectMapper().writeValueAsString(msgObject));
                }
            } catch (final Exception e) {
            }
        });

        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(listener).filesToProcess(42);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void fileProcessingStartedEventIsHandledByListener() throws Exception {
        final RfLintClientEventsListener listener = mock(RfLintClientEventsListener.class);

        final RfLintIntegrationServer server = new RfLintIntegrationServer(e -> {});
        final Thread serverThread = new Thread(() -> {
            try {
                server.start(listener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try (final Socket clientSocket = new Socket(server.getHost(), server.getPort())) {
                server.waitForServerToSetup();
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()))) {
                    final Object msgObject = ImmutableMap.of("file_processing_started",
                            newArrayList("/path/to/file.robot"));
                    writer.write(new ObjectMapper().writeValueAsString(msgObject));
                }
            } catch (final Exception e) {
            }
        });

        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(listener).processingStarted(new File("/path/to/file.robot"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void fileProcessingEndedEventIsHandledByListener() throws Exception {
        final RfLintClientEventsListener listener = mock(RfLintClientEventsListener.class);

        final RfLintIntegrationServer server = new RfLintIntegrationServer(e -> {});
        final Thread serverThread = new Thread(() -> {
            try {
                server.start(listener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try (final Socket clientSocket = new Socket(server.getHost(), server.getPort())) {
                server.waitForServerToSetup();
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()))) {
                    final Object msgObject = ImmutableMap.of("file_processing_ended",
                            newArrayList("/path/to/file.robot"));
                    writer.write(new ObjectMapper().writeValueAsString(msgObject));
                }
            } catch (final Exception e) {
            }
        });

        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(listener).processingEnded(new File("/path/to/file.robot"));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void violationFoundEventIsHandledByListener() throws Exception {
        final RfLintClientEventsListener listener = mock(RfLintClientEventsListener.class);

        final RfLintIntegrationServer server = new RfLintIntegrationServer(e -> {});
        final Thread serverThread = new Thread(() -> {
            try {
                server.start(listener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try (final Socket clientSocket = new Socket(server.getHost(), server.getPort())) {
                server.waitForServerToSetup();
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()))) {
                    final Object msgObject = ImmutableMap.of("violation_found",
                            newArrayList(ImmutableMap.builder()
                                    .put("filepath", "/path/to/file.robot")
                                    .put("line", 42)
                                    .put("character", 5)
                                    .put("rule_name", "Rule")
                                    .put("severity", "E")
                                    .put("message", "msg")
                                    .build()));
                    writer.write(new ObjectMapper().writeValueAsString(msgObject));
                }
            } catch (final Exception e) {
            }
        });

        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(listener).violationFound(new File("/path/to/file.robot"), 42, 5, "Rule", RfLintViolationSeverity.ERROR,
                "msg");
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void analysisFinishedWithoutErrorEventIsHandledByListener() throws Exception {
        final RfLintClientEventsListener listener = mock(RfLintClientEventsListener.class);

        final RfLintIntegrationServer server = new RfLintIntegrationServer(e -> {});
        final Thread serverThread = new Thread(() -> {
            try {
                server.start(listener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try (final Socket clientSocket = new Socket(server.getHost(), server.getPort())) {
                server.waitForServerToSetup();
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()))) {
                    final Object msgObject = ImmutableMap.of("analysis_finished", newArrayList());
                    writer.write(new ObjectMapper().writeValueAsString(msgObject));
                }
            } catch (final Exception e) {
            }
        });

        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(listener).analysisFinished();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void analysisFinishedWithErrorEventIsHandledByListener() throws Exception {
        final RfLintClientEventsListener listener = mock(RfLintClientEventsListener.class);

        final RfLintIntegrationServer server = new RfLintIntegrationServer(e -> {});
        final Thread serverThread = new Thread(() -> {
            try {
                server.start(listener);
            } catch (final IOException e) {
            }
        });
        final Thread clientThread = new Thread(() -> {
            try (final Socket clientSocket = new Socket(server.getHost(), server.getPort())) {
                server.waitForServerToSetup();
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(clientSocket.getOutputStream()))) {
                    final Object msgObject = ImmutableMap.of("analysis_finished", newArrayList("error"));
                    writer.write(new ObjectMapper().writeValueAsString(msgObject));
                }
            } catch (final Exception e) {
            }
        });

        serverThread.start();
        clientThread.start();

        serverThread.join();
        clientThread.join();

        verify(listener).analysisFinished("error");
        verifyNoMoreInteractions(listener);
    }
}
