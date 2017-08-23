/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.List;

import org.eclipse.ui.services.IDisposable;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;

import com.google.common.annotations.VisibleForTesting;

public class ExecutionStatusStore implements IDisposable {

    private boolean isDisposed = false;
    private boolean isDirty = false;

    private ExecutionTreeNode root;
    private ExecutionTreeNode current;

    private int currentTest;
    private int passedTests;
    private int failedTests;
    private int totalTests;

    private URI outputFile;

    public ExecutionTreeNode getExecutionTree() {
        return root;
    }

    @VisibleForTesting
    void setExecutionTree(final ExecutionTreeNode root) {
        this.root = root;
    }

    @VisibleForTesting
    void setCurrent(final ExecutionTreeNode current) {
        this.current = current;
    }

    @VisibleForTesting
    ExecutionTreeNode getCurrent() {
        return current;
    }

    public URI getOutputFilePath() {
        return outputFile;
    }

    protected void setOutputFilePath(final URI outputFilepath) {
        this.outputFile = outputFilepath;
        isDirty = true;
    }

    int getCurrentTest() {
        return currentTest;
    }

    public int getPassedTests() {
        return passedTests;
    }

    public int getFailedTests() {
        return failedTests;
    }

    int getTotalTests() {
        return totalTests;
    }

    @Override
    public void dispose() {
        outputFile = null;
        currentTest = 0;
        totalTests = 0;
        passedTests = 0;
        failedTests = 0;

        root = null;
        current = null;

        isDisposed = true;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    protected void suiteStarted(final String suiteName, final URI suiteFilePath, final int totalTests,
            final List<String> childSuites, final List<String> childTests) {

        if (root == null) {
            this.totalTests = totalTests;
            root = new ExecutionTreeNode(null, ElementKind.SUITE, suiteName);
            current = root;
        }

        current.setStatus(Status.RUNNING);
        current.setPath(suiteFilePath);
        current.addChildren(childSuites.stream()
                .map(childSuite -> new ExecutionTreeNode(current, ElementKind.SUITE, childSuite))
                .collect(toList()));
        current.addChildren(childTests.stream()
                .map(childTest -> new ExecutionTreeNode(current, ElementKind.TEST, childTest, suiteFilePath))
                .collect(toList()));

        current = current.getChildren().isEmpty() ? current : current.getChildren().get(0);
        isDirty = true;
    }

    protected void testStarted() {
        current.setStatus(Status.RUNNING);
        currentTest++;
        isDirty = true;
    }

    protected void elementEnded(final int elapsedTime, final Status status, final String errorMessage) {
        current.setElapsedTime(elapsedTime);
        current.setStatus(status);
        current.setMessage(errorMessage);

        if (current.getKind() == ElementKind.TEST) {
            if (status == Status.PASS) {
                passedTests++;
            } else {
                failedTests++;
            }
        }

        final ExecutionTreeNode parent = current.getParent();
        if (parent == null) {
            current = null;
        } else {
            final int index = parent.getChildren().indexOf(current);
            if (index + 1 >= parent.getChildren().size()) {
                current = parent;
            } else {
                current = parent.getChildren().get(index + 1);
            }
        }
        isDirty = true;
    }

    boolean checkDirtyAndReset() {
        final boolean wasDirty = isDirty;
        isDirty = false;
        return wasDirty;
    }
}
