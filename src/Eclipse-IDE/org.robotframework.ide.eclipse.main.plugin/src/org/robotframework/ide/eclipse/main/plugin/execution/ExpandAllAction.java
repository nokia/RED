/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.execution;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class ExpandAllAction extends Action implements IWorkbenchAction {

    private static final String ID = "org.robotframework.action.executionView.ExpandAllAction";

    private TreeViewer viewer;

    public ExpandAllAction(final TreeViewer viewer) {
        setId(ID);
        this.viewer = viewer;
    }

    @Override
    public void run() {
        viewer.expandAll();
    }

    @Override
    public void dispose() {

    }

}
