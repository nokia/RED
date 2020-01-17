/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode;

public class ExecutionViewNodesPropertyTesterTest {

    private final ExecutionViewNodesPropertyTester tester = new ExecutionViewNodesPropertyTester();

    @Test
    public void exceptionIsThrown_whenReceiverIsNotExecutionView() {
        assertThatIllegalArgumentException().isThrownBy(() -> tester.test(new Object(), "property", null, true))
                .withMessage("Property tester is unable to test properties of java.lang.Object. It should be used with "
                        + ExecutionTreeNode.class.getName())
                .withNoCause();
    }

    @Test
    public void falseIsReturned_whenExpectedValueIsABoolean() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "name", null);

        assertThat(tester.test(node, ExecutionViewNodesPropertyTester.KIND, null, true)).isFalse();
    }

    @Test
    public void falseIsReturnedForUnknownProperty() {
        final ExecutionTreeNode node = ExecutionTreeNode.newSuiteNode(null, "name", null);

        assertThat(tester.test(node, "unknown_property", null, "kind")).isFalse();
        assertThat(tester.test(node, "unknown_property", null, "kind")).isFalse();
    }

    @Test
    public void testNodeHasGivenKindProperty() {
        final ExecutionTreeNode suiteNode = ExecutionTreeNode.newSuiteNode(null, "suite", null);
        final ExecutionTreeNode testNode = ExecutionTreeNode.newTestNode(null, "test", null);

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
