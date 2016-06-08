/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.SelectionLayerAccessor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CopyInVariableTableHandler.E4CopyInVariableTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.variables.handler.CopyVariablesHandler.E4CopyVariablesHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CopyInVariableTableHandler extends DIParameterizedHandler<E4CopyInVariableTableHandler> {

    public CopyInVariableTableHandler() {
        super(E4CopyInVariableTableHandler.class);
    }

    public static class E4CopyInVariableTableHandler {

        @Execute
        public Object copy(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final Clipboard clipboard) {
            final SelectionLayerAccessor selectionLayerAccessor = editor.getSelectionLayerAccessor();

            if (selectionLayerAccessor.onlyFullRowsAreSelected()) {
                final E4CopyVariablesHandler copyHandler = new E4CopyVariablesHandler();
                copyHandler.copyVariables(selection, clipboard);
            } else {

            }

            return null;
        }
    }
}
