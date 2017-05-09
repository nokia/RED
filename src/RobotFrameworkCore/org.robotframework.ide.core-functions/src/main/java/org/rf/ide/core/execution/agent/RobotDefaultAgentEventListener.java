/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent;

import java.util.Map;

import org.rf.ide.core.execution.agent.event.KeywordEndedEvent;
import org.rf.ide.core.execution.agent.event.KeywordStartedEvent;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.OutputFileEvent;
import org.rf.ide.core.execution.agent.event.ResourceImportEvent;
import org.rf.ide.core.execution.agent.event.SuiteEndedEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;
import org.rf.ide.core.execution.server.AgentClient;

public abstract class RobotDefaultAgentEventListener implements RobotAgentEventListener {

    @Override
    public void setClient(final AgentClient client) {
        // those listeners which want to talk back to client should use given object for this
        // purposes
    }

    @Override
    public boolean isHandlingEvents() {
        return false;
    }

    @Override
    public void handleAgentInitializing() {
        // implement in subclasses
    }

    @Override
    public void handleAgentIsReadyToStart() {
        // implement in subclasses
    }

    @Override
    public void handleVersions(final String pythonVersion, final String robotVersion, final int protocolVersion) {
        // implement in subclasses
    }

    @Override
    public void handleSuiteStarted(final SuiteStartedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleSuiteEnded(final SuiteEndedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleTestStarted(final TestStartedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleTestEnded(final TestEndedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleKeywordStarted(final KeywordStartedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleKeywordEnded(final KeywordEndedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleResourceImport(final ResourceImportEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleGlobalVariables(final Map<String, String> globalVars) {
        // implement in subclasses
    }

    @Override
    public void handleVariables(final Map<String, Object> vars) {
        // implement in subclasses
    }

    @Override
    public void handleLogMessage(final String msg, final LogLevel level, final String timestamp) {
        // implement in subclasses
    }

    @Override
    public void handleOutputFile(final OutputFileEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleCheckCondition() {
        // implement in subclasses
    }

    @Override
    public void handleConditionError(final String error) {
        // implement in subclasses
    }

    @Override
    public void handleConditionResult(final boolean result) {
        // implement in subclasses
    }

    @Override
    public void handleConditionChecked() {
        // implement in subclasses
    }

    @Override
    public void handleClosed() {
        // implement in subclasses
    }

    @Override
    public void handlePaused() {
        // implement in subclasses
    }

    @Override
    public void handleMessage(final String msg, final LogLevel level) {
        // implement in subclasses
    }

    @Override
    public void handleLibraryImport(final LibraryImportEvent event) {
        // implement in subclasses
    }
}
