/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.ExpandAllHandler.E4ExpandAllHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ExpandAllHandler extends DIParameterizedHandler<E4ExpandAllHandler> {

    public ExpandAllHandler() {
        super(E4ExpandAllHandler.class);
    }

    public static class E4ExpandAllHandler {

        @Execute
        public void expandAll(@Named(ISources.ACTIVE_PART_NAME) final ExecutionViewWrapper view) {
            @SuppressWarnings("restriction")
            final ExecutionView executionView = view.getComponent();
            final TreeViewer viewer = executionView.getViewer();
            viewer.expandAll();
        }
    }
}
