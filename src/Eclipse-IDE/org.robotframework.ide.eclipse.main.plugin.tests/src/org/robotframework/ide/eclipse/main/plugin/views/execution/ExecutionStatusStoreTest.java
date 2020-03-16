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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.rf.ide.core.execution.agent.Status;
import org.rf.ide.core.execution.agent.event.SuiteStartedEvent.ExecutionMode;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.launch.local.RobotLaunchConfiguration;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;
import org.robotframework.red.junit.jupiter.RedTempDirectory;
import org.robotframework.red.junit.jupiter.StatefulProject;

@ExtendWith({ ProjectExtension.class, RedTempDirectory.class, PreferencesExtension.class })
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

    @BooleanPreference(key = RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, value = true)
    @Test
    public void whenSingleSuiteContainsFailedTestsAndSingleSuitePreferenceIsSet_failedSuitePathsMapIsReturnedWithSingleSuite()
            throws Exception {
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
    public void whenManySuitesContainNonExecutedTests_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite1.robot", new ArrayList<String>());
        suitePaths.put("suite2.robot", new ArrayList<String>());
        suitePaths.put("suite3.robot", new ArrayList<String>());
        suitePaths.put("suite4.robot", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");
        project.createFile("suite3.robot");
        project.createFile("suite4.robot");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", project.getLocationURI());
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "Suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "Suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(root, "Suite3", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite3.robot"));
        final ExecutionTreeNode suite4 = ExecutionTreeNode.newSuiteNode(root, "Suite4", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite4.robot"));

        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(suite1, "testFailed1", suite1.getPath());
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(suite2, "testFailed2", suite2.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "test2",
                suite2.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite3, "test3", suite3.getPath());
        final ExecutionTreeNode testNonExecuted4 = ExecutionTreeNode.newTestNode(suite4, "test4", suite4.getPath());
        suite1.setStatus(Status.FAIL);
        test1.setStatus(Status.FAIL);
        test2.setStatus(Status.FAIL);

        suite1.addChildren(test1);
        suite2.addChildren(test2, testNonExecuted2);
        suite3.addChildren(testNonExecuted3);
        suite4.addChildren(testNonExecuted4);
        root.addChildren(suite1, suite2, suite3, suite4);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest.Suite2.test2")
                .contains("ExecutionStatusStoreTest.Suite3.test3")
                .contains("ExecutionStatusStoreTest.Suite4.test4");
    }

    @Test
    public void whenManyNastedSuitesContainNonExecutedTests_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("dir1", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        project.createDir("dir1");
        project.createDir("dir1/dir2");
        project.createDir("dir1/dir2/dir3");
        project.createFile("dir1/suite1.robot");
        project.createFile("dir1/dir2/suite2.robot", "*** Test Cases ***", "test2",
                "  log    2");
        project.createFile("dir1/dir2/dir3/suite3.robot", "*** Test Cases ***", "test3",
                "  log    2");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", project.getLocationURI());
        final ExecutionTreeNode dir1 = ExecutionTreeNode.newSuiteNode(root, "dir1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/dir1"));
        final ExecutionTreeNode dir2 = ExecutionTreeNode.newSuiteNode(dir1, "dir2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/dir1/dir2"));
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(dir1, "Suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/dir1/suite1.robot"));

        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(suite1, "Test Failed1", suite1.getPath());
        suite1.setStatus(Status.FAIL);
        test1.setStatus(Status.FAIL);

        suite1.addChildren(test1);
        dir1.addChildren(dir2, suite1);
        root.addChildren(dir1);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest.dir1.dir2.suite2.test2")
                .contains("ExecutionStatusStoreTest.dir1.dir2.dir3.suite3.test3");
    }

    @Test
    public void whenManySuitesAndLinkedResourceContainNonExecutedTests_nonExecutedTestsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite1.robot", new ArrayList<String>());
        suitePaths.put("suite2.robot", new ArrayList<String>());
        suitePaths.put("suite3.robot", new ArrayList<String>());
        suitePaths.put("linked_dir", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");
        project.createFile("suite3.robot");

        final File linkedNonWorkspaceDir = RedTempDirectory.createNewDir(tempFolder, "linked_dir");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir, "linked_suite.robot", "*** Test Cases ***", "test4",
                "  log    2");
        project.createDirLink("linked_dir", linkedNonWorkspaceDir.toURI());
        project.getFile("linked_dir");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(root, "suite3", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite3.robot"));
        final ExecutionTreeNode suiteLinked4 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir",
                URI.create(linkedNonWorkspaceDir.toURI().toString()));

        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(suite1, "testFailed1", suite1.getPath());
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(suite2, "testFailed1", suite2.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite3, "test3", suite3.getPath());

        suite1.setStatus(Status.FAIL);
        test1.setStatus(Status.FAIL);
        test2.setStatus(Status.FAIL);
        suite1.addChildren(test1);
        suite2.addChildren(test2, testNonExecuted2);
        suite3.addChildren(testNonExecuted3);
        root.addChildren(suite1, suite2, suite3, suiteLinked4);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir.suite2.test2")
                .contains("ExecutionStatusStoreTest & Linked Dir.suite3.test3")
                .contains("ExecutionStatusStoreTest & Linked Dir.Linked Dir.linked_suite.test4");
    }

    @BooleanPreference(key = RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, value = true)
    @Test
    public void whenManySuitesAndLinkedResourceContainNonExecutedTestsAndSingleSuitePreferenceIsSet_nonExecutedTestsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("linked_dir1", new ArrayList<String>());
        suitePaths.put("linked_dir2", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);

        final File linkedNonWorkspaceDir1 = RedTempDirectory.createNewDir(tempFolder, "linked_dir1");
        final File linkedNonWorkspaceDir2 = RedTempDirectory.createNewDir(tempFolder, "linked_dir2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir1, "linked_suite.robot", "*** Test Cases ***", "test1",
                "  log    2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir2, "linked_suite.robot", "*** Test Cases ***", "test2",
                "  log    2");
        project.createDirLink("linked_dir1", linkedNonWorkspaceDir1.toURI());
        project.createDirLink("linked_dir2", linkedNonWorkspaceDir2.toURI());
        project.getFile("linked_dir1");
        project.getFile("linked_dir2");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                project.getLocationURI());
        final ExecutionTreeNode suiteLinked1 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir1",
                URI.create(linkedNonWorkspaceDir1.toURI().toString()));
        final ExecutionTreeNode suiteLinked2 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir2", null);

        root.addChildren(suiteLinked1, suiteLinked2);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir1 & Linked Dir2.Linked Dir1.linked_suite.test1")
                .contains("ExecutionStatusStoreTest & Linked Dir1 & Linked Dir2.Linked Dir2.linked_suite.test2");
    }

    @Test
    public void whenLinkedResourcesContainNonExecutedTestsAndItsRobotNamesAreTheSame_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite1.robot", new ArrayList<String>());
        suitePaths.put("suite2.robot", new ArrayList<String>());
        suitePaths.put("linked_dir", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");

        final File linkedNonWorkspaceDir = RedTempDirectory.createNewDir(tempFolder, "linked_dir");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir, "linked__suite.robot", "*** Test Cases ***", "test3",
                "  log    2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir, "linked___suite.robot", "*** Test Cases ***", "test4",
                "  log    2");
        project.createDirLink("linked_dir", linkedNonWorkspaceDir.toURI());
        project.getFile("linked_dir");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suiteLinked3 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir",
                URI.create(linkedNonWorkspaceDir.toURI().toString()));
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(suite1, "testFailed1", suite1.getPath());
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(suite2, "testFailed2", suite2.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "test2", suite2.getPath());

        suite1.setStatus(Status.FAIL);
        test1.setStatus(Status.FAIL);
        test2.setStatus(Status.FAIL);
        suite1.addChildren(test1);
        suite2.addChildren(test2, testNonExecuted2);
        root.addChildren(suite1, suite2, suiteLinked3);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir.suite2.test2")
                .contains("ExecutionStatusStoreTest & Linked Dir.Linked Dir.linked__suite.test3")
                .contains("ExecutionStatusStoreTest & Linked Dir.Linked Dir.linked___suite.test4");
    }

    @Test
    public void whenLinkedResourcesContainNonExecutedTestsAndOneOfThemHasPath_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("linked_dir1", new ArrayList<String>());
        suitePaths.put("linked_dir2", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);

        final File linkedNonWorkspaceDir1 = RedTempDirectory.createNewDir(tempFolder, "linked_dir1");
        final File linkedNonWorkspaceDir2 = RedTempDirectory.createNewDir(tempFolder, "linked_dir2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir1, "linked_suite.robot");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir2, "linked_suite.robot", "*** Test Cases ***", "test2",
                "  log    2");
        project.createDirLink("linked_dir1", linkedNonWorkspaceDir1.toURI());
        project.createDirLink("linked_dir2", linkedNonWorkspaceDir2.toURI());
        project.getFile("linked_dir1");
        project.getFile("linked_dir1");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                project.getLocationURI());
        final ExecutionTreeNode suiteLinked1 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir1",
                URI.create(linkedNonWorkspaceDir1.toURI().toString()));
        final ExecutionTreeNode suiteNestedInLinked1 = ExecutionTreeNode.newSuiteNode(suiteLinked1,
                "linked_suite", URI.create(linkedNonWorkspaceDir1.toURI().toString()));
        final ExecutionTreeNode suiteLinked2 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir2", null);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(suiteNestedInLinked1, "testFailed1",
                suiteLinked1.getPath());
        final ExecutionTreeNode testNonExecuted1 = ExecutionTreeNode.newTestNode(suiteNestedInLinked1, "test1",
                suiteLinked1.getPath());
        test1.setStatus(Status.FAIL);
        testNonExecuted1.setStatus(Status.RUNNING);
        suiteNestedInLinked1.addChildren(test1, testNonExecuted1);
        suiteLinked1.addChildren(suiteNestedInLinked1);
        root.addChildren(suiteLinked1, suiteLinked2);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir1 & Linked Dir2.Linked Dir1.linked_suite.test1")
                .contains("ExecutionStatusStoreTest & Linked Dir1 & Linked Dir2.Linked Dir2.linked_suite.test2");
    }

    @Test
    public void whenSingleSuiteContainsNonExecutedTests_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite.robot", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);

        project.createFile("suite.robot");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "Suite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite, "test1", suite.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite, "test2", suite.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite, "test3", suite.getPath());
        testFailed1.setStatus(Status.FAIL);
        suite.addChildren(testFailed1, testNonExecuted2, testNonExecuted3);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest.Suite.test2")
                .contains("ExecutionStatusStoreTest.Suite.test3");
    }

    @Test
    public void whenSingleNestedSuiteContainsNonExecutedTests_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite.robot", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);

        project.createFile("suite.robot");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode dir1 = ExecutionTreeNode.newSuiteNode(root, "dir1", null);
        final ExecutionTreeNode dir2 = ExecutionTreeNode.newSuiteNode(dir1, "dir2", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(dir2, "Suite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite, "test1", suite.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite, "test2", suite.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite, "test3", suite.getPath());
        testFailed1.setStatus(Status.FAIL);
        suite.addChildren(testFailed1, testNonExecuted2, testNonExecuted3);
        dir2.addChildren(suite);
        dir1.addChildren(dir2);
        root.addChildren(dir1);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest.dir1.dir2.Suite.test2")
                .contains("ExecutionStatusStoreTest.dir1.dir2.Suite.test3");
    }

    @BooleanPreference(key = RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, value = true)
    @Test
    public void whenSingleSuiteContainsNonExecutedTestsAndSingleSuitePreferenceIsSet_nonExecutedTestsPathsAreReturned()
            throws Exception {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite.robot", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);

        project.createFile("suite.robot");

        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(null, "Suite",
                URI.create("file://" + project.getLocation().toPortableString() + "/suite.robot"));
        final ExecutionTreeNode test = ExecutionTreeNode.newTestNode(suite, "test", suite.getPath());
        suite.addChildren(test);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(suite);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("Suite.test");
    }

    @Test
    public void whenSingleSuiteFromLinkedResourceContainsNonExecutedTests_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("linked_suite.robot", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        final File linkedNonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "linked_suite.robot");
        project.getFile("linked_suite.robot");
        project.createFileLink("linked_suite.robot", linkedNonWorkspaceFile.toURI());

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "Linked Suite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite, "test1", suite.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite, "test2", suite.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite, "test3", suite.getPath());
        testFailed1.setStatus(Status.FAIL);
        suite.addChildren(testFailed1, testNonExecuted2, testNonExecuted3);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest.Linked Suite.test2")
                .contains("ExecutionStatusStoreTest.Linked Suite.test3");
    }

    @Test
    public void whenSingleNestedSuiteFromLinkedResourceContainsNonExecutedTests_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("linked_dir1", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        final File linkedNonWorkspaceDir1 = RedTempDirectory.createNewDir(tempFolder, "linked_dir1");
        final File linkedNonWorkspaceDir2 = RedTempDirectory.createNewDir(linkedNonWorkspaceDir1, "linked_dir2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir2, "linked_suite.robot");
        project.createDirLink("linked_dir1", linkedNonWorkspaceDir1.toURI());
        project.getDir("linked_dir1");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode dir1 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir1", null);
        final ExecutionTreeNode dir2 = ExecutionTreeNode.newSuiteNode(dir1, "Linked Dir2", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(dir2, "Linked Suite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite, "test1", suite.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite, "test2", suite.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite, "test3", suite.getPath());
        testFailed1.setStatus(Status.FAIL);
        suite.addChildren(testFailed1, testNonExecuted2, testNonExecuted3);
        dir2.addChildren(suite);
        dir1.addChildren(dir2);
        root.addChildren(dir1);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir1.Linked Dir1.Linked Dir2.Linked Suite.test2")
                .contains("ExecutionStatusStoreTest & Linked Dir1.Linked Dir1.Linked Dir2.Linked Suite.test3");
    }

    @BooleanPreference(key = RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, value = true)
    @Test
    public void whenSingleSuiteFromLinkedResourceContainsNonExecutedTestsAndSingleSuitePreferenceIsSet_nonExecutedTestsPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("linked_suite.robot", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        final File linkedNonWorkspaceFile = RedTempDirectory.createNewFile(tempFolder, "linked_suite.robot");
        project.getFile("linked_suite.robot");
        project.createFileLink("linked_suite.robot", linkedNonWorkspaceFile.toURI());

        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(null, "Linked Suite", null);
        final ExecutionTreeNode testFailed1 = ExecutionTreeNode.newTestNode(suite, "test1", suite.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite, "test2", suite.getPath());
        final ExecutionTreeNode testNonExecuted3 = ExecutionTreeNode.newTestNode(suite, "test3", suite.getPath());
        testFailed1.setStatus(Status.FAIL);
        suite.addChildren(testFailed1, testNonExecuted2, testNonExecuted3);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(suite);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("Linked Suite.test2")
                .contains("Linked Suite.test3");
    }

    @Test
    public void whenManySuitesContainNonExecutedTasks_nonExecutedTasksPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite1.robot", new ArrayList<String>());
        suitePaths.put("suite2.robot", new ArrayList<String>());
        suitePaths.put("suite3.robot", new ArrayList<String>());
        suitePaths.put("suite4.robot", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");
        project.createFile("suite3.robot", "*** Tasks ***", "Task3",
                "  log    2");
        project.createFile("suite4.robot", "*** Tasks ***", "Task4",
                "  log    2");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", project.getLocationURI());
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "Suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "Suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(root, "Suite3", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite3.robot"));
        final ExecutionTreeNode suite4 = ExecutionTreeNode.newSuiteNode(root, "Suite4", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite4.robot"));

        final ExecutionTreeNode task1 = ExecutionTreeNode.newTestNode(suite1, "Task Passed1", suite1.getPath());
        final ExecutionTreeNode task2 = ExecutionTreeNode.newTestNode(suite2, "Task Passed2", suite2.getPath());
        final ExecutionTreeNode taskNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "Task2",
                suite2.getPath());
        suite1.setStatus(Status.PASS);
        task1.setStatus(Status.PASS);
        task2.setStatus(Status.PASS);

        suite1.addChildren(task1);
        suite2.addChildren(task2, taskNonExecuted2);
        root.addChildren(suite1, suite2, suite3, suite4);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest.Suite2.Task2")
                .contains("ExecutionStatusStoreTest.Suite3.Task3")
                .contains("ExecutionStatusStoreTest.Suite4.Task4");
    }

    @Test
    public void whenManyNastedSuitesContainNonExecutedTasks_nonExecutedTasksPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("dir1", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        project.createDir("dir1");
        project.createDir("dir1/dir2");
        project.createDir("dir1/dir2/dir3");
        project.createFile("dir1/suite1.robot");
        project.createFile("dir1/dir2/suite2_task.robot", "*** Tasks ***", "Task2",
                "  log    2");
        project.createFile("dir1/dir2/dir3/suite3_task.robot", "*** Tasks ***", "Task3",
                "  log    2");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", project.getLocationURI());
        final ExecutionTreeNode dir1 = ExecutionTreeNode.newSuiteNode(root, "dir1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/dir1"));
        final ExecutionTreeNode dir2 = ExecutionTreeNode.newSuiteNode(dir1, "dir2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/dir1/dir2"));
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(dir1, "Suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/dir1/suite1.robot"));

        final ExecutionTreeNode task1 = ExecutionTreeNode.newTestNode(suite1, "Task Passed1", suite1.getPath());
        suite1.setStatus(Status.PASS);
        task1.setStatus(Status.PASS);

        suite1.addChildren(task1);
        dir1.addChildren(dir2, suite1);
        root.addChildren(dir1);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest.dir1.dir2.suite2_task.Task2")
                .contains("ExecutionStatusStoreTest.dir1.dir2.dir3.suite3_task.Task3");
    }

    @Test
    public void whenManySuitesAndLinkedResourceContainNonExecutedTasks_nonExecutedTasksAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite1.robot", new ArrayList<String>());
        suitePaths.put("suite2.robot", new ArrayList<String>());
        suitePaths.put("suite3.robot", new ArrayList<String>());
        suitePaths.put("linked_dir", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");
        project.createFile("suite3.robot");

        final File linkedNonWorkspaceDir = RedTempDirectory.createNewDir(tempFolder, "linked_dir");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir, "linked_suite_task.robot", "*** Tasks ***", "Task4",
                "  log    2");
        project.createDirLink("linked_dir", linkedNonWorkspaceDir.toURI());
        project.getFile("linked_dir");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(root, "suite3", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite3.robot"));
        final ExecutionTreeNode suiteLinked4 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir",
                URI.create(linkedNonWorkspaceDir.toURI().toString()));

        final ExecutionTreeNode task1 = ExecutionTreeNode.newTestNode(suite1, "Task Passed1", suite1.getPath());
        final ExecutionTreeNode taks2 = ExecutionTreeNode.newTestNode(suite2, "Task Passed2", suite2.getPath());
        final ExecutionTreeNode taskNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "Task2", suite2.getPath());
        final ExecutionTreeNode taskNonExecuted3 = ExecutionTreeNode.newTestNode(suite3, "Task3", suite3.getPath());

        suite1.setStatus(Status.PASS);
        task1.setStatus(Status.PASS);
        taks2.setStatus(Status.PASS);
        suite1.addChildren(task1);
        suite2.addChildren(taks2, taskNonExecuted2);
        suite3.addChildren(taskNonExecuted3);
        root.addChildren(suite1, suite2, suite3, suiteLinked4);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir.suite2.Task2")
                .contains("ExecutionStatusStoreTest & Linked Dir.suite3.Task3")
                .contains("ExecutionStatusStoreTest & Linked Dir.Linked Dir.linked_suite_task.Task4");
    }

    @BooleanPreference(key = RedPreferences.LAUNCH_USE_SINGLE_FILE_DATA_SOURCE, value = true)
    @Test
    public void whenManySuitesAndLinkedResourceContainNonExecutedTasksAndSingleSuitePreferenceIsSet_nonExecutedTasksAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("linked_dir1", new ArrayList<String>());
        suitePaths.put("linked_dir2", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);

        final File linkedNonWorkspaceDir1 = RedTempDirectory.createNewDir(tempFolder, "linked_dir1");
        final File linkedNonWorkspaceDir2 = RedTempDirectory.createNewDir(tempFolder, "linked_dir2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir1, "linked_suite_task.robot", "*** Tasks ***", "Task1",
                "  log    2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir2, "linked_suite_task.robot", "*** Tasks ***", "Task2",
                "  log    2");
        project.createDirLink("linked_dir1", linkedNonWorkspaceDir1.toURI());
        project.createDirLink("linked_dir2", linkedNonWorkspaceDir2.toURI());
        project.getFile("linked_dir1");
        project.getFile("linked_dir2");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                project.getLocationURI());
        final ExecutionTreeNode suiteLinked1 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir1",
                URI.create(linkedNonWorkspaceDir1.toURI().toString()));
        final ExecutionTreeNode suiteLinked2 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir2", null);

        root.addChildren(suiteLinked1, suiteLinked2);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir1 & Linked Dir2.Linked Dir1.linked_suite_task.Task1")
                .contains("ExecutionStatusStoreTest & Linked Dir1 & Linked Dir2.Linked Dir2.linked_suite_task.Task2");
    }

    @Test
    public void whenLinkedResourcesContainNonExecutedTaskssAndItsRobotNamesAreTheSame_nonExecutedTasksPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("suite1.robot", new ArrayList<String>());
        suitePaths.put("suite2.robot", new ArrayList<String>());
        suitePaths.put("linked_dir", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");

        project.createFile("suite1.robot");
        project.createFile("suite2.robot");

        final File linkedNonWorkspaceDir = RedTempDirectory.createNewDir(tempFolder, "linked_dir");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir, "linked__suite_task.robot", "*** Tasks ***", "Task3",
                "  log    2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir, "linked___suite_task.robot", "*** Tasks ***", "Task4",
                "  log    2");
        project.createDirLink("linked_dir", linkedNonWorkspaceDir.toURI());
        project.getFile("linked_dir");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(root, "suite1", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite1.robot"));
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(root, "suite2", URI
                .create("file:///" + project.getLocation().toPortableString() + "/suite2.robot"));
        final ExecutionTreeNode suiteLinked3 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir",
                URI.create(linkedNonWorkspaceDir.toURI().toString()));
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(suite1, "Task Passed1", suite1.getPath());
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(suite2, "Task Passed2", suite2.getPath());
        final ExecutionTreeNode testNonExecuted2 = ExecutionTreeNode.newTestNode(suite2, "Task2", suite2.getPath());

        suite1.setStatus(Status.PASS);
        test1.setStatus(Status.PASS);
        test2.setStatus(Status.PASS);
        suite1.addChildren(test1);
        suite2.addChildren(test2, testNonExecuted2);
        root.addChildren(suite1, suite2, suiteLinked3);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir.suite2.Task2")
                .contains("ExecutionStatusStoreTest & Linked Dir.Linked Dir.linked__suite_task.Task3")
                .contains("ExecutionStatusStoreTest & Linked Dir.Linked Dir.linked___suite_task.Task4");
    }

    @Test
    public void whenLinkedResourcesContainNonExecutedTasksAndOneOfThemHasPath_nonExecutedTasksPathsAreReturned()
            throws IOException, CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        suitePaths.put("linked_dir1", new ArrayList<String>());
        suitePaths.put("linked_dir2", new ArrayList<String>());
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);

        final File linkedNonWorkspaceDir1 = RedTempDirectory.createNewDir(tempFolder, "linked_dir1");
        final File linkedNonWorkspaceDir2 = RedTempDirectory.createNewDir(tempFolder, "linked_dir2");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir1, "linked_suite_task.robot");
        RedTempDirectory.createNewFile(linkedNonWorkspaceDir2, "linked_suite_task.robot", "*** Tasks ***", "Task2",
                "  log    2");
        project.createDirLink("linked_dir1", linkedNonWorkspaceDir1.toURI());
        project.createDirLink("linked_dir2", linkedNonWorkspaceDir2.toURI());
        project.getFile("linked_dir1");
        project.getFile("linked_dir1");

        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root",
                project.getLocationURI());
        final ExecutionTreeNode suiteLinked1 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir1",
                URI.create(linkedNonWorkspaceDir1.toURI().toString()));
        final ExecutionTreeNode suiteNestedInLinked1 = ExecutionTreeNode.newSuiteNode(suiteLinked1,
                "linked_suite_task", URI.create(linkedNonWorkspaceDir1.toURI().toString()));
        final ExecutionTreeNode suiteLinked2 = ExecutionTreeNode.newSuiteNode(root, "Linked Dir2", null);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(suiteNestedInLinked1, "Task Passed1",
                suiteLinked1.getPath());
        final ExecutionTreeNode testNonExecuted1 = ExecutionTreeNode.newTestNode(suiteNestedInLinked1, "Task1",
                suiteLinked1.getPath());
        test1.setStatus(Status.PASS);
        testNonExecuted1.setStatus(Status.RUNNING);
        suiteNestedInLinked1.addChildren(test1, testNonExecuted1);
        suiteLinked1.addChildren(suiteNestedInLinked1);
        root.addChildren(suiteLinked1, suiteLinked2);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .contains("ExecutionStatusStoreTest & Linked Dir1 & Linked Dir2.Linked Dir1.linked_suite_task.Task1")
                .contains("ExecutionStatusStoreTest & Linked Dir1 & Linked Dir2.Linked Dir2.linked_suite_task.Task2");
    }

    @Test
    public void whenNonExecutedTestsOrTasksDoNotExist_emptyNonExecutedTestsOrTasksPathsAreReturned()
            throws CoreException {
        final RobotLaunchConfiguration robotConfig = mock(RobotLaunchConfiguration.class);
        final Map<String, List<String>> suitePaths = new HashMap<>();
        when(robotConfig.getSelectedSuitePaths()).thenReturn(suitePaths);
        when(robotConfig.getRobotArguments()).thenReturn("");
        
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(root, "Suite", null);
        suite.setStatus(Status.FAIL);
        root.addChildren(suite);

        final ExecutionStatusStore store = new ExecutionStatusStore();
        store.setExecutionTree(root);

        assertThat(store.getNonExecutedTestsOrTasksPaths(project.getProject(), robotConfig))
                .isEmpty();
    }
}
