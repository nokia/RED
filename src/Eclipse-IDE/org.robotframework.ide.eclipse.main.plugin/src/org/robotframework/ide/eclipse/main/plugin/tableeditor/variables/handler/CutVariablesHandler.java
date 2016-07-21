/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CopyVariablesHandler.E4CopyVariablesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CutVariablesHandler.E4CutVariablesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.DeleteVariablesHandler.E4DeleteVariableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutVariablesHandler extends DIParameterizedHandler<E4CutVariablesHandler> {

    public CutVariablesHandler() {
        super(E4CutVariablesHandler.class);
    }

    public static class E4CutVariablesHandler {

        @Execute
        public void cutVariables(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final boolean copiedToClipboard = new E4CopyVariablesHandler().copyVariables(selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteVariableHandler().deleteVariables(editor, commandsStack, selection);
            }
        }
    }
}
