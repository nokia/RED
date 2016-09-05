/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.MoveVariableDownCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.MoveVariableDownHandler.E4MoveVariableDownHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class MoveVariableDownHandler extends DIParameterizedHandler<E4MoveVariableDownHandler> {

    public MoveVariableDownHandler() {
        super(E4MoveVariableDownHandler.class);
    }

    public static class E4MoveVariableDownHandler {

        @Execute
        public void moveVariableDown(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RobotEditorCommandsStack stack) {
            final RobotVariable selectedVariable = Selections.getSingleElement(selection, RobotVariable.class);
            stack.execute(new MoveVariableDownCommand(selectedVariable));
        }
    }
}
