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
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CutInVariableTableHandler.E4CutInVariableTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CutVariablesHandler.E4CutVariablesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.DeleteInVariableTableHandler.E4DeleteInVariableTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutInVariableTableHandler extends DIParameterizedHandler<E4CutInVariableTableHandler> {

    public CutInVariableTableHandler() {
        super(E4CutInVariableTableHandler.class);
    }

    public static class E4CutInVariableTableHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public Object cut(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();

            if (selectionLayerAccessor.onlyFullRowsAreSelected()) {
                final E4CutVariablesHandler cutHandler = new E4CutVariablesHandler();
                cutHandler.cutVariables(commandsStack, selection, clipboard);

                final E4DeleteInVariableTableHandler deleteHandler = new E4DeleteInVariableTableHandler();
                deleteHandler.delete(commandsStack, editor, selection);
            } else {

            }

            return null;
        }
    }
}
