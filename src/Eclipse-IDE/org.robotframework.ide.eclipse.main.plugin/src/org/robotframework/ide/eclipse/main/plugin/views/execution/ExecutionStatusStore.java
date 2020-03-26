/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.services.IDisposable;
import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent.ExecutionMode;
import org.rf.ide.core.testdata.model.table.TaskTable;
import org.rf.ide.core.testdata.model.table.TestCaseTable;
import org.rf.ide.core.testdata.model.table.tasks.Task;
import org.rf.ide.core.testdata.model.table.testcases.TestCase;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedWorkspace;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfigurationHelper;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotPathsNaming;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFileSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotTasksSection;
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

    public ExecutionMode getMode() {
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

    protected void testStarted(final String testName, final String resolvedTestName) {
        Preconditions.checkArgument(isOpen);

        final ExecutionTreeNode currentTestNode = currentNode.getTestOrCreateIfMissing(testName, resolvedTestName);
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
                if (path.isEmpty()) {
                    final Map<String, List<String>> failedTestsMap = createFailedTestsMapFromTestsList(workspace,
                            node.getChildren(), failedTests);
                    failedSuitePaths.putAll(failedTestsMap);
                } else {
                    failedSuitePaths.put(path, failedTests);
                }
            }
        }
        return failedSuitePaths;
    }

    private Map<String, List<String>> createFailedTestsMapFromTestsList(final RedWorkspace workspace,
            final List<ExecutionTreeNode> nodes, final List<String> failedTests) {
        final Map<String, List<String>> testsMap = new HashMap<>();
        String parentName = "";
        List<String> tests = new ArrayList<>();
        for (final String test : failedTests) {
            final String currentName = test.split("\\.")[0];
            final String currentTest = test.substring(test.indexOf(".") + 1);
            if (parentName.equals(currentName)) {
                tests.add(currentTest);
            } else {
                if (!parentName.isEmpty()) {
                    testsMap.put(getParentFileName(workspace, nodes, parentName), tests);
                }
                parentName = currentName;
                tests = new ArrayList<>();
                tests.add(currentTest);
            }
        }
        testsMap.put(getParentFileName(workspace, nodes, parentName), tests);
        return testsMap;
    }

    private String getParentFileName(final RedWorkspace workspace, final List<ExecutionTreeNode> nodes,
            final String parentRobotName) {
        for (final ExecutionTreeNode node : nodes) {
            final IResource resource = workspace.forUri(node.getPath());
            final String fileName = resource.getName();
            if (RobotPathsNaming.toRobotFrameworkName(fileName).equals(parentRobotName)) {
                return fileName;
            }
        }
        return "";
    }

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

    public List<String> getNonExecutedTestsOrTasksPaths(final IProject project,
            final RobotLaunchConfiguration robotConfig) throws CoreException {
        final List<String> nonExecutedTestsOrTasks = new ArrayList<>();
        final List<String> alreadyUsedPaths = new ArrayList<>();
        final Map<String, List<String>> suitePaths = robotConfig.getSelectedSuitePaths();
        final Map<IResource, List<String>> selectedResources = RobotLaunchConfigurationHelper
                .findResources(project, suitePaths);
        final Map<IResource, List<String>> linkedResources = RobotLaunchConfigurationHelper
                .findLinkedResources(selectedResources);

        final List<IResource> dataSources = new ArrayList<>();
        dataSources.add(project);
        dataSources.addAll(linkedResources.keySet());
        for (final ExecutionTreeNode node : getExecutionTree().getChildren()) {
            if (!node.isExecuted()) {
                final List<String> nonExecutedChildren = getNonExecutedChildren(node, project,
                        linkedResources, alreadyUsedPaths);

                String topLevelSuiteName = RobotLaunchConfigurationHelper.createTopLevelSuiteName(dataSources,
                        RobotLaunchConfigurationHelper.parseArguments(robotConfig.getRobotArguments()));
                if (RedPlugin.getDefault().getPreferences().shouldUseSingleFileDataSource()) {
                    if (linkedResources.keySet().size() <= 1) {
                        topLevelSuiteName = node.getParent().getName();
                    }
                } else {
                    topLevelSuiteName = topLevelSuiteName.equals("") ? project.getName() : topLevelSuiteName;
                }

                for (final String child : nonExecutedChildren) {
                    final String path = topLevelSuiteName + "." + child;
                    nonExecutedTestsOrTasks.add(path);
                }
            }
        }
        return nonExecutedTestsOrTasks;
    }

    private List<String> getNonExecutedChildren(final ExecutionTreeNode node, final IProject project,
            final Map<IResource, List<String>> linkedResources, final List<String> alreadyUsedPath) {
        final List<String> nonExecutedTestsOrTasks = new ArrayList<>();
        collectNonExecutedTestsOrTasksPaths(node, nonExecutedTestsOrTasks, "", project, linkedResources, alreadyUsedPath);
        return nonExecutedTestsOrTasks;
    }

    private void collectNonExecutedTestsOrTasksPaths(final ExecutionTreeNode node, final List<String> nonExecutedTestsOrTasks,
            final String currentPath, final IProject project, final Map<IResource, List<String>> linkedResources,
            final List<String> alreadyUsedPaths) {

        if (!node.isExecuted()) {
            final String newPath = currentPath.isEmpty() ? node.getName() : currentPath + "." + node.getName();
            if (node.getKind() == ElementKind.SUITE) {
                if (!node.getChildren().isEmpty()) {
                    for (final ExecutionTreeNode n : node.getChildren()) {
                        collectNonExecutedTestsOrTasksPaths(n, nonExecutedTestsOrTasks, newPath, project, linkedResources,
                                alreadyUsedPaths);
                    }
                } else {
                    final RobotModel model = RedPlugin.getModelManager().getModel();
                    final IWorkspaceRoot root = project.getWorkspace().getRoot();
                    final RedWorkspace workspace = new RedWorkspace(root);
                    IResource resource = workspace.forUri(node.getPath());
                    if (resource == null) {
                        for (final IResource linkedResource : linkedResources.keySet()) {
                            final String name = linkedResource.getName();
                            final String robotName = RobotPathsNaming
                                    .toRobotFrameworkName(name.contains(".") ? name.substring(0, name.lastIndexOf('.')) : name);
                            if (robotName.equals(node.getName())) {
                                resource = linkedResource;
                            }
                        }
                    }
                    collectTestCasesOrTasks(model, project, resource, nonExecutedTestsOrTasks, newPath);
                }
            } else {
                nonExecutedTestsOrTasks.add(newPath);
            }
        }
    }

    private void collectTestCasesOrTasks(final RobotModel model, final IResource project, final IResource resource,
            final List<String> failedTestsOrTasks, final String newPath) {
        if (resource instanceof IFile) {
            final RobotSuiteFile suiteFile = model.createSuiteFile((IFile) resource);
            final List<RobotSuiteFileSection> sections = suiteFile.getSections();
            for (final RobotSuiteFileSection section : sections) {
                if (section instanceof RobotCasesSection) {
                    final TestCaseTable table = (TestCaseTable) section.getLinkedElement();
                    final List<TestCase> cases = table.getTestCases();
                    for (final TestCase testCase : cases) {
                        failedTestsOrTasks.add(newPath + "." + testCase.getName().getText());
                    }
                } else if (section instanceof RobotTasksSection) {
                    final TaskTable table = (TaskTable) section.getLinkedElement();
                    final List<Task> tasks = table.getTasks();
                    for (final Task task : tasks) {
                        failedTestsOrTasks.add(newPath + "." + task.getName().getText());
                    }
                }

            }
        } else {
            final File location = new File(resource.getLocation().toString());
            for (final File file : location.listFiles()) {
                final String path = file.getName().contains(".")
                        ? newPath + "." + file.getName().substring(0, file.getName().indexOf("."))
                        : newPath + "." + file.getName();
                final IWorkspaceRoot root = project.getWorkspace().getRoot();
                final RedWorkspace workspace = new RedWorkspace(root);
                final URI pathURI = URI.create("file:///" + file.getPath().replace("\\", "/"));
                final IResource nestedResource = workspace.forUri(pathURI);
                collectTestCasesOrTasks(model, project, nestedResource, failedTestsOrTasks, path);
            }
        }
    }
}
