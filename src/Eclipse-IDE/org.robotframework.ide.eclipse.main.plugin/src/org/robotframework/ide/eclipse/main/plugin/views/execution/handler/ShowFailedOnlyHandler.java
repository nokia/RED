/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.execution.handler;

import java.util.Map;

import javax.inject.Named;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionView;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewContentProvider;
import org.robotframework.ide.eclipse.main.plugin.views.execution.ExecutionViewWrapper;
import org.robotframework.ide.eclipse.main.plugin.views.execution.handler.ShowFailedOnlyHandler.E4ShowFailedOnlyHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ShowFailedOnlyHandler extends DIParameterizedHandler<E4ShowFailedOnlyHandler> implements IElementUpdater {

    private static final String COMMAND_ID = "org.robotframework.red.view.execution.showFailedOnly";
    private static final String COMMAND_STATE_ID = COMMAND_ID + ".state";

    public static void setCommandState(final boolean value) {
        final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        final Command command = commandService.getCommand(COMMAND_ID);
        command.getState(COMMAND_STATE_ID).setValue(value);
        commandService.refreshElements(COMMAND_ID, null);
    }

    public ShowFailedOnlyHandler() {
        super(E4ShowFailedOnlyHandler.class);
    }

    @Override
    public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
        final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        final Command command = commandService.getCommand(COMMAND_ID);
        element.setChecked((boolean) command.getState(COMMAND_STATE_ID).getValue());
    }

    public static class E4ShowFailedOnlyHandler {

        @Execute
        public void toggleShowFailedOnly(
                final @Named(COMMAND_STATE_ID) State commandState,
                @Named(ISources.ACTIVE_PART_NAME) final ExecutionViewWrapper view) {
            @SuppressWarnings("restriction")
            final ExecutionView executionView = view.getComponent();
            final TreeViewer viewer = executionView.getViewer();
            final ExecutionViewContentProvider contentProvider = (ExecutionViewContentProvider) viewer
                    .getContentProvider();

            final boolean currentValue = (boolean) commandState.getValue();

            contentProvider.setFailedFilter(!currentValue);
            commandState.setValue(!currentValue);
            viewer.refresh();
        }
    }
}
