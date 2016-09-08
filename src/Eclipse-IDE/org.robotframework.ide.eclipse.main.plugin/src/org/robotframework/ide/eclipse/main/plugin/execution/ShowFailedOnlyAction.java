/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.execution;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.robotframework.ide.eclipse.main.plugin.RedImages;

public class ShowFailedOnlyAction extends Action implements IWorkbenchAction {

    private static final String ID = "org.robotframework.action.executionView.ShowFailedOnlyAction";

    private final TreeViewer viewer;
    
    private final ExecutionViewContentProvider executionViewContentProvider;
    
    public ShowFailedOnlyAction(final TreeViewer viewer, final ExecutionViewContentProvider executionViewContentProvider) {
        super("Show Failures Only", RedImages.getFailuresImage());
        setId(ID);
        setChecked(false);

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
