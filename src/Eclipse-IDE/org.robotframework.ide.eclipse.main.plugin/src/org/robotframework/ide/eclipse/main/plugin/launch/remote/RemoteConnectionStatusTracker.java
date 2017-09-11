/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch.remote;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.VersionsEvent;
import org.rf.ide.core.execution.server.AgentServerStatusListener;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotConsoleFacade;

import com.google.common.base.Splitter;

class RemoteConnectionStatusTracker extends RobotDefaultAgentEventListener implements AgentServerStatusListener {

    private static final String POISON_PILL = "POISON_PILL";

    private final BlockingQueue<String> messagesQueue = new ArrayBlockingQueue<>(50);

    private RobotConsoleFacade redConsole;

    @Override
    public void serverEstablished(final String host, final int port) {
        writeMessageLine("Remote execution server is listening on " + host + ":" + port
                + ", you can execute tests on remote client");
    }

    @Override
    public void clientConnected(final int clientId) {
        writeMessageLine("Remote connection has been established (client id #" + clientId + ")");
    }

    @Override
    public void clientConnectionClosed(final int clientId) {
        writeMessageLine("Remote connection closed (client id #" + clientId + ")");
        endQueueing();
    }

    @Override
    public void clientConnectionTimedOut(final SocketTimeoutException e) {
        writeMessageLine("A timeout was reached while waiting for a remote connection");
        endQueueing();
    }

    @Override
    public void clientConnectionError(final IOException e) {
        writeMessageLine("Connection error occurred:\n" + indentMessage(e.getMessage()));
        endQueueing();
    }

    @Override
    public void clientEventHandlingError(final RobotAgentEventsListenerException e) {
        writeMessageLine("Error occurred when communicating with agent:\n" + indentMessage(e.getMessage()));
        endQueueing();
    }

    private static String indentMessage(final String message) {
        final List<String> msgLines = Splitter.on('\n').splitToList(message);
        return "\t" + String.join("\n\t", msgLines);
    }

    @Override
    public void handleVersions(final VersionsEvent event) {
        writeMessageLine("client command line: " + event.getCommandLine());
        writeMessageLine("client python version: " + event.getPythonVersion());
        writeMessageLine("client robot version: " + event.getRobotVersion());
    }

    @Override
    public void handleClosed() {
        writeMessageLine("tests finished on client machine");
    }

    private void writeMessageLine(final String message) {
        try {
            messagesQueue.put(message);
        } catch (final InterruptedException e) {
            RedPlugin.logError("Interrupted when queuing messages for remote console", e);
        }
    }

    private void endQueueing() {
        try {
            messagesQueue.put(POISON_PILL);
        } catch (final InterruptedException e) {
            RedPlugin.logError("Interrupted when queuing messages for remote console", e);
        }
    }

    void startTrackingInto(final RobotConsoleFacade redConsole) {
        this.redConsole = redConsole;
        new Thread(this::writeMessagesToStream).start();
    }

    private void writeMessagesToStream() {
        try {
            // take with timeout for the first time, since maybe nothing was written if
            // server failed before setting up
            String msg = messagesQueue.poll(10_000, TimeUnit.MILLISECONDS);
            if (msg == null) {
                return;
            }
            while (!POISON_PILL.equals(msg)) {
                redConsole.writeLine(msg);
                msg = messagesQueue.take();
            }
        } catch (final IOException e) {
            // this is fine - user disposed the console, so there is no place to write messages
            return;

        } catch (final InterruptedException e) {
            RedPlugin.logError("Interrupted when writing remote messages to console", e);
        }
    }
}
