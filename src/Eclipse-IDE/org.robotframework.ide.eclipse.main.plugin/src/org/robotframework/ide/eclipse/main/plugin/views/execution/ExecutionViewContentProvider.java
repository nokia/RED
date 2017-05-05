/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.util.List;
import java.util.Optional;

import org.rf.ide.core.execution.Status;
import org.robotframework.red.viewers.TreeContentProvider;

public class ExecutionViewContentProvider extends TreeContentProvider {

    private boolean isFailedFilterEnabled;

    public void setFailedFilter(final boolean isEnabled) {
        this.isFailedFilterEnabled = isEnabled;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return ((List<?>) inputElement).toArray(new Object[0]);
    }

    @Override
    public Object[] getChildren(final Object element) {
        final List<ExecutionTreeNode> children = ((ExecutionTreeNode) element).getChildren();

        if (isFailedFilterEnabled) {
            return children.stream()
                    .filter(child -> child.getStatus().equals(Optional.of(Status.FAIL)))
                    .toArray(ExecutionTreeNode[]::new);
        } else {
            return children.toArray(new ExecutionTreeNode[0]);
        }
    }

    @Override
    public Object getParent(final Object element) {
        return ((ExecutionTreeNode) element).getParent();
    }

    @Override
    public boolean hasChildren(final Object element) {
        return ((ExecutionTreeNode) element).getChildren().size() > 0;
    }
}
