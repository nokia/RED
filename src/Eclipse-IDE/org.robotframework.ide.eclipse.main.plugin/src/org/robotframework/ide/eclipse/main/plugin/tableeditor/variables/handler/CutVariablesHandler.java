/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteVariableCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.VariablesTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CutVariablesHandler.E4CutVariablesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutVariablesHandler extends DIParameterizedHandler<E4CutVariablesHandler> {

    public CutVariablesHandler() {
        super(E4CutVariablesHandler.class);
    }

    public static class E4CutVariablesHandler {

        @Execute
        public Object cutVariables(final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {

            final List<RobotVariable> variables = Selections.getElements(selection, RobotVariable.class);
            clipboard.setContents(new RobotVariable[][] { variables.toArray(new RobotVariable[variables.size()]) },
                    new Transfer[] { VariablesTransfer.getInstance() });
            commandsStack.execute(new DeleteVariableCommand(variables));

            return null;
        }
    }
}
