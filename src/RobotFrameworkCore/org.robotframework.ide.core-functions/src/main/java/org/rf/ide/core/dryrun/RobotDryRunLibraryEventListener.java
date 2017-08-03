/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.dryrun;

import java.util.function.Consumer;

import org.rf.ide.core.execution.agent.LogLevel;
import org.rf.ide.core.execution.agent.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.server.response.ContinueExecution;
import org.rf.ide.core.execution.server.response.ServerResponse.ResponseException;

public class RobotDryRunLibraryEventListener extends RobotDefaultAgentEventListener {

    private final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector;

    private final Consumer<String> startSuiteHandler;

    public RobotDryRunLibraryEventListener(final RobotDryRunLibraryImportCollector dryRunLibraryImportCollector,
            final Consumer<String> startSuiteHandler) {
        this.dryRunLibraryImportCollector = dryRunLibraryImportCollector;
        this.startSuiteHandler = startSuiteHandler;
    }

    @Override
    public void handleSuiteStarted(final SuiteStartedEvent event) {
        startSuiteHandler.accept(event.getName());
    }

    @Override
    public void handleLibraryImport(final LibraryImportEvent event) {
        dryRunLibraryImportCollector.collectFromLibraryImportEvent(event);
    }

    @Override
    public void handleMessage(final MessageEvent event) {
        if (event.getLevel() == LogLevel.FAIL) {
            dryRunLibraryImportCollector.collectFromFailMessageEvent(event.getMessage());
        } else if (event.getLevel() == LogLevel.ERROR) {
            dryRunLibraryImportCollector.collectFromErrorMessageEvent(event.getMessage());
        }
    }

    @Override
    public void handleShouldContinue(final ShouldContinueEvent event) {
        try {
            event.responder().respond(new ContinueExecution());
        } catch (final ResponseException e) {
            throw new RobotAgentEventsListenerException("Unable to send response to client", e);
        }
    }
}
