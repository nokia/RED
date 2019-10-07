/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static java.util.stream.Collectors.toList;

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
            final int totalTests, final List<String> childSuites, final List<String> childTests,
            final List<String> childPaths) {
        Preconditions.checkArgument(isOpen);

        if (root == null) {
            this.totalTests = totalTests;
            this.mode = executionMode;
            root = new ExecutionTreeNode(null, ElementKind.SUITE, suiteName);
            current = root;
        }

        current.setStatus(Status.RUNNING);
        current.setPath(suiteFilePath);
        current.setChildrenPaths(childPaths);
        current.addChildren(childSuites.stream()
                .map(childSuite -> new ExecutionTreeNode(current, ElementKind.SUITE, childSuite))
                .collect(toList()));
        current.addChildren(childTests.stream()
                .map(childTest -> new ExecutionTreeNode(current, ElementKind.TEST, childTest, suiteFilePath,
                        childPaths))
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
            } else if (status == Status.FAIL) {
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

    public final Map<String, List<String>> getNonExecutedSuitePaths(final IProject project,
            final List<String> linkedResources) {
        final List<ExecutionTreeNode> nodes = this.getExecutionTree().getChildren();
        final boolean testsOnly = areOnlyTestsToRerun(nodes);
        final Map<String, List<String>> nonExecutedSuitePaths = new HashMap<>();
        final List<String> alreadyUsedPaths = new ArrayList<>();
        for (final ExecutionTreeNode node : nodes) {
            if (isNonExecuted(node)) {
                final List<String> nonExecutedSuitesOrTests = getNonChildren(node, testsOnly, project, linkedResources,
                        alreadyUsedPaths);

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

    private final List<String> getNonChildren(final ExecutionTreeNode node, final boolean testsOnly,
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

        if (isNonExecuted(node)) {
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

        if (isNonExecuted(node)) {
            if (node.getKind() == ElementKind.SUITE) {
                if (node.getChildren().size() != 0) {
                    for (final ExecutionTreeNode n : node.getChildren()) {
                        collectNonExecutedSuitesPaths(n, failedSuites, project, linkedResources, alreadyUsedPaths);
                    }
                } else {
                    final String path = constructProjectRelativePath(node, project, linkedResources,
                            alreadyUsedPaths);
                    failedSuites.add(path);
                }
            } else {
                final String path = constructProjectRelativePath(node.getParent(), project, linkedResources,
                        alreadyUsedPaths);
                failedSuites.add(path);
            }
        }
    }

    private String constructProjectRelativePath(final ExecutionTreeNode node, final IProject project,
            final List<String> linkedResources, final List<String> alreadyUsedPaths) {
        final String nodeName = node.getName();
        final ExecutionTreeNode parent = node.getParent();
        final List<ExecutionTreeNode> suites = parent.getChildren();
        final List<String> childPaths = parent.getChildrenPaths();

        final IWorkspaceRoot root = project.getWorkspace().getRoot();
        final RedWorkspace workspace = new RedWorkspace(root);

        try {
            try {
                final Optional<String> pathFromChildren = getPathFromChildrenPaths(nodeName, project, suites,
                        childPaths);
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
            final List<ExecutionTreeNode> suites, final List<String> childPaths) {
        for (final ExecutionTreeNode suite : suites) {
            if (nodeName.equals(suite.getName())) {
                if (!childPaths.isEmpty()) {
                    final Path childPath = Paths.get(childPaths.get(suites.indexOf(suite)));
                    final Path projectPath = Paths.get(project.getLocation().toPortableString());
                    final Path relativePath = projectPath.relativize(childPath);
                    return Optional.of(
                            org.eclipse.core.runtime.Path.fromOSString(relativePath.toString()).toPortableString());
                }
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

    private static boolean isNonExecuted(final ExecutionTreeNode node) {
        return !node.getStatus().isPresent() || node.getStatus().equals(Optional.of(Status.RUNNING));
    }

    private static boolean areOnlyTestsToRerun(final List<ExecutionTreeNode> nodes) {
        final List<String> suites = new ArrayList<>();
        for (final ExecutionTreeNode node : nodes) {
            collectSuitesNamesIfNonExecuted(node, suites);
            if (!suites.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static void collectSuitesNamesIfNonExecuted(final ExecutionTreeNode node, final List<String> suites) {
        if (isNonExecuted(node)) {
            if (node.getKind() == ElementKind.SUITE) {
                if (node.getChildren().size() == 0) {
                    suites.add(node.getName());
                } else {
                    for (final ExecutionTreeNode n : node.getChildren()) {
                        collectSuitesNamesIfNonExecuted(n, suites);
                    }
                }
            }
        }
    }
}
