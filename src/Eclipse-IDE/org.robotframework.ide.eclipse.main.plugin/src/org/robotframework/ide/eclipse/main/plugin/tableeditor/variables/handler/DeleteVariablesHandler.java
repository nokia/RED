/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.variables.RemoveVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.DeleteVariablesHandler.E4DeleteVariableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteVariablesHandler extends DIParameterizedHandler<E4DeleteVariableHandler> {

    public DeleteVariablesHandler() {
        super(E4DeleteVariableHandler.class);
    }

    public static class E4DeleteVariableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public void deleteVariables(@Named(Selections.SELECTION) final IStructuredSelection selection) {
            final List<RobotVariable> variables = Selections.getElements(selection, RobotVariable.class);
            commandsStack.execute(new RemoveVariableCommand(variables));
        }
    }
}
