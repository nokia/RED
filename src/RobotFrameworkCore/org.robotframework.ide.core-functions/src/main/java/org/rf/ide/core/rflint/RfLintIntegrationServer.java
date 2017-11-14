/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.rflint;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.synchronizedList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.Iterables;

public class RfLintIntegrationServer {

    public static final int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (final IOException e) {
            return -1;
        }
    }

    private final int port;

    private final String host;

    private final int timeoutInMillis;

    private final Consumer<Exception> onServerException;

    private final Semaphore serverSetupSemaphore = new Semaphore(0);

    private ServerSocket serverSocket;

    private Socket clientSocket;

    private boolean stopped;

    public RfLintIntegrationServer(final Consumer<Exception> onServerException) {
        this(30, TimeUnit.SECONDS, onServerException);
    }

    public RfLintIntegrationServer(final int timeout, final TimeUnit timoutUnit,
            final Consumer<Exception> onServerException) {
        this.host = "127.0.0.1";
        this.port = findFreePort();
        this.timeoutInMillis = (int) TimeUnit.MILLISECONDS.convert(timeout, timoutUnit);
        this.onServerException = onServerException;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void start(final RfLintClientEventsListener... eventsListeners) throws IOException {
        try {
            serverSocket = new ServerSocket(port, 50, InetAddress.getByName(host));
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(timeoutInMillis);

            serverSetupSemaphore.release();
            try (Socket clientSocket = serverSocket.accept()) {
                this.clientSocket = clientSocket;
                final BufferedReader eventsReader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                runEventsLoop(eventsReader, eventsListeners);
            } catch (final Exception e) {
                if (!stopped) {
                    // report only when raised because of different action than stop() call
                    onServerException.accept(e);
                }
            }
        } catch (final Exception e) {
            onServerException.accept(e);
        } finally {
            stop();
        }
    }

    public void waitForServerToSetup() throws InterruptedException {
        serverSetupSemaphore.acquire();
    }

    public void stop() throws IOException {
        stopped = true;
        if (clientSocket != null) {
            clientSocket.close();
        }
        if (serverSocket != null) {
            serverSocket.close();
        }
    }

    private void runEventsLoop(final BufferedReader eventReader, final RfLintClientEventsListener... listeners)
            throws IOException {
        final List<RfLintClientEventsListener> eventsListeners = synchronizedList(newArrayList(listeners));

        String event = eventReader.readLine();
        final ObjectMapper mapper = new ObjectMapper();
        while (event != null) {

            final TypeReference<Map<String, Object>> stringToObjectMapType = new TypeReference<Map<String, Object>>() {
            };
            final Map<String, Object> eventMap = mapper.readValue(event, stringToObjectMapType);
            final String eventType = getEventType(eventMap);
            if (eventType == null) {
                event = eventReader.readLine();
                continue;
            }

            switch (eventType) {
                case "files_to_process":
                    handleFilesToProcess(eventsListeners, eventMap);
                    break;
                case "file_processing_started":
                    handleFileProcessingStarted(eventsListeners, eventMap);
                    break;
                case "file_processing_ended":
                    handleFileProcessingEnded(eventsListeners, eventMap);
                    break;
                case "violation_found":
                    handleViolationFound(eventsListeners, eventMap);
                    break;
                case "analysis_finished":
                    handleAnalysisFinished(eventsListeners, eventMap);
                default:
                    throw new IllegalStateException();
            }
            event = eventReader.readLine();
        }
    }

    private void handleFilesToProcess(final List<RfLintClientEventsListener> eventsListeners,
            final Map<String, Object> eventMap) {
        final int numberOfFiles = (Integer) ((List<?>) eventMap.get("files_to_process")).get(0);
        eventsListeners.forEach(listener -> listener.filesToProcess(numberOfFiles));
    }

    private void handleFileProcessingStarted(final List<RfLintClientEventsListener> eventsListeners,
            final Map<String, Object> eventMap) {
        final File filepath = new File((String) ((List<?>) eventMap.get("file_processing_started")).get(0));
        eventsListeners.forEach(listener -> listener.processingStarted(filepath));
    }

    private void handleFileProcessingEnded(final List<RfLintClientEventsListener> eventsListeners,
            final Map<String, Object> eventMap) {
        final File filepath = new File((String) ((List<?>) eventMap.get("file_processing_ended")).get(0));
        eventsListeners.forEach(listener -> listener.processingEnded(filepath));

    }

    private void handleViolationFound(final List<RfLintClientEventsListener> eventsListeners,
            final Map<String, Object> eventMap) {
        final Map<String, ?> args = ensureOrderedMapOfStringsToObjects(
                (Map<?, ?>) ((List<?>) eventMap.get("violation_found")).get(0));
        final File filepath = new File((String) args.get("filepath"));
        final Integer line = (Integer) args.get("line");
        final Integer character = (Integer) args.get("character");
        final String ruleName = (String) args.get("rule_name");
        final RfLintViolationSeverity severity = RfLintViolationSeverity.from((String) args.get("severity"));
        final String message = (String) args.get("message");

        eventsListeners
                .forEach(listener -> listener.violationFound(filepath, line, character, ruleName, severity, message));
    }

    private void handleAnalysisFinished(final List<RfLintClientEventsListener> eventsListeners,
            final Map<String, Object> eventMap) {
        final List<?> args = (List<?>) eventMap.get("analysis_finished");
        if (args.isEmpty()) {
            eventsListeners.forEach(RfLintClientEventsListener::analysisFinished);
        } else {
            final String errorMsg = (String) args.get(0);
            eventsListeners.forEach(listener -> listener.analysisFinished(errorMsg));
        }
    }

    private String getEventType(final Map<String, ?> eventMap) {
        if (eventMap == null) {
            return null;
        }
        return Iterables.getFirst(eventMap.keySet(), null);
    }

    static Map<String, Object> ensureOrderedMapOfStringsToObjects(final Map<?, ?> map) {
        final LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        map.entrySet().stream().forEach(e -> result.put((String) e.getKey(), e.getValue()));
        return result;
    }
}
