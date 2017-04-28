/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.rf.ide.core.execution.Status;

public class ExecutionViewContentProviderTest {

    @Test
    public void arrayOfNodesIsReturnedAsElements_whenProviderIsAskedForInputWithList() {
        final ExecutionViewContentProvider provider = new ExecutionViewContentProvider();

        final ExecutionTreeNode n1 = node(null);
        final ExecutionTreeNode n2 = node(null);

        assertThat(provider.getElements(newArrayList())).isEmpty();
        assertThat(provider.getElements(newArrayList(n1))).containsExactly(n1);
        assertThat(provider.getElements(newArrayList(n1, n2))).containsExactly(n1, n2);
    }

    @Test
    public void parentOfNodeIsReturned_whenProviderIsAskedForParent() {
        final ExecutionViewContentProvider provider = new ExecutionViewContentProvider();
        final ExecutionTreeNode parent = node(null);

        assertThat(provider.getParent(node(null))).isNull();
        assertThat(provider.getParent(node(parent))).isSameAs(parent);
    }

    @Test
    public void onlyForNodesWithChildren_providerSaysTheyHaveChildren() {
        final ExecutionViewContentProvider provider = new ExecutionViewContentProvider();

        assertThat(provider.hasChildren(node(null))).isFalse();
        assertThat(provider.hasChildren(node(null, node(null)))).isTrue();
        assertThat(provider.hasChildren(node(null, node(null), node(null)))).isTrue();
    }

    @Test
    public void nodeChildrenAreProvided_whenProviderIsAskedForChildren() {
        final ExecutionViewContentProvider provider = new ExecutionViewContentProvider();

        final ExecutionTreeNode c1 = node(null, Status.PASS);
        final ExecutionTreeNode c2 = node(null, Status.FAIL);
        final ExecutionTreeNode c3 = node(null, Status.RUNNING);
        final ExecutionTreeNode c4 = node(null);
        assertThat(provider.getChildren(node(null, c1, c2, c3, c4))).containsExactly(c1, c2, c3, c4);
    }

    @Test
    public void onlyFailedNodeChildrenAreProvided_whenProviderIsAskedForChildrenAdnFilterIsSwitched() {
        final ExecutionViewContentProvider provider = new ExecutionViewContentProvider();
        provider.switchFailedFilter();

        final ExecutionTreeNode c1 = node(null, Status.PASS);
        final ExecutionTreeNode c2 = node(null, Status.FAIL);
        final ExecutionTreeNode c3 = node(null, Status.RUNNING);
        final ExecutionTreeNode c4 = node(null);
        assertThat(provider.getChildren(node(null, c1, c2, c3, c4))).containsExactly(c2);
    }

    @Test
    public void afterSwitchingFilteringTwice_childrenAreProvidedNormally() {
        final ExecutionViewContentProvider provider = new ExecutionViewContentProvider();

        final ExecutionTreeNode c1 = node(null, Status.PASS);
        final ExecutionTreeNode c2 = node(null, Status.FAIL);
        final ExecutionTreeNode c3 = node(null, Status.RUNNING);
        final ExecutionTreeNode c4 = node(null);

        assertThat(provider.getChildren(node(null, c1, c2, c3, c4))).containsExactly(c1, c2, c3, c4);
        provider.switchFailedFilter();
        assertThat(provider.getChildren(node(null, c1, c2, c3, c4))).containsExactly(c2);
        provider.switchFailedFilter();
        assertThat(provider.getChildren(node(null, c1, c2, c3, c4))).containsExactly(c1, c2, c3, c4);
    }

    @Test
    public void afterResetingFiltering_childrenAreProvidedNormally() {
        final ExecutionViewContentProvider provider = new ExecutionViewContentProvider();

        final ExecutionTreeNode c1 = node(null, Status.PASS);
        final ExecutionTreeNode c2 = node(null, Status.FAIL);
        final ExecutionTreeNode c3 = node(null, Status.RUNNING);
        final ExecutionTreeNode c4 = node(null);

        assertThat(provider.getChildren(node(null, c1, c2, c3, c4))).containsExactly(c1, c2, c3, c4);
        provider.switchFailedFilter();
        provider.resetFailedFilter();
        assertThat(provider.getChildren(node(null, c1, c2, c3, c4))).containsExactly(c1, c2, c3, c4);
    }

    private ExecutionTreeNode node(final ExecutionTreeNode parent, final ExecutionTreeNode... children) {
        return node(parent, null, children);
    }

    private ExecutionTreeNode node(final ExecutionTreeNode parent, final Status status,
            final ExecutionTreeNode... children) {
        final ExecutionTreeNode node = new ExecutionTreeNode(parent, null, "name");
        node.addChildren(newArrayList(children));
        node.setStatus(status);
        return node;
    }
}
