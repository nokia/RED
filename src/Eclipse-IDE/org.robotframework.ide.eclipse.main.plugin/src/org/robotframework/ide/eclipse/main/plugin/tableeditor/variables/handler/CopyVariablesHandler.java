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
import org.robotframework.ide.eclipse.main.plugin.model.RobotVariable;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.TableHandlersSupport;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CopyVariablesHandler.E4CopyVariablesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyVariablesHandler extends DIParameterizedHandler<E4CopyVariablesHandler> {

    public CopyVariablesHandler() {
        super(E4CopyVariablesHandler.class);
    }

    public static class E4CopyVariablesHandler {

        @Execute
        public void copyVariables(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {

            final List<RobotVariable> variables = Selections.getElements(selection, RobotVariable.class);
            if (!variables.isEmpty()) {
                final Object variablesCopy = TableHandlersSupport.createVariablesCopy(variables);

                clipboard.insertContent(variablesCopy);
            }
        }
    }
}
