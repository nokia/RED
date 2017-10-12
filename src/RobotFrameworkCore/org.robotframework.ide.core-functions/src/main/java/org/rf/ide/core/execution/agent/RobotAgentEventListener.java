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

public interface RobotAgentEventListener {

    void eventsProcessingAboutToStart();

    boolean isHandlingEvents();

    void handleAgentInitializing(AgentInitializingEvent event);

    void handleAgentIsReadyToStart(ReadyToStartEvent event);

    void handleVersions(VersionsEvent event);

    void handleSuiteStarted(SuiteStartedEvent event);

    void handleSuiteEnded(SuiteEndedEvent event);

    void handleTestStarted(TestStartedEvent event);

    void handleTestEnded(TestEndedEvent event);

    void handleKeywordAboutToStart(KeywordStartedEvent event);

    void handleKeywordStarted(KeywordStartedEvent event);

    void handleKeywordAboutToEnd(KeywordEndedEvent event);

    void handleKeywordEnded(KeywordEndedEvent event);

    void handleResourceImport(ResourceImportEvent event);

    void handleLibraryImport(LibraryImportEvent event);

    void handleVariables(VariablesEvent event);

    void handleLogMessage(MessageEvent event);

    void handleMessage(MessageEvent event);

    void handleOutputFile(OutputFileEvent event);

    void handleShouldContinue(ShouldContinueEvent event);

    void handleConditionEvaluated(ConditionEvaluatedEvent event);

    void handlePaused(PausedEvent event);

    void handleResumed();

    void handleClosed();

    void eventsProcessingFinished();

    public static class RobotAgentEventsListenerException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public RobotAgentEventsListenerException(final String message) {
            super(message);
        }

        public RobotAgentEventsListenerException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }
}