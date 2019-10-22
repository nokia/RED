/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.execution.agent.Status;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.DynamicFlag;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;

public class ExecutionTreeNodeTest {

    @Test
    public void testNodePropertiesGetting() throws Exception {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "", null);

        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getParent()).isNull();
        assertThat(ExecutionTreeNode.newSuiteNode(parent, "", null).getParent()).isSameAs(parent);

        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getKind()).isEqualTo(ElementKind.SUITE);
        assertThat(ExecutionTreeNode.newTestNode(null, "", null).getKind()).isEqualTo(ElementKind.TEST);

        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getNumberOfTests()).isEqualTo(0);
        assertThat(ExecutionTreeNode.newTestNode(null, "", null).getNumberOfTests()).isEqualTo(1);

        assertThat(ExecutionTreeNode.newSuiteNode(null, "a", null).getName()).isEqualTo("a");

        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getPath()).isNull();
        assertThat(ExecutionTreeNode.newSuiteNode(null, "", new URI("file:///f.robot")).getPath())
                .isEqualTo(new URI("file:///f.robot"));

        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(ExecutionTreeNode.newTestNode(null, "", null).getDynamic()).isEqualTo(DynamicFlag.NONE);

        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getMessage()).isEmpty();
        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getElapsedTime()).isEqualTo(-1);
        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getStatus()).isEqualTo(Optional.empty());
        assertThat(ExecutionTreeNode.newSuiteNode(null, "", null).getChildren()).isEmpty();
    }

    @Test
    public void suitesAndTestsAreProperlyAdded() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(null, "s2", null);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(null, "s2", null);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(null, "t1", null);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(null, "t2", null);

        parent.addChildren(suite1);
        assertThat(parent.getChildren()).containsExactly(suite1);

        parent.addChildren(test1);
        assertThat(parent.getChildren()).containsExactly(suite1, test1);

        parent.addChildren(suite2);
        assertThat(parent.getChildren()).containsExactly(suite1, suite2, test1);

        parent.addChildren(test2);
        assertThat(parent.getChildren()).containsExactly(suite1, suite2, test1, test2);
    }

    @Test
    public void executionStatusIsSetProperly() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "", null);

        node.setStatus(Status.RUNNING);
        assertThat(node.getStatus()).isEqualTo(Optional.of(Status.RUNNING));

        node.setStatus(Status.FAIL);
        assertThat(node.getStatus()).isEqualTo(Optional.of(Status.FAIL));

        node.setStatus(Status.PASS);
        assertThat(node.getStatus()).isEqualTo(Optional.of(Status.PASS));
    }

    @Test
    public void numberOfTestsIsSetProperly() throws Exception {
        final ExecutionTreeNode suite = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        assertThat(suite.getNumberOfTests()).isEqualTo(0);

        suite.setNumberOfTests(42);
        assertThat(suite.getNumberOfTests()).isEqualTo(42);
    }

    @Test
    public void pathIsSetProperly() throws Exception {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "", null);

        node.setPath(new URI("file:///path"));
        assertThat(node.getPath()).isEqualTo(new URI("file:///path"));
    }

    @Test
    public void messageIsSetProperly() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "", null);

        node.setMessage("msg");
        assertThat(node.getMessage()).isEqualTo("msg");
    }

    @Test
    public void elapsedTimeIsSetProperly() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "", null);

        node.setElapsedTime(42);
        assertThat(node.getElapsedTime()).isEqualTo(42);
    }
    
    @Test
    public void existingSuiteNodeChildIsReturned_whenItExists() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(9);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(parent, "s1", null);
        suite1.setNumberOfTests(3);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(parent, "s2", null);
        suite2.setNumberOfTests(3);
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(parent, "s3", null);
        suite3.setNumberOfTests(3);
        parent.addChildren(suite1, suite2, suite3);

        final ExecutionTreeNode found = parent.getSuiteOrCreateIfMissing("s1", 42);
        assertThat(found).isSameAs(suite1);
        assertThat(parent.getChildren()).containsExactly(suite1, suite2, suite3);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(9);
        assertThat(suite1.getNumberOfTests()).isEqualTo(3);
        assertThat(suite2.getNumberOfTests()).isEqualTo(3);
        assertThat(suite3.getNumberOfTests()).isEqualTo(3);
    }

    @Test
    public void existingNotYetExecutedSuiteNodeChildIsReturned_whenThereAreSuitesWithTheSameNames() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(6);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(parent, "suite", null);
        suite1.setNumberOfTests(3);
        suite1.setStatus(Status.PASS);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(parent, "suite", null);
        suite2.setNumberOfTests(3);
        parent.addChildren(suite1, suite2);

        final ExecutionTreeNode found = parent.getSuiteOrCreateIfMissing("suite", 3);
        assertThat(found).isSameAs(suite2);
        assertThat(parent.getChildren()).containsExactly(suite1, suite2);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(6);
        assertThat(suite1.getNumberOfTests()).isEqualTo(3);
        assertThat(suite2.getNumberOfTests()).isEqualTo(3);
    }

    @Test
    public void existingSuiteNodeChildIsMovedAndReturned_whenItExistButIsNotFirstWithoutStatus_1() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(9);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(parent, "s1", null);
        suite1.setNumberOfTests(3);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(parent, "s2", null);
        suite2.setNumberOfTests(3);
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(parent, "s3", null);
        suite3.setNumberOfTests(3);
        parent.addChildren(suite1, suite2, suite3);

        final ExecutionTreeNode found = parent.getSuiteOrCreateIfMissing("s2", 42);
        assertThat(found).isSameAs(suite2);
        assertThat(parent.getChildren()).containsExactly(suite2, suite1, suite3);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(suite1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(9);
        assertThat(suite1.getNumberOfTests()).isEqualTo(3);
        assertThat(suite2.getNumberOfTests()).isEqualTo(3);
        assertThat(suite3.getNumberOfTests()).isEqualTo(3);
    }

    @Test
    public void existingSuiteNodeChildIsMovedAndReturned_whenItExistButIsNotFirstWithoutStatus_2() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(9);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(parent, "s1", null);
        suite1.setNumberOfTests(3);
        suite1.setStatus(Status.PASS);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(parent, "s2", null);
        suite2.setNumberOfTests(3);
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(parent, "s3", null);
        suite3.setNumberOfTests(3);
        parent.addChildren(suite1, suite2, suite3);

        final ExecutionTreeNode found = parent.getSuiteOrCreateIfMissing("s3", 42);
        assertThat(found).isSameAs(suite3);
        assertThat(parent.getChildren()).containsExactly(suite1, suite3, suite2);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(suite1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(9);
        assertThat(suite1.getNumberOfTests()).isEqualTo(3);
        assertThat(suite2.getNumberOfTests()).isEqualTo(3);
        assertThat(suite3.getNumberOfTests()).isEqualTo(3);
    }

    @Test
    public void newSuiteNodeChildIsCreatedMovedAndReturned_whenItDoesNotExist_1() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(9);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(parent, "s1", null);
        suite1.setNumberOfTests(3);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(parent, "s2", null);
        suite2.setNumberOfTests(3);
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(parent, "s3", null);
        suite3.setNumberOfTests(3);
        parent.addChildren(suite1, suite2, suite3);

        final ExecutionTreeNode found = parent.getSuiteOrCreateIfMissing("s4", 42);
        assertThat(found).isNotIn(suite1, suite2, suite3);
        assertThat(parent.getChildren()).containsExactly(found, suite1, suite2, suite3);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(found.getDynamic()).isEqualTo(DynamicFlag.ADDED);
        assertThat(suite1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(51);
        assertThat(found.getNumberOfTests()).isEqualTo(42);
        assertThat(suite1.getNumberOfTests()).isEqualTo(3);
        assertThat(suite2.getNumberOfTests()).isEqualTo(3);
        assertThat(suite3.getNumberOfTests()).isEqualTo(3);
    }

    @Test
    public void newSuiteNodeChildIsCreatedMovedAndReturned_whenItDoesNotExist_2() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(9);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(parent, "s1", null);
        suite1.setNumberOfTests(3);
        suite1.setStatus(Status.FAIL);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(parent, "s2", null);
        suite2.setNumberOfTests(3);
        final ExecutionTreeNode suite3 = ExecutionTreeNode.newSuiteNode(parent, "s3", null);
        suite3.setNumberOfTests(3);
        parent.addChildren(suite1, suite2, suite3);

        final ExecutionTreeNode found = parent.getSuiteOrCreateIfMissing("s4", 42);
        assertThat(found).isNotIn(suite1, suite2, suite3);
        assertThat(parent.getChildren()).containsExactly(suite1, found, suite2, suite3);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(suite1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(found.getDynamic()).isEqualTo(DynamicFlag.ADDED);
        assertThat(suite2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(51);
        assertThat(suite1.getNumberOfTests()).isEqualTo(3);
        assertThat(found.getNumberOfTests()).isEqualTo(42);
        assertThat(suite2.getNumberOfTests()).isEqualTo(3);
        assertThat(suite3.getNumberOfTests()).isEqualTo(3);
    }

    @Test
    public void existingTestNodeChildIsReturned_whenItExists() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(3);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(parent, "t1", null);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(parent, "t2", null);
        final ExecutionTreeNode test3 = ExecutionTreeNode.newTestNode(parent, "t3", null);
        parent.addChildren(test1, test2, test3);

        final ExecutionTreeNode found = parent.getTestOrCreateIfMissing("t1");
        assertThat(found).isSameAs(test1);
        assertThat(parent.getChildren()).containsExactly(test1, test2, test3);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(3);
        assertThat(test1.getNumberOfTests()).isEqualTo(1);
        assertThat(test2.getNumberOfTests()).isEqualTo(1);
        assertThat(test3.getNumberOfTests()).isEqualTo(1);
    }
    
    @Test
    public void existingNotYetExecutedTestNodeChildIsReturned_whenThereAreTestsWithTheSameNames() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(2);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(parent, "test", null);
        test1.setStatus(Status.FAIL);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(parent, "test", null);
        parent.addChildren(test1, test2);

        final ExecutionTreeNode found = parent.getTestOrCreateIfMissing("test");
        assertThat(found).isSameAs(test2);
        assertThat(parent.getChildren()).containsExactly(test1, test2);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(2);
        assertThat(test1.getNumberOfTests()).isEqualTo(1);
        assertThat(test2.getNumberOfTests()).isEqualTo(1);
    }

    @Test
    public void existingTestNodeChildIsMovedAndReturned_whenItExistButIsNotFirstWithoutStatus_1() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(3);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(parent, "t1", null);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(parent, "t2", null);
        final ExecutionTreeNode test3 = ExecutionTreeNode.newTestNode(parent, "t3", null);
        parent.addChildren(test1, test2, test3);

        final ExecutionTreeNode found = parent.getTestOrCreateIfMissing("t2");
        assertThat(found).isSameAs(test2);
        assertThat(parent.getChildren()).containsExactly(test2, test1, test3);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(test1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(3);
        assertThat(test1.getNumberOfTests()).isEqualTo(1);
        assertThat(test2.getNumberOfTests()).isEqualTo(1);
        assertThat(test3.getNumberOfTests()).isEqualTo(1);
    }

    @Test
    public void existingTestNodeChildIsMovedAndReturned_whenItExistButIsNotFirstWithoutStatus_2() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(3);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(parent, "t1", null);
        test1.setStatus(Status.PASS);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(parent, "t2", null);
        final ExecutionTreeNode test3 = ExecutionTreeNode.newTestNode(parent, "t3", null);
        parent.addChildren(test1, test2, test3);

        final ExecutionTreeNode found = parent.getTestOrCreateIfMissing("t3");
        assertThat(found).isSameAs(test3);
        assertThat(parent.getChildren()).containsExactly(test1, test3, test2);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(test1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(3);
        assertThat(test1.getNumberOfTests()).isEqualTo(1);
        assertThat(test2.getNumberOfTests()).isEqualTo(1);
        assertThat(test3.getNumberOfTests()).isEqualTo(1);
    }

    @Test
    public void newTestNodeChildIsCreatedMovedAndReturned_whenItDoesNotExist_1() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(3);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(parent, "t1", null);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(parent, "t2", null);
        final ExecutionTreeNode test3 = ExecutionTreeNode.newTestNode(parent, "t3", null);
        parent.addChildren(test1, test2, test3);

        final ExecutionTreeNode found = parent.getTestOrCreateIfMissing("t4");
        assertThat(found).isNotIn(test1, test2, test3);
        assertThat(parent.getChildren()).containsExactly(found, test1, test2, test3);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(found.getDynamic()).isEqualTo(DynamicFlag.ADDED);
        assertThat(test1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(4);
        assertThat(found.getNumberOfTests()).isEqualTo(1);
        assertThat(test1.getNumberOfTests()).isEqualTo(1);
        assertThat(test2.getNumberOfTests()).isEqualTo(1);
        assertThat(test3.getNumberOfTests()).isEqualTo(1);
    }

    @Test
    public void newTestNodeChildIsCreatedMovedAndReturned_whenItDoesNotExist_2() {
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(null, "parent", null);
        parent.setNumberOfTests(3);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(parent, "t1", null);
        test1.setStatus(Status.FAIL);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(parent, "t2", null);
        final ExecutionTreeNode test3 = ExecutionTreeNode.newTestNode(parent, "t3", null);
        parent.addChildren(test1, test2, test3);

        final ExecutionTreeNode found = parent.getTestOrCreateIfMissing("t4");
        assertThat(found).isNotIn(test1, test2, test3);
        assertThat(parent.getChildren()).containsExactly(test1, found, test2, test3);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(test1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(found.getDynamic()).isEqualTo(DynamicFlag.ADDED);
        assertThat(test2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test3.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getNumberOfTests()).isEqualTo(4);
        assertThat(test1.getNumberOfTests()).isEqualTo(1);
        assertThat(found.getNumberOfTests()).isEqualTo(1);
        assertThat(test2.getNumberOfTests()).isEqualTo(1);
        assertThat(test3.getNumberOfTests()).isEqualTo(1);
    }

    @Test
    public void numberOfTestsIsUpdatedAndNotExecutedElementsAreMarkedRemoved_whenThereAreElementsWithoutStatus() {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        root.setNumberOfTests(8);
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(root, "parent", null);
        parent.setNumberOfTests(8);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(parent, "t1", null);
        test1.setStatus(Status.FAIL);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(parent, "t2", null);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(parent, "s1", null);
        suite1.setStatus(Status.PASS);
        suite1.setNumberOfTests(3);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(parent, "s2", null);
        suite2.setNumberOfTests(3);

        root.addChildren(parent);
        parent.addChildren(suite1, suite2, test1, test2);

        parent.updateNumberOfTestsBalanceOnSuiteEnd();

        assertThat(root.getNumberOfTests()).isEqualTo(4);
        assertThat(parent.getNumberOfTests()).isEqualTo(4);
        assertThat(test1.getNumberOfTests()).isEqualTo(1);
        assertThat(test2.getNumberOfTests()).isEqualTo(1);
        assertThat(suite1.getNumberOfTests()).isEqualTo(3);
        assertThat(suite2.getNumberOfTests()).isEqualTo(3);

        assertThat(root.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.OTHER);
        assertThat(test1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test2.getDynamic()).isEqualTo(DynamicFlag.REMOVED);
        assertThat(suite1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite2.getDynamic()).isEqualTo(DynamicFlag.REMOVED);
    }

    @Test
    public void numberOfTestsIsNotUpdatedNothingChangesInDynamicFlags_whenAllChildElementsHasStatus() {
        final ExecutionTreeNode root = ExecutionTreeNode.newSuiteNode(null, "root", null);
        root.setNumberOfTests(8);
        final ExecutionTreeNode parent = ExecutionTreeNode.newSuiteNode(root, "parent", null);
        parent.setNumberOfTests(8);
        final ExecutionTreeNode test1 = ExecutionTreeNode.newTestNode(parent, "t1", null);
        test1.setStatus(Status.FAIL);
        final ExecutionTreeNode test2 = ExecutionTreeNode.newTestNode(parent, "t2", null);
        test2.setStatus(Status.PASS);
        final ExecutionTreeNode suite1 = ExecutionTreeNode.newSuiteNode(parent, "s1", null);
        suite1.setStatus(Status.PASS);
        suite1.setNumberOfTests(3);
        final ExecutionTreeNode suite2 = ExecutionTreeNode.newSuiteNode(parent, "s2", null);
        suite2.setStatus(Status.FAIL);
        suite2.setNumberOfTests(3);

        root.addChildren(parent);
        parent.addChildren(suite1, suite2, test1, test2);

        parent.updateNumberOfTestsBalanceOnSuiteEnd();

        assertThat(root.getNumberOfTests()).isEqualTo(8);
        assertThat(parent.getNumberOfTests()).isEqualTo(8);
        assertThat(test1.getNumberOfTests()).isEqualTo(1);
        assertThat(test2.getNumberOfTests()).isEqualTo(1);
        assertThat(suite1.getNumberOfTests()).isEqualTo(3);
        assertThat(suite2.getNumberOfTests()).isEqualTo(3);

        assertThat(root.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(parent.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(test2.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite1.getDynamic()).isEqualTo(DynamicFlag.NONE);
        assertThat(suite2.getDynamic()).isEqualTo(DynamicFlag.NONE);
    }
}
