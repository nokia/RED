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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent.ExecutionMode;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;
import org.robotframework.red.junit.ProjectProvider;

public class ExecutionStatusStoreTest {

    private static final String PROJECT_NAME = ExecutionStatusStoreTest.class.getSimpleName();

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(PROJECT_NAME);

    @Test
    public void newlyCreatedStoreHaveZeroedCountersAndNoTree() {
        final ExecutionStatusStore store = new ExecutionStatusStore();

        assertThat(store.getExecutionTree()).isNull();
        assertThat(store.getCurrent()).isNull();
        assertThat(store.getCurrentTest()).isEqualTo(0);
        assertThat(store.getTotalTests()).isEqualTo(0);
        assertThat(store.getPassedTests()).isEqualTo(0);
        assertThat(store.getFailedTests()).isEqualTo(0);
        assertThat(store.getOutputFilePath()).isNull();
    }

    @Test
    public void whenSuiteStartsAndStoreIsFreshlyCreated_rootIsEstablishedWithChildrenAndTotalTestsCounter()
            throws Exception {
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();

        store.suiteStarted("suite", new URI("file:///suite"), ExecutionMode.TESTS, 42, newArrayList("s1", "s2"),
                new ArrayList<>());

        assertThat(store.getTotalTests()).isEqualTo(42);

        final ExecutionTreeNode root = store.getExecutionTree();
        assertThat(root.getStatus()).isEqualTo(Optional.of(Status.RUNNING));
        assertThat(root.getPath()).isEqualTo(new URI("file:///suite"));
        assertThat(root.getChildren().stream().map(ExecutionTreeNode::getName).collect(toList())).containsExactly("s1",
                "s2");
        assertThat(root.getChildren().stream().map(ExecutionTreeNode::getKind).collect(toList()))
                .containsExactly(ElementKind.SUITE, ElementKind.SUITE);

        final ExecutionTreeNode current = store.getCurrent();
        assertThat(current.getStatus()).isEqualTo(Optional.empty());
        assertThat(current.getName()).isEqualTo("s1");
    }

    @Test
    public void whenSuiteStartsAndStoreIsFreshlyCreated_theStoreGetsDirty() throws Exception {
        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();

        store.suiteStarted("suite", new URI("file:///suite"), ExecutionMode.TESTS, 42, newArrayList("s1", "s2"),
                new ArrayList<>());
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenSuiteStartsAndStoreHasRootEstablished_childrenAreCreatedInCurrentNode() throws Exception {
        final ExecutionTreeNode root = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode current = new ExecutionTreeNode(root, ElementKind.SUITE, "inner");
        root.addChildren(newArrayList(current));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setExecutionTree(root);
        store.setCurrent(current);

        store.suiteStarted("another", new URI("file:///another"), ExecutionMode.TESTS, 30, new ArrayList<>(),
                newArrayList("t1", "t2"));

        assertThat(store.getTotalTests()).isEqualTo(0);
        assertThat(store.getExecutionTree()).isSameAs(root);

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.RUNNING));
        assertThat(current.getPath()).isEqualTo(new URI("file:///another"));
        assertThat(current.getChildren().stream().map(ExecutionTreeNode::getName).collect(toList()))
                .containsExactly("t1", "t2");
        assertThat(current.getChildren().stream().map(ExecutionTreeNode::getKind).collect(toList()))
                .containsExactly(ElementKind.TEST, ElementKind.TEST);

