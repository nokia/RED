/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;

public class ExecutionViewNodesPropertyTesterTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final ExecutionViewNodesPropertyTester tester = new ExecutionViewNodesPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotExecutionView() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                + ExecutionTreeNode.class.getName());

        tester.test(new Object(), "property", null, true);
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsABoolean() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "name");

        assertThat(tester.test(node, ExecutionViewNodesPropertyTester.KIND, null, true)).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "name");

        assertThat(tester.test(node, "unknown_property", null, "kind")).isFalse();
        assertThat(tester.test(node, "unknown_property", null, "kind")).isFalse();
    }

    @Test
    public void testNodeHasGivenKindProperty() {
        final ExecutionTreeNode suiteNode = new ExecutionTreeNode(null, ElementKind.SUITE, "suite");
        final ExecutionTreeNode testNode = new ExecutionTreeNode(null, ElementKind.TEST, "test");

        assertThat(nodeHasGivenKind(suiteNode, "test")).isFalse();
        assertThat(nodeHasGivenKind(suiteNode, "suite")).isTrue();
        assertThat(nodeHasGivenKind(suiteNode, "something")).isFalse();

        assertThat(nodeHasGivenKind(testNode, "test")).isTrue();
        assertThat(nodeHasGivenKind(testNode, "suite")).isFalse();
        assertThat(nodeHasGivenKind(testNode, "something")).isFalse();
    }

    private boolean nodeHasGivenKind(final ExecutionTreeNode node, final String expected) {
        return tester.test(node, ExecutionViewNodesPropertyTester.KIND, null, expected);
    }
}
