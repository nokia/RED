/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.debug;

import java.io.File;

import org.rf.ide.core.execution.ExecutionElement;
import org.rf.ide.core.execution.ExecutionElementsFactory;
import org.rf.ide.core.execution.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotEventBroker;


public class ExecutionTrackerForExecutionView extends RobotDefaultAgentEventListener {

    private final RobotEventBroker robotEventBroker;

    public ExecutionTrackerForExecutionView(final RobotEventBroker robotEventBroker) {
        this.robotEventBroker = robotEventBroker;
    }

    @Override
    public void handleOutputFile(final File outputFilepath) {
        final ExecutionElement execElement = ExecutionElementsFactory.createOutputFileExecutionElement(outputFilepath);
        robotEventBroker.sendExecutionEventToExecutionView(execElement);
    }

    @Override
    public void handleSuiteStarted(final String suiteName, final File suiteFilePath) {
        final ExecutionElement execElement = ExecutionElementsFactory.createStartSuiteExecutionElement(suiteName,
                suiteFilePath);
        robotEventBroker.sendExecutionEventToExecutionView(execElement);
    }

    @Override
    public void handleSuiteEnded(final String suiteName, final int elapsedTime, final Status status,
            final String errorMessage) {
        final ExecutionElement execElement = ExecutionElementsFactory.createEndSuiteExecutionElement(suiteName,
                elapsedTime, errorMessage, status);
        robotEventBroker.sendExecutionEventToExecutionView(execElement);
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        final ExecutionElement execElement = ExecutionElementsFactory.createStartTestExecutionElement(testCaseName);
        robotEventBroker.sendExecutionEventToExecutionView(execElement);
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        final ExecutionElement execElement = ExecutionElementsFactory.createEndTestExecutionElement(testCaseName,
                elapsedTime, errorMessage, status);
        robotEventBroker.sendExecutionEventToExecutionView(execElement);
    }
}
