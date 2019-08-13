/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.ui.services.IDisposable;
import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent.ExecutionMode;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

public class ExecutionStatusStore implements IDisposable {

    private boolean isOpen = false;
    private boolean isDisposed = false;
    private boolean isDirty = false;

    private ExecutionTreeNode root;
    private ExecutionTreeNode current;

    private int currentTest;
    private int passedTests;
    private int failedTests;
    private int totalTests;

    private URI outputFile;

    private ExecutionMode mode;

    public ExecutionTreeNode getExecutionTree() {
        return root;
    }

    @VisibleForTesting
    public void setExecutionTree(final ExecutionTreeNode root) {
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
        Preconditions.checkArgument(isOpen);

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

    ExecutionMode getMode() {
        return mode;
    }

    protected void suiteStarted(final String suiteName, final URI suiteFilePath, final ExecutionMode executionMode,
            final int totalTests, final List<String> childSuites, final List<String> childTests) {
        Preconditions.checkArgument(isOpen);

        if (root == null) {
            this.totalTests = totalTests;
            this.mode = executionMode;
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
        Preconditions.checkArgument(isOpen);

        current.setStatus(Status.RUNNING);
        currentTest++;
        isDirty = true;
    }

    protected void elementEnded(final int elapsedTime, final Status status, final String errorMessage) {
        Preconditions.checkArgument(isOpen);

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

    boolean isOpen() {
        return isOpen;
    }

    void open() {
        isOpen = true;
    }

    void close() {
        isOpen = false;
    }

    public Map<String, List<String>> getFailedSuitePaths(final IProject project) {
        final List<ExecutionTreeNode> nodes = this.getExecutionTree().getChildren();
        final Map<String, List<String>> failedSuitePaths = new HashMap<>();
        for (final ExecutionTreeNode node : nodes) {
            final List<String> failedTests = getFailedChildren(node);
            if (!failedTests.isEmpty()) {
                final IWorkspaceRoot root = project.getWorkspace().getRoot();
                final RedWorkspace workspace = new RedWorkspace(root);
                final IResource resource = workspace.forUri(node.getPath());
                final String path = resource.getProjectRelativePath().toPortableString();
                failedSuitePaths.put(path, failedTests);
            }
        }
        return failedSuitePaths;
    };

    private List<String> getFailedChildren(final ExecutionTreeNode node) {
        final List<String> failedTests = new ArrayList<>();
        collectFailedTestsPaths(node, failedTests, "");
        return failedTests;
    }

    private void collectFailedTestsPaths(final ExecutionTreeNode node, final List<String> failedTests,
            final String currentPath) {
        final ExecutionTreeNode current = node;
        String newPath = currentPath.isEmpty() ? current.getName() : currentPath + "." + current.getName();
        if (current.getKind() == ElementKind.SUITE) {
            for (final ExecutionTreeNode c : current.getChildren()) {
                collectFailedTestsPaths(c, failedTests, newPath);
            }
        } else {
            if (current.getStatus().equals(Optional.of(Status.FAIL))) {
                newPath = newPath.substring(newPath.indexOf(".") + 1);
                failedTests.add(newPath);
            }
        }
    }
}
