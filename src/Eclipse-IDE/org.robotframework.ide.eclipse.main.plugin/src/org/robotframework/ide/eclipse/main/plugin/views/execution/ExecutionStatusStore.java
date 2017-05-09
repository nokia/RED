/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static com.google.common.base.Predicates.instanceOf;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.services.IDisposable;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;

import com.google.common.annotations.VisibleForTesting;

public class ExecutionStatusStore implements IDisposable {

    private boolean isDisposed;

    private ExecutionTreeNode root;
    private ExecutionTreeNode current;

    private int currentTest;
    private int passedTests;
    private int failedTests;
    private int totalTests;

    private URI outputFile;

    private final List<ExecutionStatusStoreListener> storeListeners = new ArrayList<>();

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

        storeListeners.clear();
        isDisposed = true;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    protected void suiteStarted(final String suiteName, final URI suiteFilePath, final int totalTests,
            final List<String> childSuites, final List<String> childTests) {

        boolean notifyAboutProgress = false;
        if (root == null) {
            this.totalTests = totalTests;
            root = new ExecutionTreeNode(null, ElementKind.SUITE, suiteName);
            current = root;
            notifyAboutProgress = true;
        }

        current.setStatus(Status.RUNNING);
        current.setPath(suiteFilePath);
        current.addChildren(childSuites.stream()
                .map(childSuite -> new ExecutionTreeNode(current, ElementKind.SUITE, childSuite))
                .collect(toList()));
        current.addChildren(childTests.stream()
                .map(childTest -> new ExecutionTreeNode(current, ElementKind.TEST, childTest, suiteFilePath))
                .collect(toList()));

        final ExecutionTreeNode previous = current;
        current = current.getChildren().isEmpty() ? current : current.getChildren().get(0);

        notifyTreeChanges(previous);
        if (notifyAboutProgress) {
            notifyProgress();
        }
    }

    protected void testStarted() {
        current.setStatus(Status.RUNNING);
        currentTest++;
        notifyTreeChanges(current);
        notifyProgress();
    }

    protected void elementEnded(final int elapsedTime, final Status status, final String errorMessage) {
        current.setElapsedTime(elapsedTime);
        current.setStatus(status);
        current.setMessage(errorMessage);

        boolean notifyAboutProgress = false;
        if (current.getKind() == ElementKind.TEST) {
            if (status == Status.PASS) {
                passedTests++;
            } else {
                failedTests++;
            }
            notifyAboutProgress = true;
        }

        final ExecutionTreeNode previous = current;
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

        notifyTreeChanges(previous);
        if (notifyAboutProgress) {
            notifyProgress();
        }
    }

    void addTreeListener(final ExecutionTreeElementListener storeListener) {
        storeListeners.add(storeListener);
    }

    void addProgressListener(final ExecutionProgressListener storeListener) {
        storeListeners.add(storeListener);
    }

    @VisibleForTesting
    List<ExecutionStatusStoreListener> getListeners() {
        return storeListeners;
    }

    private void notifyTreeChanges(final ExecutionTreeNode node) {
        storeListeners.stream()
                .filter(instanceOf(ExecutionTreeElementListener.class))
                .map(ExecutionTreeElementListener.class::cast)
                .forEach(l -> l.nodeChanged(this, node));
    }

    private void notifyProgress() {
        storeListeners.stream()
                .filter(instanceOf(ExecutionProgressListener.class))
                .map(ExecutionProgressListener.class::cast)
                .forEach(l -> l.progressChanged(currentTest, passedTests, failedTests, totalTests));
    }

    void removeStoreListener(final ExecutionStatusStoreListener... storeListeners) {
        for (final ExecutionStatusStoreListener storeListener : storeListeners) {
            this.storeListeners.remove(storeListener);
        }
    }

    protected static interface ExecutionStatusStoreListener {
        // just a common interface to store lambdas on single list
    }

    @FunctionalInterface
    protected static interface ExecutionTreeElementListener extends ExecutionStatusStoreListener {

        void nodeChanged(ExecutionStatusStore store, ExecutionTreeNode node);
    }

    @FunctionalInterface
    protected static interface ExecutionProgressListener extends ExecutionStatusStoreListener {

        void progressChanged(int currentTest, int passedSoFar, int failedSoFar, int totalTests);
    }
}
