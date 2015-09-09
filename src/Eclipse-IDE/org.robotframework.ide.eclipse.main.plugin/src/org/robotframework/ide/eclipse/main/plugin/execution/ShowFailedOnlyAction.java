/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.execution;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class ShowFailedOnlyAction extends Action implements IWorkbenchAction {

    private static final String ID = "org.robotframework.action.executionView.ShowFailedOnlyAction";

    private TreeViewer viewer;
    
    private ExecutionViewContentProvider executionViewContentProvider;
    
    public ShowFailedOnlyAction(final TreeViewer viewer, final ExecutionViewContentProvider executionViewContentProvider) {
        setId(ID);
        this.viewer = viewer;
        this.executionViewContentProvider = executionViewContentProvider;
    }
    
    @Override
    public void run() {
        executionViewContentProvider.switchFailedFilter();
        viewer.refresh();
    }

    @Override
    public void dispose() {

    }

}
