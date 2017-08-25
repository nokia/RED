/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution.agent;

import org.rf.ide.core.execution.agent.event.AgentInitializingEvent;
import org.rf.ide.core.execution.agent.event.ConditionEvaluatedEvent;
import org.rf.ide.core.execution.agent.event.KeywordEndedEvent;
import org.rf.ide.core.execution.agent.event.KeywordStartedEvent;
import org.rf.ide.core.execution.agent.event.LibraryImportEvent;
import org.rf.ide.core.execution.agent.event.MessageEvent;
import org.rf.ide.core.execution.agent.event.OutputFileEvent;
import org.rf.ide.core.execution.agent.event.PausedEvent;
import org.rf.ide.core.execution.agent.event.ReadyToStartEvent;
import org.rf.ide.core.execution.agent.event.ResourceImportEvent;
import org.rf.ide.core.execution.agent.event.ShouldContinueEvent;
import org.rf.ide.core.execution.agent.event.SuiteEndedEvent;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent;
import org.rf.ide.core.execution.agent.event.TestEndedEvent;
import org.rf.ide.core.execution.agent.event.TestStartedEvent;
import org.rf.ide.core.execution.agent.event.VariablesEvent;
import org.rf.ide.core.execution.agent.event.VersionsEvent;

public abstract class RobotDefaultAgentEventListener implements RobotAgentEventListener {

    @Override
    public boolean isHandlingEvents() {
        return false;
    }

    @Override
    public void handleAgentInitializing(final AgentInitializingEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleAgentIsReadyToStart(final ReadyToStartEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleVersions(final VersionsEvent event) {
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
    public void handleKeywordAboutToStart(final KeywordStartedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleKeywordStarted(final KeywordStartedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleKeywordAboutToEnd(final KeywordEndedEvent event) {
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
    public void handleLibraryImport(final LibraryImportEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleVariables(final VariablesEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleLogMessage(final MessageEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleMessage(final MessageEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleOutputFile(final OutputFileEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleShouldContinue(final ShouldContinueEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleConditionEvaluated(final ConditionEvaluatedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handlePaused(final PausedEvent event) {
        // implement in subclasses
    }

    @Override
    public void handleResumed() {
        // implement in subclasses
    }

    @Override
    public void handleClosed() {
        // implement in subclasses
    }

    @Override
    public void eventsProcessingFinished() {
        // implement in subclasses
    }
}
