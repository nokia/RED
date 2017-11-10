/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.cmd.DeleteCellCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.DeleteCellHandler.E4DeleteCellHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class DeleteCellHandler extends DIParameterizedHandler<E4DeleteCellHandler> {

    public DeleteCellHandler() {
        super(E4DeleteCellHandler.class);
    }

    public static class E4DeleteCellHandler {

        @Execute
        public void deleteCell(final RobotEditorCommandsStack commandsStack,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final RobotKeywordCall call = (RobotKeywordCall) selection.getFirstElement();
            final int column = editor.getSelectionLayerAccessor().getSelectedPositions()[0].getColumnPosition();

            if (column < call.getLinkedElement().getElementTokens().size()) {
                commandsStack.execute(new DeleteCellCommand(call, column));
            }
        }
    }
}
