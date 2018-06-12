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
import org.robotframework.ide.eclipse.main.plugin.model.cmd.InsertCellCommand;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.handler.InsertCellHandler.E4InsertCellHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class InsertCellHandler extends DIParameterizedHandler<E4InsertCellHandler> {

    public InsertCellHandler() {
        super(E4InsertCellHandler.class);
    }

    public static class E4InsertCellHandler {

        @Execute
        public void insertCell(final RobotEditorCommandsStack commandsStack,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection) {

            final RobotKeywordCall call = (RobotKeywordCall) selection.getFirstElement();
            final int column = editor.getSelectionLayerAccessor().getSelectedPositions()[0].getColumnPosition();

            final boolean isWholeLineComment = call.getAction().isPresent()
                    && call.getAction().get().getText().isEmpty() && call.getAction().get().getFilePosition().isNotSet()
                    && call.getArgumentTokens().isEmpty();

            if (column < call.getLinkedElement().getElementTokens().size() - (isWholeLineComment ? 1 : 0)) {
                commandsStack.execute(new InsertCellCommand(call, column, null));
            }
        }
    }
}
