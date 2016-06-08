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
import org.eclipse.swt.dnd.Clipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.VariablesTransfer;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.PasteInVariableTableHandler.E4PasteInVariableTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.PasteVariablesHandler.E4PasteVariablesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class PasteInVariableTableHandler extends DIParameterizedHandler<E4PasteInVariableTableHandler> {

    public PasteInVariableTableHandler() {
        super(E4PasteInVariableTableHandler.class);
    }

    public static class E4PasteInVariableTableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object paste(@Named(Selections.SELECTION) final IStructuredSelection selection,
                final Clipboard clipboard) {

            if (VariablesTransfer.hasVariables(clipboard)) {
                final E4PasteVariablesHandler pasteHandler = new E4PasteVariablesHandler();
                pasteHandler.pasteVariables(commandsStack, selection, clipboard);
            } else {

            }

            return null;
        }
    }
}
