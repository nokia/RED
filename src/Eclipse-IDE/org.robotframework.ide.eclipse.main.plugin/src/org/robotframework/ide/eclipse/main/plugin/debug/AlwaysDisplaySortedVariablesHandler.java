/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.debug;

import java.util.Map;

import javax.inject.Named;

import org.eclipse.debug.ui.IDebugView;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.commands.PersistentState;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.robotframework.ide.eclipse.main.plugin.debug.AlwaysDisplaySortedVariablesHandler.E4AlwaysDisplayVariablesSortedHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class AlwaysDisplaySortedVariablesHandler extends DIParameterizedHandler<E4AlwaysDisplayVariablesSortedHandler>
        implements IElementUpdater {

    public static final String COMMAND_ID = "org.robotframework.red.view.debug.variables.sort";
    public static final String COMMAND_STATE_ID = COMMAND_ID + ".state";

    public static boolean isSortingEnabled() {
        final PersistentState state = provideSortingState();
        state.setShouldPersist(true);
        return (boolean) state.getValue();
    }

    private static PersistentState provideSortingState() {
        final ICommandService commandService = PlatformUI.getWorkbench().getService(ICommandService.class);
        return (PersistentState) commandService.getCommand(COMMAND_ID).getState(COMMAND_STATE_ID);
    }

    public AlwaysDisplaySortedVariablesHandler() {
        super(E4AlwaysDisplayVariablesSortedHandler.class);
    }

    @Override
    public void updateElement(final UIElement element, @SuppressWarnings("rawtypes") final Map parameters) {
        final PersistentState state = provideSortingState();
        state.setShouldPersist(true);
        element.setChecked((boolean) state.getValue());
    }

    public static class E4AlwaysDisplayVariablesSortedHandler {

        @Execute
        public void displayVariablesSorted(final @Named(COMMAND_STATE_ID) PersistentState commandState,
                @Named(ISources.ACTIVE_PART_NAME) final IDebugView variablesView) {

            commandState.setShouldPersist(true);
            final boolean currentValue = (boolean) commandState.getValue();
            commandState.setValue(!currentValue);


            final TreeViewer viewer = (TreeViewer) variablesView.getViewer();
            try {
                viewer.getTree().setRedraw(false);
                viewer.setInput(viewer.getInput());
            } finally {
                viewer.getTree().setRedraw(true);
            }
        }
    }
}
