/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.RedoHandler.E4RedoHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class RedoHandler extends DIParameterizedHandler<E4RedoHandler> {

    public RedoHandler() {
        super(E4RedoHandler.class);
    }

    public static class E4RedoHandler {

        @Execute
        public void redo(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack) {

            // TODO: implement redo action
        }
    }
}
