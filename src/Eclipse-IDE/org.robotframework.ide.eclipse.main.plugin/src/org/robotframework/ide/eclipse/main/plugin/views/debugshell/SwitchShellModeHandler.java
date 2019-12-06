/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import java.util.Map;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.SwitchShellModeHandler.E4SwitchShellModeHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class SwitchShellModeHandler extends DIParameterizedHandler<E4SwitchShellModeHandler> implements IElementUpdater{

    public static final String COMMAND_ID = "org.robotframework.red.view.debug.switchShellMode";
    public static final String COMMAND_MODE_PARAMETER = COMMAND_ID + ".mode";
    public static final String COMMAND_STATE_ID = COMMAND_ID + ".state";

    static void setMode(final IServiceLocator serviceLocator, final ExpressionType mode) {
        final PersistentState state = provideState(serviceLocator);
        if (state != null) {
            state.setShouldPersist(true);
            state.setValue(mode.name());
        }
        serviceLocator.getService(ICommandService.class).refreshElements(COMMAND_ID, null);
    }

    private static PersistentState provideState(final IServiceLocator serviceLocator) {
        final ICommandService commandService = serviceLocator.getService(ICommandService.class);
        return (PersistentState) java.util.Optional.ofNullable(commandService)
                .map(s -> s.getCommand(COMMAND_ID))
                .map(c -> c.getState(COMMAND_STATE_ID))
                .orElse(null);
    }


    public SwitchShellModeHandler() {
        super(E4SwitchShellModeHandler.class);
    }
    
    @Override
    public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
        final IWorkbenchWindow window = (IWorkbenchWindow) parameters.get(IWorkbenchWindow.class.getName());
        final PersistentState state = provideState(window);
        state.setShouldPersist(true);

        element.setChecked(state.getValue().equals(parameters.get(COMMAND_MODE_PARAMETER)));
    }

    public static class E4SwitchShellModeHandler {

        @Execute
        public void switchMode(final ICommandService commandService,
                final @Named(ISources.ACTIVE_PART_NAME) DebugShellViewWrapper view,
                @Optional @Named(COMMAND_MODE_PARAMETER) final String mode,
                final @Named(COMMAND_STATE_ID) PersistentState commandState) {

            final ExpressionType newMode = mode == null ? null : ExpressionType.valueOf(mode);
            if (newMode != ExpressionType.valueOf((String) commandState.getValue())) {
                final ExpressionType finalMode = view.getView().switchToMode(newMode);
                commandState.setValue(finalMode.name());

                commandService.refreshElements(COMMAND_ID, null);
            }
        }
    }
}
