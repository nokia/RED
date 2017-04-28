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
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewContentProvider;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.ShowFailedOnlyHandler.E4ShowFailedOnlyHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ShowFailedOnlyHandler extends DIParameterizedHandler<E4ShowFailedOnlyHandler> {

    public ShowFailedOnlyHandler() {
        super(E4ShowFailedOnlyHandler.class);
    }

    public static class E4ShowFailedOnlyHandler {

        @Execute
        public void toggleShowFailedOnly(@Named(ISources.ACTIVE_PART_NAME) final ExecutionViewWrapper view) {
            @SuppressWarnings("restriction")
            final ExecutionView executionView = view.getComponent();
            final TreeViewer viewer = executionView.getViewer();
            final ExecutionViewContentProvider contentProvider = (ExecutionViewContentProvider) viewer
                    .getContentProvider();
            contentProvider.switchFailedFilter();
            viewer.refresh();
        }
    }
}
