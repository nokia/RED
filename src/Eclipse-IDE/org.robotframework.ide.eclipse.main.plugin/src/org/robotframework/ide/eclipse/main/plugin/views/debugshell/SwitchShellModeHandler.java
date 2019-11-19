/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.SwitchShellModeHandler.E4SwitchShellModeHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class SwitchShellModeHandler extends DIParameterizedHandler<E4SwitchShellModeHandler> {

    public SwitchShellModeHandler() {
        super(E4SwitchShellModeHandler.class);
    }

    public static class E4SwitchShellModeHandler {

        @Execute
        public void switchMode(final @Named(ISources.ACTIVE_PART_NAME) DebugShellViewWrapper view) {
            view.getView().switchToNextMode();
        }
    }
}
