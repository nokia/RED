/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionStatusStore;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.ClearExecutionHandler.E4ClearExecutionHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ClearExecutionHandler extends DIParameterizedHandler<E4ClearExecutionHandler> {

    public ClearExecutionHandler() {
        super(E4ClearExecutionHandler.class);
    }

    public static class E4ClearExecutionHandler {

        @Execute
        public void clearView(@Named(ISources.ACTIVE_PART_NAME) final ExecutionViewWrapper view) {
            @SuppressWarnings("restriction")
            final ExecutionView executionView = view.getComponent();
            
            executionView.getCurrentlyShownLaunch()
                    .flatMap(launch -> launch.getExecutionData(ExecutionStatusStore.class))
                    .ifPresent(store -> {
                        store.dispose();
                        executionView.clearView();
                    });
        }
    }
}
