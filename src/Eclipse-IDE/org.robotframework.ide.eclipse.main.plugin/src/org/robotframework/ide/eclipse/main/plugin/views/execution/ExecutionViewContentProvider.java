/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution;

import java.util.List;

import org.rf.ide.core.execution.Status;
import org.robotframework.red.viewers.TreeContentProvider;

class ExecutionViewContentProvider extends TreeContentProvider {

    private boolean isFailedFilterEnabled;

    void setFailedFilterEnabled(final boolean isFailedFilterEnabled) {
        this.isFailedFilterEnabled = isFailedFilterEnabled;
    }

    void switchFailedFilter() {
        isFailedFilterEnabled = !isFailedFilterEnabled;
    }

    @Override
    public Object[] getElements(final Object inputElement) {
        return (ExecutionStatus[]) inputElement;
    }

    @Override
    public Object[] getChildren(final Object element) {
        if (element instanceof ExecutionStatus) {
            final List<ExecutionStatus> children = ((ExecutionStatus) element).getChildren();

            if (isFailedFilterEnabled) {
                return children.stream()
                        .filter(status -> status.getStatus() == Status.FAIL)
                        .toArray(ExecutionStatus[]::new);
            }
            return children.toArray(new ExecutionStatus[0]);
        }
        return null;
    }

    @Override
    public Object getParent(final Object element) {
        if (element instanceof ExecutionStatus) {
            return ((ExecutionStatus) element).getParent();
        }
        return null;
    }

    @Override
    public boolean hasChildren(final Object element) {
        if (element instanceof ExecutionStatus) {
            return ((ExecutionStatus) element).getChildren().size() > 0;
        }
        return false;
    }
}
