/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.views.debugshell.ClearDebugLogViewHandler.E4ClearDebugLogViewHandler;
import org.robotframework.red.commands.DIParameterizedHandler;


public class ClearDebugLogViewHandler extends DIParameterizedHandler<E4ClearDebugLogViewHandler> {

    public ClearDebugLogViewHandler() {
        super(E4ClearDebugLogViewHandler.class);
    }

    public static class E4ClearDebugLogViewHandler {

        @Execute
        public void clear(final @Named(ISources.ACTIVE_PART_NAME) DebugShellViewWrapper view) {
            view.getView().clear();
        }
    }
}
