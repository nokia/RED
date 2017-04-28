/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.execution.server.AgentClient;

public interface RobotAgentEventListener {

    void setClient(AgentClient client);

    boolean isHandlingEvents();

    void handleAgentInitializing();

    void handleAgentIsReadyToStart();

    void handleVersions(String pythonVersion, String robotVersion, int protocolVersion);

    void handleSuiteStarted(String name, URI suiteFilePath, int totalTests, List<String> childSuites,
            List<String> childTests);

    void handleSuiteEnded(String suiteName, int elapsedTime, Status status, String errorMessage);

    void handleTestStarted(String testCaseName, String testCaseLongName);

    void handleTestEnded(String testCaseName, String testCaseLongName, int elapsedTime, Status status,
            String errorMessage);

    void handleKeywordStarted(String keywordName, String keywordType, List<String> keywordArgs);

    void handleKeywordEnded(String keywordName, String keywordType);

    void handleResourceImport(URI resourceFilePath);

    void handleGlobalVariables(Map<String, String> globalVars);

    void handleVariables(Map<String, Object> vars);

    void handleLogMessage(String msg, LogLevel level, String timestamp);

    void handleOutputFile(URI path);

    void handleCheckCondition();

    void handleConditionError(String error);

    void handleConditionResult(boolean result);

    void handleConditionChecked();

    void handleClosed();

    void handlePaused();

    void handleMessage(String msg, LogLevel level);

    void handleLibraryImport(String name, URI importer, URI source, List<String> args);

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