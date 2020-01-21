/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent.ExecutionMode;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;
import org.robotframework.red.junit.jupiter.StatefulProject;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class })
public class ExecutionStatusStoreTest {

    @Project(cleanUpAfterEach = true)
    static StatefulProject project;

    @TempDir
    public File tempFolder;

    @Test
    public void newlyCreatedStoreHaveZeroedCountersAndNoTree() {
        final ExecutionStatusStore store = new ExecutionStatusStore();

        assertThat(store.getExecutionTree()).isNull();
        assertThat(store.getCurrent()).isNull();
        assertThat(store.getCurrentTest()).isEqualTo(0);
        assertThat(store.getTotalTests()).isEqualTo(0);
        assertThat(store.getPassedTests()).isEqualTo(0);
        assertThat(store.getFailedTests()).isEqualTo(0);
        assertThat(store.getNonExecutedTests()).isEqualTo(0);
        assertThat(store.getOutputFilePath()).isNull();
    }

    @Test
    public void whenSuiteStartsAndStoreIsFreshlyCreated_rootIsEstablishedWithChildrenAndTotalTestsCounter()
            throws Exception {
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();

        store.suiteStarted("suite", new URI("file:///suite"), ExecutionMode.TESTS, 42, new ArrayList<>(),
                newArrayList("s1", "s2"), newArrayList(Optional.empty(), Optional.empty()));

        assertThat(store.getTotalTests()).isEqualTo(42);
        assertThat(store.getNonExecutedTests()).isEqualTo(42);

        final ExecutionTreeNode root = store.getExecutionTree();
        assertThat(root.getStatus()).isEqualTo(Optional.of(Status.RUNNING));
        assertThat(root.getPath()).isEqualTo(new URI("file:///suite"));
        assertThat(root.getChildren().stream().map(ExecutionTreeNode::getName).collect(toList())).containsExactly("s1",
                "s2");
        assertThat(root.getChildren().stream().map(ExecutionTreeNode::getKind).collect(toList()))
                .containsExactly(ElementKind.SUITE, ElementKind.SUITE);

        final ExecutionTreeNode current = store.getCurrent();
        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.RUNNING));
        assertThat(current.getName()).isEqualTo("suite");
    }

    @Test
    public void whenSuiteStartsAndStoreIsFreshlyCreated_theStoreGetsDirty() throws Exception {
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();

        store.suiteStarted("suite", new URI("file:///suite"), ExecutionMode.TESTS, 42, newArrayList("s1", "s2"),
                new ArrayList<>(), new ArrayList<>());
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenSuiteStartsAndStoreHasRootEstablished_childrenAreCreatedInCurrentNode() throws Exception {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(root, "inner", null);
        root.addChildren(current);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setExecutionTree(root);
        store.setCurrent(current);

        store.suiteStarted("another", new URI("file:///another"), ExecutionMode.TESTS, 30, newArrayList("t1", "t2"),
                new ArrayList<>(), new ArrayList<>());

        assertThat(store.getTotalTests()).isEqualTo(30);
        assertThat(store.getNonExecutedTests()).isEqualTo(30);
        assertThat(store.getExecutionTree()).isSameAs(root);

        final ExecutionTreeNode newCurrent = store.getCurrent();
        assertThat(newCurrent.getStatus()).isEqualTo(Optional.of(Status.RUNNING));
        assertThat(newCurrent.getName()).isEqualTo("another");
        assertThat(newCurrent.getPath()).isEqualTo(new URI("file:///another"));
        assertThat(newCurrent.getChildren()).extracting(ExecutionTreeNode::getName).containsExactly("t1", "t2");
        assertThat(newCurrent.getChildren()).extracting(ExecutionTreeNode::getKind)
                .containsExactly(ElementKind.TEST, ElementKind.TEST);
    }

    @Test
    public void whenSuiteStartsAndStoreHasRootEstablished_childrenPathsAreSetInCurrentNode() throws Exception {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(root, "inner", null);
        root.addChildren(current);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setExecutionTree(root);
        store.setCurrent(current);

        store.suiteStarted("another", new URI("file:///another"), ExecutionMode.TESTS, 30, new ArrayList<>(),
                newArrayList("s1", "s2"),
                newArrayList(Optional.of(new URI("path/to/s1")), Optional.of(new URI("path/to/s2"))));
        assertThat(store.getCurrent().getChildren().get(0).getPath()).isEqualTo(new URI("path/to/s1"));
        assertThat(store.getCurrent().getChildren().get(1).getPath()).isEqualTo(new URI("path/to/s2"));
    }

    @Test
    public void whenSuiteStartsAndStoreHasRootEstablished_theStoreGetsDirty() throws Exception {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(root, "inner", null);
        root.addChildren(current);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setExecutionTree(root);
        store.setCurrent(current);

        store.suiteStarted("another", new URI("file:///another"), ExecutionMode.TESTS, 30, new ArrayList<>(),
                newArrayList("t1", "t2"), new ArrayList<>());
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenSuiteEnds_currentNodeChangesStatus_currentIsNullifiedIfThereIsNoParent() {
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(null, "suite", null);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.suiteEnded(100, Status.PASS, "");

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.PASS));
        assertThat(current.getMessage()).isEmpty();
        assertThat(current.getElapsedTime()).isEqualTo(100);

        assertThat(store.getCurrent()).isNull();
    }

    @Test
    public void whenSuiteEnds_theStoreGetsDirty_1() {
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(null, "suite", null);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.suiteEnded(100, Status.PASS, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenSuiteEnds_currentNodeChangesStatus_currentIsMovedToParentIfThereIsNoSibling() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode previous = ExecutionTreeNode.newSuiteNode(parent, "inner1", null);
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(parent, "inner2", null);
        parent.addChildren(previous, current);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.suiteEnded(100, Status.PASS, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.PASS));
        assertThat(current.getMessage()).isEmpty();
        assertThat(current.getElapsedTime()).isEqualTo(100);

        assertThat(store.getCurrent()).isSameAs(parent);
    }

    @Test
    public void whenSuiteEnds_theStoreGetsDirty_2() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode previous = ExecutionTreeNode.newSuiteNode(parent, "inner1", null);
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(parent, "inner2", null);
        parent.addChildren(previous, current);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.suiteEnded(100, Status.PASS, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenSuiteEnds_currentNodeChangesStatus_currentIsMovedToParent() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(parent, "inner1", null);
        final ExecutionTreeNode next = ExecutionTreeNode.newSuiteNode(parent, "inner2", null);
        parent.addChildren(current, next);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.suiteEnded(100, Status.FAIL, "error");

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.FAIL));
        assertThat(current.getMessage()).isEqualTo("error");
        assertThat(current.getElapsedTime()).isEqualTo(100);

        assertThat(store.getCurrent()).isSameAs(parent);
    }

    @Test
    public void whenSuiteEnds_theStoreGetsDirty_3() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode current = ExecutionTreeNode.newSuiteNode(parent, "inner1", null);
        final ExecutionTreeNode next = ExecutionTreeNode.newSuiteNode(parent, "inner2", null);
        parent.addChildren(current, next);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.suiteEnded(100, Status.PASS, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenTestStarts_currentNodeChangesStatusAndTestCounterIsIncremented() {
        final ExecutionTreeNode currentSuite = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(currentSuite, "test", null);
        currentSuite.addChildren(test);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(currentSuite);

        store.testStarted("test", "resolvedTest");

        assertThat(test.getStatus()).isEqualTo(Optional.of(Status.RUNNING));
        assertThat(store.getCurrentTest()).isEqualTo(1);
    }

    @Test
    public void whenTestStarts_theStoreGetsDirty() {
        final ExecutionTreeNode currentSuite = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        ExecutionTreeNode.newTestNode(currentSuite, "test", null);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(currentSuite);

        store.testStarted("test", "resolvedTest");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenTestEnds_currentNodeChangesStatusCountersAreChanged_currentIsMovedToParentIfThereIsNoSibling() {
        final ExecutionTreeNode currentSuite = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode previous = ExecutionTreeNode.newTestNode(currentSuite, "test1", null);
        final ExecutionTreeNode currentTest = ExecutionTreeNode.newTestNode(currentSuite, "test2", null);
        currentSuite.addChildren(previous, currentTest);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(currentTest);

        store.testEnded(42, Status.PASS, "");

        assertThat(currentTest.getStatus()).isEqualTo(Optional.of(Status.PASS));
        assertThat(currentTest.getElapsedTime()).isEqualTo(42);
        assertThat(currentTest.getMessage()).isEmpty();
        assertThat(store.getPassedTests()).isEqualTo(1);
        assertThat(store.getFailedTests()).isEqualTo(0);
        assertThat(store.getCurrent()).isSameAs(currentSuite);
    }

    @Test
    public void whenTestEnds_currentNodeChangesStatusCountersAreChanged_currentIsMovedToParent() {
        final ExecutionTreeNode currentSuite = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode currentTest = ExecutionTreeNode.newTestNode(currentSuite, "test1", null);
        final ExecutionTreeNode next = ExecutionTreeNode.newTestNode(currentSuite, "test2", null);
        currentSuite.addChildren(currentTest, next);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(currentTest);

        store.testEnded(42, Status.FAIL, "");

        assertThat(currentTest.getStatus()).isEqualTo(Optional.of(Status.FAIL));
        assertThat(currentTest.getElapsedTime()).isEqualTo(42);
        assertThat(currentTest.getMessage()).isEmpty();
        assertThat(store.getPassedTests()).isEqualTo(0);
        assertThat(store.getFailedTests()).isEqualTo(1);
        assertThat(store.getCurrent()).isSameAs(currentSuite);
    }

    @Test
    public void whenTestEnds_theStoreGetsDirty() {
        final ExecutionTreeNode currentSuite = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode currentTest = ExecutionTreeNode.newTestNode(currentSuite, "test1", null);
        final ExecutionTreeNode next = ExecutionTreeNode.newTestNode(currentSuite, "test2", null);
        currentSuite.addChildren(currentTest, next);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(currentTest);

        store.testEnded(42, Status.FAIL, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenOutputFileIsGenerated_itIsStored() throws Exception {
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setOutputFilePath(new URI("file:///output.xml"));

        assertThat(store.getOutputFilePath()).isEqualTo(new URI("file:///output.xml"));
    }

    @Test
    public void whenStoreIsDisposed_treeIsRemoved() {
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(ExecutionTreeNode.newSuiteNode(null, "r", null));
        store.setCurrent(ExecutionTreeNode.newSuiteNode(null, "s", null));

        assertThat(store.isDisposed()).isFalse();

        store.dispose();

        assertThat(store.getExecutionTree()).isNull();
        assertThat(store.getCurrent()).isNull();
        assertThat(store.isDisposed()).isTrue();
    }

    @Test
    public void whenManySuitesContainFailedTests_failedSuitePathsMapIsReturnedWithManySuites()
            throws IOException, CoreException {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                URI.create("file://" + project.getLocation().toPortableString()));
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file://" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file://" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(root, "suite3", URI
                .create("file://" + project.getLocation().toPortableString() + "/suite3.robot"));
        final ExecutionTreeNode suite4 = ExecutionTreeNode.newSuiteNode(root, "suite4", URI
                .create("file://" + project.getLocation().toPortableString() + "/suite4.robot"));
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite1, "test1", suite1.getPath());
        final ExecutionTreeNode testPassed1 = ExecutionTreeNode.newTestNode(suite1, "test2", suite1.getPath());
        final ExecutionTreeNode testFailed2 = ExecutionTreeNode.newTestNode(suite2, "test1", suite2.getPath());
        final ExecutionTreeNode testPassed2 = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());
        final ExecutionTreeNode testPassed3 = ExecutionTreeNode.newTestNode(suite3, "test1", suite3.getPath());
        final ExecutionTreeNode testPassed4 = ExecutionTreeNode.newTestNode(suite4, "test1", suite4.getPath());
        testFailed1.setStatus(Status.FAIL);
        testFailed2.setStatus(Status.FAIL);
        suite1.addChildren(testFailed1, testPassed1);
        suite2.addChildren(testFailed2, testPassed2);
        suite3.addChildren(testPassed3);
        suite4.addChildren(testPassed4);
        root.addChildren(suite1, suite2, suite3, suite4);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");
        project.createFile("suite3.robot");
        project.createFile("suite4.robot");

        assertThat(store.getFailedSuitePaths(project.getProject()))
                .containsEntry("suite1.robot", newArrayList("test1"))
                .containsEntry("suite2.robot", newArrayList("test1"));
    }

    @Test
    public void whenManySuitesAndLinkedResourceContainFailedTests_failedSuitePathsMapIsReturnedWithManySuites()
            throws IOException, CoreException {
        final File linkedNonWorkspaceFile1 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_1");
        project.createFileLink("linked_suite", linkedNonWorkspaceFile1.toURI());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode projectNode = ExecutionTreeNode.newSuiteNode(root, "Project",
                URI.create("file://" + project.getLocation().toPortableString()));
        final ExecutionTreeNode suite1Catalog = ExecutionTreeNode.newSuiteNode(projectNode, "Suite1 Catalog",
                URI.create("file://" + project.getLocation().toPortableString() + "/suite1_catalog"));
        final ExecutionTreeNode suite2Catalog = ExecutionTreeNode.newSuiteNode(projectNode, "Suite2 Catalog",
                URI.create("file://" + project.getLocation().toPortableString() + "/suite2_catalog"));
        final ExecutionTreeNode suiteLinked3 = ExecutionTreeNode.newSuiteNode(root, "LinkedSuite 1",
                linkedNonWorkspaceFile1.toURI());

        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(suite1Catalog, "Suite1",
                URI.create("file://" + project.getLocation().toPortableString() + "/suite1_catalog/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(suite2Catalog, "Suite2",
                URI.create("file://" + project.getLocation().toPortableString() + "/suite2_catalog/suite2.robot"));

        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite1, "test1", suite1.getPath());
        final ExecutionTreeNode testPassed1 = ExecutionTreeNode.newTestNode(suite1, "test2", suite1.getPath());
        final ExecutionTreeNode testFailed2 = ExecutionTreeNode.newTestNode(suite2, "test1", suite2.getPath());
        final ExecutionTreeNode testPassed2 = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());
        final ExecutionTreeNode testFailedSec2 = ExecutionTreeNode.newTestNode(suite2, "test3", suite2.getPath());
        final ExecutionTreeNode testFailed3 = ExecutionTreeNode.newTestNode(suiteLinked3, "test1",
                suiteLinked3.getPath());

        suite1Catalog.setStatus(Status.FAIL);
        suite2Catalog.setStatus(Status.FAIL);
        suiteLinked3.setStatus(Status.FAIL);
        suite1.setStatus(Status.FAIL);
        suite2.setStatus(Status.FAIL);
        testFailed1.setStatus(Status.FAIL);
        testFailed2.setStatus(Status.FAIL);
        testFailedSec2.setStatus(Status.FAIL);
        testFailed3.setStatus(Status.FAIL);
        suite1.addChildren(testFailed1, testPassed1);
        suite2.addChildren(testFailed2, testPassed2, testFailedSec2);
        suite1Catalog.addChildren(suite1);
        suite2Catalog.addChildren(suite2);
        suiteLinked3.addChildren(testFailed3);
        projectNode.addChildren(suite1Catalog, suite2Catalog);
        root.addChildren(projectNode, suiteLinked3);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createDir("suite1_catalog");
        project.createDir("suite2_catalog");
        project.createFile("suite1_catalog/suite1.robot");
        project.createFile("suite2_catalog/suite2.robot");
        project.createFile("suiteLinked3.robot");

        assertThat(store.getFailedSuitePaths(project.getProject()))
                .containsEntry("suite1_catalog", newArrayList("Suite1.test1"))
                .containsEntry("suite2_catalog", newArrayList("Suite2.test1", "Suite2.test3"))
                .containsEntry("linked_suite", newArrayList("test1"));
    }

    @Test
    public void whenSingleSuiteContainsFailedTests_failedSuitePathsMapIsReturnedWithSingleSuite()
            throws IOException, CoreException {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                project.getLocationURI());
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "suite",
                URI.create("file://" + project.getLocation().toPortableString() + "/suite.robot"));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);
        root.addChildren(suite);

        test.setStatus(Status.FAIL);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("suite.robot");

        assertThat(store.getFailedSuitePaths(project.getProject())).containsEntry("suite.robot",
                newArrayList("test"));
    }

    @Test
    public void whenSingleSuiteContainsFailedTestsAndSingleSuitePreferenceIsSet_failedSuitePathsMapIsReturnedWithSingleSuite()
            throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(null, "suite",
                URI.create("file://" + project.getLocation().toPortableString() + "/suite.robot"));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        test.setStatus(Status.FAIL);
        suite.addChildren(test);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(suite);

        project.createFile("suite.robot");

        assertThat(store.getFailedSuitePaths(project.getProject())).containsEntry("suite.robot",
                newArrayList("test"));
    }

    @Test
    public void whenFailedTestsDoNotExist_emptyFailedSuitePathsMapIsReturned() {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "suite", null);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getFailedSuitePaths(project.getProject())).isEmpty();
    }

    @Test
    public void whenManySuitesContainNonExecutedTests_nonExecutedSuitePathsMapIsReturnedWithNonExecutedSuitesWithoutTests()
            throws IOException, CoreException {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", project.getLocationURI());
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(root, "suite3", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite3.robot"));
        final ExecutionTreeNode suite4 = ExecutionTreeNode.newSuiteNode(root, "suite4", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite4.robot"));
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite1, "test1", suite1.getPath());
        final ExecutionTreeNode testPassed1 = ExecutionTreeNode.newTestNode(suite1, "test2", suite1.getPath());
        final ExecutionTreeNode testFailed2 = ExecutionTreeNode.newTestNode(suite2, "test1", suite2.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());
        suite1.setStatus(Status.FAIL);
        testFailed1.setStatus(Status.FAIL);
        testPassed1.setStatus(Status.PASS);
        testFailed2.setStatus(Status.FAIL);

        suite1.addChildren(testFailed1, testPassed1);
        suite2.addChildren(testFailed2, testNonExecuted2);
        root.addChildren(suite1, suite2, suite3, suite4);

        final List<String> linkedResources = new ArrayList<>();
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");
        project.createFile("suite3.robot");
        project.createFile("suite4.robot");

        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources))
                .containsEntry("suite2.robot", new ArrayList<>()).containsEntry("suite3.robot", new ArrayList<>())
                .containsEntry("suite4.robot", new ArrayList<>());
    }

    @Test
    public void whenManySuitesAndLinkedResourceContainNonExecutedTests_nonExecutedSuitePathsMapIsReturnedWithNonExecutedSuitesWithoutTests()
            throws IOException, CoreException {
        final File linkedNonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite.robot");
        final IFile linkedSuite = project.getFile("linkedSuite.robot");
        project.createFileLink("linkedSuite.robot", linkedNonWorkspaceFile.toURI());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(root, "suite3", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite3.robot"));
        final ExecutionTreeNode suiteLinked4 = ExecutionTreeNode.newSuiteNode(root, "LinkedSuite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite1, "test1", suite1.getPath());
        final ExecutionTreeNode testPassed1 = ExecutionTreeNode.newTestNode(suite1, "test2", suite1.getPath());
        final ExecutionTreeNode testFailed2 = ExecutionTreeNode.newTestNode(suite2, "test1", suite2.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());
        suite1.setStatus(Status.FAIL);
        testFailed1.setStatus(Status.FAIL);
        testPassed1.setStatus(Status.PASS);
        testFailed2.setStatus(Status.FAIL);
        suite1.addChildren(testFailed1, testPassed1);
        suite2.addChildren(testFailed2, testNonExecuted2);
        root.addChildren(suite1, suite2, suite3, suiteLinked4);

        final List<String> linkedResources = new ArrayList<>();
        linkedResources.add(linkedSuite.getFullPath().toPortableString());
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");
        project.createFile("suite3.robot");

        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources))
                .containsEntry("suite2.robot", new ArrayList<>()).containsEntry("suite3.robot", new ArrayList<>())
                .containsEntry("linkedSuite.robot", new ArrayList<>());
    }

    @Test
    public void whenLinkedResourcesContainNonExecutedTestsAndItsRobotNamesAreTheSame_nonExecutedSuitePathsMapIsReturnedWithAllNonExecutedSuitesWithoutTests()
            throws IOException, CoreException {
        final File linkedNonWorkspaceFile1 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_1");
        final File linkedNonWorkspaceFile2 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_2");
        final IFile linkedSuite1 = project.getFile("linked_suite.robot");
        final IFile linkedSuite2 = project.getFile("linked suite.robot");
        project.createFileLink("linked_suite.robot", linkedNonWorkspaceFile1.toURI());
        project.createFileLink("linked suite.robot", linkedNonWorkspaceFile2.toURI());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suiteLinked3 = ExecutionTreeNode.newSuiteNode(root, "Linked Suite", null);
        final ExecutionTreeNode suiteLinked4 = ExecutionTreeNode.newSuiteNode(root, "Linked Suite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite1, "test1", suite1.getPath());
        final ExecutionTreeNode testPassed1 = ExecutionTreeNode.newTestNode(suite1, "test2", suite1.getPath());
        final ExecutionTreeNode testFailed2 = ExecutionTreeNode.newTestNode(suite2, "test1", suite2.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());
        suite1.setStatus(Status.FAIL);
        testFailed1.setStatus(Status.FAIL);
        testPassed1.setStatus(Status.PASS);
        testFailed2.setStatus(Status.FAIL);
        suite1.addChildren(testFailed1, testPassed1);
        suite2.addChildren(testFailed2, testNonExecuted2);
        root.addChildren(suite1, suite2, suiteLinked3, suiteLinked4);

        final List<String> linkedResources = new ArrayList<>();
        linkedResources.add(linkedSuite1.getFullPath().toPortableString());
        linkedResources.add(linkedSuite2.getFullPath().toPortableString());
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");

        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources))
                .containsEntry("suite2.robot", new ArrayList<>()).containsEntry("linked_suite.robot", new ArrayList<>())
                .containsEntry("linked suite.robot", new ArrayList<>());
    }

    @Test
    public void whenLinkedResourcesContainNonExecutedTestsAndOneOfThemHasPath_nonExecutedSuitePathsMapIsReturnedWithAllNonExecutedSuitesWithoutTests()
            throws IOException, CoreException {
        final File linkedNonWorkspaceFile1 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_1");
        final File linkedNonWorkspaceFile2 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_2");
        project.createFileLink("linked_suite_1.robot", linkedNonWorkspaceFile1.toURI());
        project.createFileLink("linked_suite_2.robot", linkedNonWorkspaceFile2.toURI());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                project.getLocationURI());
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(root, "parent",
                URI.create("file://" + project.getLocation().toPortableString() + "/parent"));
        final ExecutionTreeNode suiteLinked1 = ExecutionTreeNode.newSuiteNode(parent, "Linked Suite 1",
                URI.create(linkedNonWorkspaceFile1.toURI().toString() + "/linked_suite_1.robot"));
        final ExecutionTreeNode suiteLinked2 = ExecutionTreeNode.newSuiteNode(parent, "Linked Suite 2", null);
        final ExecutionTreeNode testFailed = ExecutionTreeNode.newTestNode(suiteLinked1, "test1",
                suiteLinked1.getPath());
        final ExecutionTreeNode testNonExecuted = ExecutionTreeNode.newTestNode(suiteLinked1, "test2",
                suiteLinked1.getPath());
        testFailed.setStatus(Status.FAIL);
        testNonExecuted.setStatus(Status.RUNNING);
        suiteLinked1.addChildren(testFailed, testNonExecuted);
        parent.addChildren(suiteLinked1, suiteLinked2);
        root.addChildren(parent);

        final List<String> linkedResources = new ArrayList<>();
        linkedResources.add(parent.getPath().toString());
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("parent");

        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources))
                .containsEntry("parent", new ArrayList<>());
    }

    @Test
    public void whenManySuitesAndSingleLinkedResourceContainNonExecutedTests_nonExecutedSuitePathsMapIsReturnedWithNonExecutedSuitesWithoutTests()
            throws IOException, CoreException {
        final File linkedNonWorkspaceFile1 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_1.robot");
        final File linkedNonWorkspaceFile2 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_2.robot");
        final IFile linkedSuite1 = project.getFile("linkedSuite_1.robot");
        final IFile linkedSuite2 = project.getFile("linkedSuite_2.robot");
        project.createFileLink("linkedSuite_1.robot", linkedNonWorkspaceFile1.toURI());
        project.createFileLink("linkedSuite_2.robot", linkedNonWorkspaceFile2.toURI());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suiteLinked3 = ExecutionTreeNode.newSuiteNode(root, "LinkedSuite 1", null);
        final ExecutionTreeNode suiteLinked4 = ExecutionTreeNode.newSuiteNode(root, "LinkedSuite 2", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite1, "test1", suite1.getPath());
        final ExecutionTreeNode testPassed1 = ExecutionTreeNode.newTestNode(suite1, "test2", suite1.getPath());
        final ExecutionTreeNode testFailed2 = ExecutionTreeNode.newTestNode(suite2, "test1", suite2.getPath());
        final ExecutionTreeNode testNonExecuted = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());
        suite1.setStatus(Status.FAIL);
        testFailed1.setStatus(Status.FAIL);
        testPassed1.setStatus(Status.PASS);
        testFailed2.setStatus(Status.FAIL);

        suite1.addChildren(testFailed1, testPassed1);
        suite2.addChildren(testFailed2, testNonExecuted);
        root.addChildren(suite1, suite2, suiteLinked3, suiteLinked4);

        final List<String> linkedResources = newArrayList(linkedSuite1.getFullPath().toPortableString(),
                linkedSuite2.getFullPath().toPortableString());
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");

        final Map<String, List<String>> nonExecutedSuitePaths = store.getNonExecutedSuitePaths(project.getProject(),
                linkedResources);
        assertThat(nonExecutedSuitePaths)
                .containsEntry("suite2.robot", new ArrayList<>())
                .containsEntry("linkedSuite_1.robot", new ArrayList<>())
                .containsEntry("linkedSuite_2.robot", new ArrayList<>());
    }

    @Test
    public void whenManySuitesAndLinkedResourcesContainNonExecutedTests_nonExecutedSuitePathsMapIsReturnedWithNonExecutedSuitesWithoutTests()
            throws IOException, CoreException {
        final File linkedNonWorkspaceFile1 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_1.robot");
        final File linkedNonWorkspaceFile2 = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite_2.robot");
        final IFile linkedSuite1 = project.getFile("linkedSuite_1.robot");
        final IFile linkedSuite2 = project.getFile("linkedSuite_2.robot");
        project.createFileLink("linkedSuite_1.robot", linkedNonWorkspaceFile1.toURI());
        project.createFileLink("linkedSuite_2.robot", linkedNonWorkspaceFile2.toURI());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suiteLinked3 = ExecutionTreeNode.newSuiteNode(root, "LinkedSuite 1", null);
        final ExecutionTreeNode suiteLinked4 = ExecutionTreeNode.newSuiteNode(root, "LinkedSuite 2", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite1, "test1", suite1.getPath());
        final ExecutionTreeNode testPassed1 = ExecutionTreeNode.newTestNode(suite1, "test2", suite1.getPath());
        final ExecutionTreeNode testFailed2 = ExecutionTreeNode.newTestNode(suite2, "test1", suite2.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());
        suite1.setStatus(Status.FAIL);
        testFailed1.setStatus(Status.FAIL);
        testPassed1.setStatus(Status.PASS);
        testFailed2.setStatus(Status.FAIL);
        suite1.addChildren(testFailed1, testPassed1);
        suite2.addChildren(testFailed2, testNonExecuted2);
        root.addChildren(suite1, suite2, suiteLinked3, suiteLinked4);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");

        final List<String> linkedResources = newArrayList(linkedSuite1.getFullPath().toPortableString(),
                linkedSuite2.getFullPath().toPortableString());
        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources))
                .containsEntry("suite2.robot", new ArrayList<>())
                .containsEntry("linkedSuite_1.robot", new ArrayList<>())
                .containsEntry("linkedSuite_2.robot", new ArrayList<>());
    }

    @Test
    public void whenSingleSuiteContainsNonExcutedTests_nonExcutedSuitePathsMapIsReturnedWithOnlyNonExecutedTests()
            throws IOException, CoreException {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                project.getLocationURI());
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "suite", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite.robot"));
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite, "test1", suite.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite, "test2", suite.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite, "test3", suite.getPath());
        testFailed1.setStatus(Status.FAIL);
        suite.addChildren(testFailed1, testNonExecuted2, testNonExecuted3);
        root.addChildren(suite);

        final List<String> linkedResources = new ArrayList<>();
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        project.createFile("suite.robot");

        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources))
                .containsEntry("suite.robot", newArrayList("test2", "test3"));
    }

    @Test
    public void whenSingleSuiteContainsNonExcutedTestsAndSingleSuitePreferenceIsSet_nonExcutedSuitePathsMapIsReturnedWithOnlyNonExecutedTests()
            throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(null, "suite", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite.robot"));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);

        final List<String> linkedResources = new ArrayList<>();
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(suite);

        project.createFile("suite.robot");

        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources))
                .containsEntry("suite.robot", newArrayList("test"));
    }

    @Test
    public void whenSingleSuiteFromLinkedResourceContainsNonExcutedTests_nonExcutedSuitePathsMapIsReturnedWithOnlyNonExecutedTests()
            throws IOException, CoreException {
        final File linkedNonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite.robot");
        final IFile linkedSuite = project.getFile("linkedSuite.robot");
        project.createFileLink("linkedSuite.robot", linkedNonWorkspaceFile.toURI());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "LinkedSuite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite, "test1", suite.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite, "test2", suite.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite, "test3", suite.getPath());
        testFailed1.setStatus(Status.FAIL);
        suite.addChildren(testFailed1, testNonExecuted2, testNonExecuted3);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        final List<String> linkedResources = newArrayList(linkedSuite.getFullPath().toPortableString());
        final Map<String, List<String>> nonExecutedSuitePaths = store
                .getNonExecutedSuitePaths(project.getProject(), linkedResources);

        assertThat(nonExecutedSuitePaths).containsEntry("linkedSuite.robot", newArrayList("test2", "test3"));
    }

    @Test
    public void whenSingleSuiteFromLinkedResourceContainsNonExcutedTestsAndSingleSuitePreferenceIsSet_nonExcutedSuitePathsMapIsReturnedWithOnlyNonExecutedTests()
            throws IOException, CoreException {
        final File linkedNonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "non_workspace_suite.robot");
        final IFile linkedSuite = project.getFile("linkedSuite.robot");
        project.createFileLink("linkedSuite.robot", linkedNonWorkspaceFile.toURI());

        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "LinkedSuite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite, "test1", suite.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite, "test2", suite.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite, "test3", suite.getPath());
        testFailed1.setStatus(Status.FAIL);
        suite.addChildren(testFailed1, testNonExecuted2, testNonExecuted3);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        final List<String> linkedResources = newArrayList(linkedSuite.getFullPath().toPortableString());
        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources))
                .containsEntry("linkedSuite.robot", newArrayList("test2", "test3"));
    }

    @Test
    public void whenNonExcutedTestsDoNotExist_emptyNonExcutedSuitePathsMapIsReturned() {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "suite", null);
        suite.setStatus(Status.FAIL);
        root.addChildren(suite);

        final List<String> linkedResources = new ArrayList<>();
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedSuitePaths(project.getProject(), linkedResources)).isEmpty();
    }
}
