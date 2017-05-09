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

public interface RobotAgentEventListener {

    void setClient(AgentClient client);

    boolean isHandlingEvents();

    void handleAgentInitializing();

    void handleAgentIsReadyToStart();

    void handleVersions(String pythonVersion, String robotVersion, int protocolVersion);

    void handleSuiteStarted(SuiteStartedEvent event);

    void handleSuiteEnded(SuiteEndedEvent event);

    void handleTestStarted(TestStartedEvent event);

    void handleTestEnded(TestEndedEvent event);

    void handleKeywordStarted(KeywordStartedEvent event);

    void handleKeywordEnded(KeywordEndedEvent event);

    void handleResourceImport(ResourceImportEvent event);

    void handleGlobalVariables(Map<String, String> globalVars);

    void handleVariables(Map<String, Object> vars);

    void handleLogMessage(String msg, LogLevel level, String timestamp);

    void handleOutputFile(OutputFileEvent event);

    void handleCheckCondition();

    void handleConditionError(String error);

    void handleConditionResult(boolean result);

    void handleConditionChecked();

    void handleClosed();

    void handlePaused();

    void handleMessage(String msg, LogLevel level);

    void handleLibraryImport(LibraryImportEvent event);

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