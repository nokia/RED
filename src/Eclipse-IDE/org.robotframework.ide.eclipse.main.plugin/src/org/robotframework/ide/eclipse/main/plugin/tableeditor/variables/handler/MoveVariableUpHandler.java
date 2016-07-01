/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.MoveVariableUpCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.MoveVariableUpHandler.E4MoveVariableUpHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class MoveVariableUpHandler extends DIParameterizedHandler<E4MoveVariableUpHandler> {

    public MoveVariableUpHandler() {
        super(E4MoveVariableUpHandler.class);
    }

    public static class E4MoveVariableUpHandler {

        @Inject
        private RobotEditorCommandsStack stack;

        @Execute
        public void moveVariableUp(@Named(Selections.SELECTION) final IStructuredSelection selection) {

            final RobotVariable selectedVariable = Selections.getSingleElement(selection, RobotVariable.class);
            stack.execute(new MoveVariableUpCommand(selectedVariable));
        }
    }
}
