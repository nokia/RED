/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.io.File;

import org.rf.ide.core.execution.ExecutionElement;
import org.rf.ide.core.execution.ExecutionElementsFactory;
import org.rf.ide.core.execution.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;


public class ExecutionElementsTracker extends RobotDefaultAgentEventListener {

    private final RobotTestsLaunch testsLaunchContext;

    public ExecutionElementsTracker(final RobotTestsLaunch testsLaunchContext) {
        this.testsLaunchContext = testsLaunchContext;
    }

    @Override
    public void handleAgentInitializing() {
        testsLaunchContext.getExecutionData(ExecutionElementsStore.class, ExecutionElementsStore::new);
    }

    @Override
    public void handleOutputFile(final File outputFilepath) {
        final ExecutionElement execElement = ExecutionElementsFactory.createOutputFileExecutionElement(outputFilepath);
        testsLaunchContext.getExecutionData(ExecutionElementsStore.class)
                .ifPresent(store -> store.addElement(execElement));
    }

    @Override
    public void handleSuiteStarted(final String suiteName, final File suiteFilePath) {
        final ExecutionElement execElement = ExecutionElementsFactory.createStartSuiteExecutionElement(suiteName,
                suiteFilePath);
        testsLaunchContext.getExecutionData(ExecutionElementsStore.class)
                .ifPresent(store -> store.addElement(execElement));
    }

    @Override
    public void handleSuiteEnded(final String suiteName, final int elapsedTime, final Status status,
            final String errorMessage) {
        final ExecutionElement execElement = ExecutionElementsFactory.createEndSuiteExecutionElement(suiteName,
                elapsedTime, errorMessage, status);
        testsLaunchContext.getExecutionData(ExecutionElementsStore.class)
                .ifPresent(store -> store.addElement(execElement));
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        final ExecutionElement execElement = ExecutionElementsFactory.createStartTestExecutionElement(testCaseName);
        testsLaunchContext.getExecutionData(ExecutionElementsStore.class)
                .ifPresent(store -> store.addElement(execElement));
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        final ExecutionElement execElement = ExecutionElementsFactory.createEndTestExecutionElement(testCaseName,
                elapsedTime, errorMessage, status);
        testsLaunchContext.getExecutionData(ExecutionElementsStore.class)
                .ifPresent(store -> store.addElement(execElement));
    }
}
