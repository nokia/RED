/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotPathsNaming;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;

public class ExecutionStatusStore implements IDisposable {

    private boolean isOpen = false;
    private boolean isDisposed = false;
    private boolean isDirty = false;

    private ExecutionTreeNode root;

    private ExecutionTreeNode currentNode;

    private int currentTest;
    private int passedTests;
    private int failedTests;

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
        this.currentNode = current;
    }

    @VisibleForTesting
    ExecutionTreeNode getCurrent() {
        return currentNode;
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

    public int getNonExecutedTests() {
        return getTotalTests() - (passedTests + failedTests);
    }

    public int getTotalTests() {
        return root == null ? 0 : root.getNumberOfTests();
    }

    @Override
    public void dispose() {
        outputFile = null;
        currentTest = 0;
        passedTests = 0;
        failedTests = 0;

        root = null;
        currentNode = null;

        isDisposed = true;
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    ExecutionMode getMode() {
        return mode;
    }

    protected void suiteStarted(final String suiteName, final URI suiteFilePath, final ExecutionMode executionMode,
            final int totalTestsInSuite, final List<String> childTests, final List<String> childSuites,
            final List<Optional<URI>> childSuitesPaths) {
        Preconditions.checkArgument(isOpen);

        final ExecutionTreeNode currentSuiteNode;
        if (currentNode == null) {
            this.mode = executionMode;
            this.root = ExecutionTreeNode.newSuiteNode(null, suiteName, suiteFilePath);

            currentSuiteNode = root;
        } else {
            currentSuiteNode = currentNode.getSuiteOrCreateIfMissing(suiteName, totalTestsInSuite);
        }

        currentSuiteNode.setStatus(Status.RUNNING);
        currentSuiteNode.setPath(suiteFilePath);
        currentSuiteNode.setNumberOfTests(totalTestsInSuite);
        Streams.zip(childSuites.stream(), childSuitesPaths.stream(), (childSuite, childPath) -> ExecutionTreeNode
                .newSuiteNode(currentSuiteNode, childSuite, childPath.orElse(null)))
                .forEach(currentSuiteNode::addChildren);
        childTests.stream()
                .map(childTest -> ExecutionTreeNode.newTestNode(currentSuiteNode, childTest, suiteFilePath))
                .forEach(currentSuiteNode::addChildren);

        this.currentNode = currentSuiteNode;

        isDirty = true;
    }

    protected void testStarted(final String testName) {
        Preconditions.checkArgument(isOpen);

        final ExecutionTreeNode currentTestNode = currentNode.getTestOrCreateIfMissing(testName);
        currentTestNode.setStatus(Status.RUNNING);

        this.currentNode = currentTestNode;

        currentTest++;
        isDirty = true;
    }

    protected void testEnded(final int elapsedTime, final Status status, final String errorMessage) {
        Preconditions.checkArgument(isOpen);

        currentNode.setElapsedTime(elapsedTime);
        currentNode.setStatus(status);
        currentNode.setMessage(errorMessage);

        this.currentNode = currentNode.getParent();

        if (status == Status.PASS) {
            passedTests++;
        } else if (status == Status.FAIL) {
            failedTests++;
        }

        isDirty = true;
    }

    protected void suiteEnded(final int elapsedTime, final Status status, final String errorMessage) {
        Preconditions.checkArgument(isOpen);

        currentNode.setElapsedTime(elapsedTime);
        currentNode.setStatus(status);
        currentNode.setMessage(errorMessage);

        currentNode.updateNumberOfTestsBalanceOnSuiteEnd();

        this.currentNode = currentNode.getParent();

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
        } else if (current.isFailed()) {
            newPath = newPath.substring(newPath.indexOf(".") + 1);
            failedTests.add(newPath);
        }
    }
    
    public Map<String, List<String>> getNonExecutedSuitePaths(final IProject project,
            final List<String> linkedResources) {
        final boolean testsOnly = onlyTestsRequiresRerun(getExecutionTree());

        final Map<String, List<String>> nonExecutedSuitePaths = new HashMap<>();
        final List<String> alreadyUsedPaths = new ArrayList<>();
        for (final ExecutionTreeNode node : getExecutionTree().getChildren()) {
            if (!node.isExecuted()) {
                final List<String> nonExecutedSuitesOrTests = getNonExecutedChildren(node, testsOnly, project,
                        linkedResources, alreadyUsedPaths);

                if (!nonExecutedSuitesOrTests.isEmpty()) {
                    if (testsOnly) {
                        final String nodePath = constructProjectRelativePath(node, project, linkedResources,
                                alreadyUsedPaths);
                        if (nonExecutedSuitePaths.keySet().contains(nodePath)) {
                            final List<String> allTests = new ArrayList<>();
                            allTests.addAll(nonExecutedSuitePaths.get(nodePath));
                            allTests.addAll(nonExecutedSuitesOrTests);
                            nonExecutedSuitePaths.put(nodePath, allTests);
                        } else {
                            nonExecutedSuitePaths.put(nodePath, nonExecutedSuitesOrTests);
                        }
                    } else {
                        for (final String suitePath : nonExecutedSuitesOrTests) {
                            nonExecutedSuitePaths.put(suitePath, new ArrayList<>());
                        }
                    }
                } else {
                    final String nodePath = constructProjectRelativePath(node, project, linkedResources,
                            alreadyUsedPaths);
                    nonExecutedSuitePaths.put(nodePath, new ArrayList<>());
                }
            }
        }
        return nonExecutedSuitePaths;
    }

    private List<String> getNonExecutedChildren(final ExecutionTreeNode node, final boolean testsOnly,
            final IProject project, final List<String> linkedResources, final List<String> alreadyUsedPaths) {
        final List<String> failedTestsOrSuites = new ArrayList<>();
        if (testsOnly) {
            collectNonExecutedTestsPaths(node, failedTestsOrSuites, "");
        } else {
            collectNonExecutedSuitesPaths(node, failedTestsOrSuites, project, linkedResources, alreadyUsedPaths);
        }
        return failedTestsOrSuites;
    }

    private void collectNonExecutedTestsPaths(final ExecutionTreeNode node, final List<String> failedTests,
            final String currentPath) {

        if (!node.isExecuted()) {
            String newPath = currentPath.isEmpty() ? node.getName() : currentPath + "." + node.getName();
            if (node.getKind() == ElementKind.SUITE) {
                for (final ExecutionTreeNode n : node.getChildren()) {
                    collectNonExecutedTestsPaths(n, failedTests, newPath);
                }
            } else {
                newPath = newPath.substring(newPath.indexOf(".") + 1);
                failedTests.add(newPath);
            }
        }
    }

    private void collectNonExecutedSuitesPaths(final ExecutionTreeNode node, final List<String> failedSuites,
            final IProject project, final List<String> linkedResources, final List<String> alreadyUsedPaths) {

        if (!node.isExecuted()) {
            if (node.getKind() == ElementKind.SUITE) {
                if (!node.getChildren().isEmpty()) {
                    for (final ExecutionTreeNode n : node.getChildren()) {
                        collectNonExecutedSuitesPaths(n, failedSuites, project, linkedResources, alreadyUsedPaths);
                    }
                } else {
                    failedSuites.add(constructProjectRelativePath(node, project, linkedResources, alreadyUsedPaths));
                }
            } else {
                failedSuites.add(
                        constructProjectRelativePath(node.getParent(), project, linkedResources, alreadyUsedPaths));
            }
        }
    }

    private String constructProjectRelativePath(final ExecutionTreeNode node, final IProject project,
            final List<String> linkedResources, final List<String> alreadyUsedPaths) {
        final String nodeName = node.getName();
        final ExecutionTreeNode parent = node.getParent();
        final List<ExecutionTreeNode> suites = parent.getChildren();

        final IWorkspaceRoot root = project.getWorkspace().getRoot();
        final RedWorkspace workspace = new RedWorkspace(root);

        try {
            try {
                final Optional<String> pathFromChildren = getPathFromChildrenPaths(nodeName, project, suites);
                if (pathFromChildren.isPresent()) {
                    alreadyUsedPaths.add("file:///" + pathFromChildren.get());
                    return pathFromChildren.get();
                }
                return getPathFromNodePath(workspace, node, project, alreadyUsedPaths);
            } catch (final Exception e) {

                return getPathFromNodePath(workspace, node.getParent(), project, alreadyUsedPaths);
            }
        } catch (final Exception e) {
            return getPathFromLinkedResources(nodeName, project, workspace, linkedResources, alreadyUsedPaths);
        }
    }

    private static Optional<String> getPathFromChildrenPaths(final String nodeName, final IProject project,
            final List<ExecutionTreeNode> suites) {
        for (final ExecutionTreeNode suite : suites) {
            if (nodeName.equals(suite.getName()) && suite.getPath() != null) {
                final Path childPath = new File(suite.getPath()).toPath();
                final Path projectPath = Paths.get(project.getLocation().toPortableString());
                final Path relativePath = projectPath.relativize(childPath);
                return Optional
                        .of(org.eclipse.core.runtime.Path.fromOSString(relativePath.toString()).toPortableString());
            }
        }
        return Optional.empty();
    }

    private static String getPathFromNodePath(final RedWorkspace workspace, final ExecutionTreeNode node,
            final IProject project, final List<String> alreadyUsedPaths) {
        final IResource resource = workspace.forUri(node.getPath());
        final String path = resource.getProjectRelativePath().toPortableString();
        alreadyUsedPaths.add("file:///" + project.getLocation() + '/' + path.replaceAll(" ", "%20"));
        return path;
    }

    private static String getPathFromLinkedResources(final String nodeName, final IProject project,
            final RedWorkspace workspace, final List<String> linkedResources, final List<String> linkedCandidates) {

        for (final String resource : linkedResources) {
            final String name = Paths.get(resource).getFileName().toString();
            final String robotName = RobotPathsNaming
                    .toRobotFrameworkName(name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name);
            if (robotName.equals(nodeName)) {
                final String path = "file:///" + project.getLocation().removeLastSegments(1)
                        + resource.replaceAll(" ", "%20");
                if (!(linkedCandidates.stream().anyMatch(candidate -> candidate.startsWith(path)))) {
                    linkedCandidates.add(path);
                    final URI pathURI = URI.create(path);
                    final IResource linkedResource = workspace.forUri(pathURI);
                    return linkedResource.getProjectRelativePath().toPortableString();
                }
            }
        }
        return nodeName;
    }

    private static boolean onlyTestsRequiresRerun(final ExecutionTreeNode node) {
        if (node.getKind() == ElementKind.SUITE && !node.isExecuted()) {
            if (node.getChildren().isEmpty()) {
                return false;
            } else {
                for (final ExecutionTreeNode n : node.getChildren()) {
                    if (!onlyTestsRequiresRerun(n)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
