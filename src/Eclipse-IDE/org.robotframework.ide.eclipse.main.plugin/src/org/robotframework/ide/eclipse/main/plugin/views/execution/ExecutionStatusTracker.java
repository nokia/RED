/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.net.URI;
import java.util.List;

import org.rf.ide.core.execution.RobotDefaultAgentEventListener;
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.launch.RobotTestExecutionService.RobotTestsLaunch;


public class ExecutionStatusTracker extends RobotDefaultAgentEventListener {

    private final RobotTestsLaunch testsLaunchContext;

    public ExecutionStatusTracker(final RobotTestsLaunch testsLaunchContext) {
        this.testsLaunchContext = testsLaunchContext;
    }

    @Override
    public void handleAgentInitializing() {
        testsLaunchContext.getExecutionData(ExecutionStatusStore.class, ExecutionStatusStore::new);
    }

    @Override
    public void handleSuiteStarted(final String suiteName, final URI suiteFilePath, final int totalTests,
            final List<String> childSuites, final List<String> childTests) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.suiteStarted(suiteName, suiteFilePath, totalTests, childSuites, childTests));
    }

    @Override
    public void handleSuiteEnded(final String suiteName, final int elapsedTime, final Status status,
            final String errorMessage) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.elementEnded(elapsedTime, status, errorMessage));
    }

    @Override
    public void handleTestStarted(final String testCaseName, final String testCaseLongName) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class, 
                store -> store.testStarted());
    }

    @Override
    public void handleTestEnded(final String testCaseName, final String testCaseLongName, final int elapsedTime,
            final Status status, final String errorMessage) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.elementEnded(elapsedTime, status, errorMessage));
    }

    @Override
    public void handleOutputFile(final URI outputFilepath) {
        testsLaunchContext.performOnExecutionData(ExecutionStatusStore.class,
                store -> store.setOutputFilePath(outputFilepath));
    }

}
