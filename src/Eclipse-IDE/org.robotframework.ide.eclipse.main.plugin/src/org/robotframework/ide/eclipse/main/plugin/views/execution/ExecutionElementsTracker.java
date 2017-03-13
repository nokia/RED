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
        addExecutionElement(ExecutionElementsFactory.createOutputFileExecutionElement(outputFilepath));
    }

    @Override
    public void handleSuiteStarted(final String suiteName, final File suiteFilePath) {
        addExecutionElement(ExecutionElementsFactory.createStartSuiteExecutionElement(suiteName, suiteFilePath));
    }

    @Override
    public void handleSuiteEnded(final String suiteName, final int elapsedTime, final Status status,
            final String errorMessage) {
        addExecutionElement(
                ExecutionElementsFactory.createEndSuiteExecutionElement(suiteName, elapsedTime, errorMessage, status));
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        addExecutionElement(ExecutionElementsFactory.createStartTestExecutionElement(testCaseName));
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        addExecutionElement(ExecutionElementsFactory.createEndTestExecutionElement(testCaseName, elapsedTime,
                errorMessage, status));
    }

    private void addExecutionElement(final ExecutionElement element) {
        testsLaunchContext.performOnExecutionData(ExecutionElementsStore.class, store -> store.addElement(element));
    }
}
