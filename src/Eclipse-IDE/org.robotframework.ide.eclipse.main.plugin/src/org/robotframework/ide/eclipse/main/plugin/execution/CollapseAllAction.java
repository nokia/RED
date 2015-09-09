/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.execution;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class CollapseAllAction extends Action implements IWorkbenchAction {

    private static final String ID = "org.robotframework.action.executionView.CollapseAllAction";

    private TreeViewer viewer;

    public CollapseAllAction(final TreeViewer viewer) {
        setId(ID);
        this.viewer = viewer;
    }

    @Override
    public void run() {
        viewer.collapseAll();
    }

    @Override
    public void dispose() {

    }

}
