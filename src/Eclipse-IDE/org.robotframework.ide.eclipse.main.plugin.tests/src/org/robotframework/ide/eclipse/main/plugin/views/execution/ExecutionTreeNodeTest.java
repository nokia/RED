/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Optional;

import org.junit.Test;
import org.rf.ide.core.execution.Status;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionTreeNode.ElementKind;

public class ExecutionTreeNodeTest {

    @Test
    public void testNodePropertiesGetting() throws Exception {
        final ExecutionTreeNode parent = new ExecutionTreeNode(null, null, "");

        assertThat(new ExecutionTreeNode(null, null, "").getParent()).isNull();
        assertThat(new ExecutionTreeNode(parent, null, "").getParent()).isSameAs(parent);

        assertThat(new ExecutionTreeNode(null, null, "a").getKind()).isNull();
        assertThat(new ExecutionTreeNode(null, ElementKind.SUITE, "").getKind()).isEqualTo(ElementKind.SUITE);
        assertThat(new ExecutionTreeNode(null, ElementKind.TEST, "").getKind()).isEqualTo(ElementKind.TEST);

        assertThat(new ExecutionTreeNode(null, null, "a").getName()).isEqualTo("a");

        assertThat(new ExecutionTreeNode(null, null, "").getPath()).isNull();
        assertThat(new ExecutionTreeNode(null, null, "", new URI("file:///f.robot")).getPath())
                .isEqualTo(new URI("file:///f.robot"));

        assertThat(new ExecutionTreeNode(null, null, "").getMessage()).isEmpty();
        assertThat(new ExecutionTreeNode(null, null, "").getElapsedTime()).isEqualTo(0);
        assertThat(new ExecutionTreeNode(null, null, "").getStatus()).isEqualTo(Optional.empty());
        assertThat(new ExecutionTreeNode(null, null, "").getChildren()).isEmpty();
    }

    @Test
    public void childrenAreProperlyAdded() {
        final ExecutionTreeNode c1 = new ExecutionTreeNode(null, ElementKind.SUITE, "c1");
        final ExecutionTreeNode c2 = new ExecutionTreeNode(null, ElementKind.SUITE, "c2");

        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "");
        node.addChildren(newArrayList(c1, c2));

        assertThat(node.getChildren()).containsExactly(c1, c2);
    }

    @Test
    public void executionStatusIsSetProperly() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "");

        node.setStatus(Status.RUNNING);
        assertThat(node.getStatus()).isEqualTo(Optional.of(Status.RUNNING));

        node.setStatus(Status.FAIL);
        assertThat(node.getStatus()).isEqualTo(Optional.of(Status.FAIL));

        node.setStatus(Status.PASS);
        assertThat(node.getStatus()).isEqualTo(Optional.of(Status.PASS));
    }

    @Test
    public void pathIsSetProperly() throws Exception {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "");

        node.setPath(new URI("file:///path"));
        assertThat(node.getPath()).isEqualTo(new URI("file:///path"));
    }

    @Test
    public void messageIsSetProperly() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "");

        node.setMessage("msg");
        assertThat(node.getMessage()).isEqualTo("msg");
    }

    @Test
    public void elapsedTimeIsSetProperly() {
        final ExecutionTreeNode node = new ExecutionTreeNode(null, ElementKind.SUITE, "");

        node.setElapsedTime(42);
        assertThat(node.getElapsedTime()).isEqualTo(42);
    }

}