        final ExecutionTreeNode newCurrent = store.getCurrent();
        assertThat(newCurrent.getStatus()).isEqualTo(Optional.empty());
        assertThat(newCurrent.getName()).isEqualTo("t1");
    }

    @Test
    public void whenSuiteStartsAndStoreHasRootEstablished_theStoreGetsDirty() throws Exception {
        final ExecutionTreeNode root = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode current = new ExecutionTreeNode(root, ElementKind.SUITE, "inner");
        root.addChildren(newArrayList(current));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setExecutionTree(root);
        store.setCurrent(current);

        store.suiteStarted("another", new URI("file:///another"), ExecutionMode.TESTS, 30, new ArrayList<>(),
                newArrayList("t1", "t2"));
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenSuiteEnds_currentNodeChangesStatus_currentIsNullifiedIfThereIsNoParent() {
        final ExecutionTreeNode current = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(100, Status.PASS, "");

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.PASS));
        assertThat(current.getMessage()).isEmpty();
        assertThat(current.getElapsedTime()).isEqualTo(100);

        assertThat(store.getCurrent()).isNull();
    }

    @Test
    public void whenSuiteEnds_theStoreGetsDirty_1() {
        final ExecutionTreeNode current = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(100, Status.PASS, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenSuiteEnds_currentNodeChangesStatus_currentIsMovedToParentIfThereIsNoSibling() {
        final ExecutionTreeNode parent = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode previous = new ExecutionTreeNode(parent, ElementKind.SUITE, "inner1");
        final ExecutionTreeNode current = new ExecutionTreeNode(parent, ElementKind.SUITE, "inner2");
        parent.addChildren(newArrayList(previous, current));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(100, Status.PASS, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.PASS));
        assertThat(current.getMessage()).isEmpty();
        assertThat(current.getElapsedTime()).isEqualTo(100);

        assertThat(store.getCurrent()).isSameAs(parent);
    }

    @Test
    public void whenSuiteEnds_theStoreGetsDirty_2() {
        final ExecutionTreeNode parent = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode previous = new ExecutionTreeNode(parent, ElementKind.SUITE, "inner1");
        final ExecutionTreeNode current = new ExecutionTreeNode(parent, ElementKind.SUITE, "inner2");
        parent.addChildren(newArrayList(previous, current));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(100, Status.PASS, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenSuiteEnds_currentNodeChangesStatus_currentIsMovedToSibling() {
        final ExecutionTreeNode parent = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode current = new ExecutionTreeNode(parent, ElementKind.SUITE, "inner1");
        final ExecutionTreeNode next = new ExecutionTreeNode(parent, ElementKind.SUITE, "inner2");
        parent.addChildren(newArrayList(current, next));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(100, Status.FAIL, "error");

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.FAIL));
        assertThat(current.getMessage()).isEqualTo("error");
        assertThat(current.getElapsedTime()).isEqualTo(100);

        assertThat(store.getCurrent()).isSameAs(next);
    }

    @Test
    public void whenSuiteEnds_theStoreGetsDirty_3() {
        final ExecutionTreeNode parent = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode current = new ExecutionTreeNode(parent, ElementKind.SUITE, "inner1");
        final ExecutionTreeNode next = new ExecutionTreeNode(parent, ElementKind.SUITE, "inner2");
        parent.addChildren(newArrayList(current, next));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(100, Status.PASS, "");
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenTestStarts_currentNodeChangesStatusAndTestCounterIsIncremented() {
        final ExecutionTreeNode current = new ExecutionTreeNode(null, ElementKind.TEST, "test");

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.testStarted();

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.RUNNING));
        assertThat(store.getCurrentTest()).isEqualTo(1);
    }

    @Test
    public void whenTestStarts_theStoreGetsDirty() {
        final ExecutionTreeNode current = new ExecutionTreeNode(null, ElementKind.TEST, "test");

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.testStarted();
        assertThat(store.checkDirtyAndReset()).isTrue();
        assertThat(store.checkDirtyAndReset()).isFalse();
    }

    @Test
    public void whenTestEnds_currentNodeChangesStatusCountersAreChanged_currentIsMovedToParentIfThereIsNoSibling() {
        final ExecutionTreeNode parent = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode previous = new ExecutionTreeNode(parent, ElementKind.TEST, "test1");
        final ExecutionTreeNode current = new ExecutionTreeNode(parent, ElementKind.TEST, "test2");
        parent.addChildren(newArrayList(previous, current));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(42, Status.PASS, "");

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.PASS));
        assertThat(current.getElapsedTime()).isEqualTo(42);
        assertThat(current.getMessage()).isEmpty();
        assertThat(store.getPassedTests()).isEqualTo(1);
        assertThat(store.getFailedTests()).isEqualTo(0);
        assertThat(store.getCurrent()).isSameAs(parent);
    }

    @Test
    public void whenTestEnds_currentNodeChangesStatusCountersAreChanged_currentIsMovedSibling() {
        final ExecutionTreeNode parent = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode current = new ExecutionTreeNode(parent, ElementKind.TEST, "test1");
        final ExecutionTreeNode next = new ExecutionTreeNode(parent, ElementKind.TEST, "test2");
        parent.addChildren(newArrayList(current, next));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(42, Status.FAIL, "");

        assertThat(current.getStatus()).isEqualTo(Optional.of(Status.FAIL));
        assertThat(current.getElapsedTime()).isEqualTo(42);
        assertThat(current.getMessage()).isEmpty();
        assertThat(store.getPassedTests()).isEqualTo(0);
        assertThat(store.getFailedTests()).isEqualTo(1);
        assertThat(store.getCurrent()).isSameAs(next);
    }

    @Test
    public void whenTestEnds_theStoreGetsDirty() {
        final ExecutionTreeNode parent = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode current = new ExecutionTreeNode(parent, ElementKind.TEST, "test1");
        final ExecutionTreeNode next = new ExecutionTreeNode(parent, ElementKind.TEST, "test2");
        parent.addChildren(newArrayList(current, next));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.open();
        store.setCurrent(current);

        store.elementEnded(42, Status.FAIL, "");
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
        store.setExecutionTree(new ExecutionTreeNode(null, ElementKind.SUITE, "r"));
        store.setCurrent(new ExecutionTreeNode(null, ElementKind.SUITE, "s"));

        assertThat(store.isDisposed()).isFalse();

        store.dispose();

        assertThat(store.getExecutionTree()).isNull();
        assertThat(store.getCurrent()).isNull();
        assertThat(store.isDisposed()).isTrue();
    }

    @Test
    public void whenManySuitesContainFailedTests_failedSuitePathsMapIsReturnedWithManySuites()
            throws IOException, CoreException {
        final ExecutionTreeNode root = new ExecutionTreeNode(null, ElementKind.SUITE, "root");
        final ExecutionTreeNode suite1 = new ExecutionTreeNode(root, ElementKind.SUITE, "suite1");
        final ExecutionTreeNode suite2 = new ExecutionTreeNode(root, ElementKind.SUITE, "suite2");
        final ExecutionTreeNode suite3 = new ExecutionTreeNode(root, ElementKind.SUITE, "suite3");
        final ExecutionTreeNode suite4 = new ExecutionTreeNode(root, ElementKind.SUITE, "suite4");
        final ExecutionTreeNode test1_failed = new ExecutionTreeNode(suite1, ElementKind.TEST, "test1");
        final ExecutionTreeNode test1_passed = new ExecutionTreeNode(suite1, ElementKind.TEST, "test2");
        final ExecutionTreeNode test2_failed = new ExecutionTreeNode(suite2, ElementKind.TEST, "test1");
        final ExecutionTreeNode test2_passed = new ExecutionTreeNode(suite2, ElementKind.TEST, "test2");
        final ExecutionTreeNode test3_passed = new ExecutionTreeNode(suite3, ElementKind.TEST, "test1");
        final ExecutionTreeNode test4_passed = new ExecutionTreeNode(suite4, ElementKind.TEST, "test1");
        test1_failed.setStatus(Status.FAIL);
        test2_failed.setStatus(Status.FAIL);
        suite1.setPath(URI
                .create("file://" + projectProvider.getProject().getLocation().toPortableString() + "/suite1.robot"));
        suite2.setPath(URI
                .create("file://" + projectProvider.getProject().getLocation().toPortableString() + "/suite2.robot"));
        suite3.setPath(URI
                .create("file://" + projectProvider.getProject().getLocation().toPortableString() + "/suite3.robot"));
        suite4.setPath(URI
                .create("file://" + projectProvider.getProject().getLocation().toPortableString() + "/suite4.robot"));
        suite1.addChildren(newArrayList(test1_failed, test1_passed));
        suite2.addChildren(newArrayList(test2_failed, test2_passed));
        suite3.addChildren(newArrayList(test3_passed));
        suite4.addChildren(newArrayList(test4_passed));
        root.addChildren(newArrayList(suite1, suite2, suite3, suite4));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        projectProvider.createFile("suite1.robot");
        projectProvider.createFile("suite2.robot");
        projectProvider.createFile("suite3.robot");
        projectProvider.createFile("suite4.robot");

        assertThat(store.getFailedSuitePaths(projectProvider.getProject()))
                .containsEntry("suite1.robot", newArrayList("test1"))
                .containsEntry("suite2.robot", newArrayList("test1"));
    }

    @Test
    public void whenSingleSuiteContainsFailedTests_failedSuitePathsMapIsReturnedWithSingleSuite()
            throws IOException, CoreException {
        final ExecutionTreeNode root = new ExecutionTreeNode(null, ElementKind.SUITE, "root");
        final ExecutionTreeNode suite = new ExecutionTreeNode(root, ElementKind.SUITE, "suite");
        final ExecutionTreeNode test = new ExecutionTreeNode(suite, ElementKind.TEST, "test");
        test.setStatus(Status.FAIL);
        suite.setPath(
                URI.create("file://" + projectProvider.getProject().getLocation().toPortableString() + "/suite.robot"));
        suite.addChildren(newArrayList(test));
        root.addChildren(newArrayList(suite));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        projectProvider.createFile("suite.robot");

        assertThat(store.getFailedSuitePaths(projectProvider.getProject())).containsEntry("suite.robot",
                newArrayList("test"));
    }

    @Test
    public void whenSingleSuiteContainsFailedTestsAndSingleSuitePreferenceIsSet_failedSuitePathsMapIsReturnedWithSingleSuite()
            throws Exception {
        final RedPreferences preferences = mock(RedPreferences.class);
        when(preferences.shouldUseSingleFileDataSource()).thenReturn(true);

        final ExecutionTreeNode suite = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode test = new ExecutionTreeNode(suite, ElementKind.TEST, "test");
        test.setStatus(Status.FAIL);
        test.setPath(
                URI.create("file://" + projectProvider.getProject().getLocation().toPortableString() + "/suite.robot"));
        suite.addChildren(newArrayList(test));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(suite);

        projectProvider.createFile("suite.robot");

        assertThat(store.getFailedSuitePaths(projectProvider.getProject())).containsEntry("suite.robot",
                newArrayList("test"));
    }

    @Test
    public void whenFailedTestsDoNotExist_emptyFailedSuitePathsMapIsReturned() {
        final ExecutionTreeNode root = new ExecutionTreeNode(null, ElementKind.SUITE, "root");
        final ExecutionTreeNode suite = new ExecutionTreeNode(root, ElementKind.SUITE, "suite");
        root.addChildren(newArrayList(suite));

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getFailedSuitePaths(projectProvider.getProject())).isEmpty();
    }
}
