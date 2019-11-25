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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.SwitchShellModeHandler.E4SwitchShellModeHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class SwitchShellModeHandler extends DIParameterizedHandler<E4SwitchShellModeHandler> implements IElementUpdater{

    public static final String COMMAND_ID = "org.robotframework.red.view.debug.switchShellMode";

    public static final String COMMAND_MODE_PARAMETER = COMMAND_ID + ".mode";
    public static final String COMMAND_STATE_ID = COMMAND_ID + ".state";

    static void setMode(final ExpressionType mode) {
        final PersistentState state = provideState();
        state.setShouldPersist(true);
        state.setValue(mode.name());
    }

    private static PersistentState provideState() {
        final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        return (PersistentState) commandService.getCommand(COMMAND_ID).getState(COMMAND_STATE_ID);
    }


    public SwitchShellModeHandler() {
        super(E4SwitchShellModeHandler.class);
    }
    
    @Override
    public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
        final PersistentState state = provideState();
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
