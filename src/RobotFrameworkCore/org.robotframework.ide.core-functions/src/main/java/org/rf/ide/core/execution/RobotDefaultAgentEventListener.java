/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.execution;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.rf.ide.core.execution.server.AgentClient;

public abstract class RobotDefaultAgentEventListener implements RobotAgentEventListener {

    @Override
    public void setClient(final AgentClient client) {
        // those listeners which want to talk back to client should use given object for this purposes
    }
    
    @Override
    public boolean isHandlingEvents() {
        return false;
    }

    @Override
    public void handleAgentIsReadyToStart() {
        // implement in subclasses
    }

    @Override
    public void handlePid() {
        // implement in subclasses
    }

    @Override
    public void handleVersions(final String pythonVersion, final String robotVersion) {
        // implement in subclasses
    }

    @Override
    public void handleSuiteStarted(final String suiteName, final File suiteFilePath) {
        // implement in subclasses
    }

    @Override
    public void handleSuiteEnded(final String suiteName, final int elapsedTime, final Status status,
            final String errorMessage) {
        // implement in subclasses
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        // implement in subclasses
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        // implement in subclasses
    }

    @Override
    public void handleKeywordStarted(final String keywordName, final String keywordType,
            final List<String> keywordArgs) {
        // implement in subclasses
    }

    @Override
    public void handleKeywordEnded(final String keywordName, final String keywordType) {
        // implement in subclasses
    }

    @Override
    public void handleResourceImport(final File resourceFilePath) {
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
    public void handleOutputFile(final File outputFilepath) {
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
}
