/*
 * Copyright 2018 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.code.handler.DeleteInCodeHoldersTableHandler.E4DeleteInCodeHoldersTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CopyInTasksTableHandler.E4CopyInTasksTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.tasks.handler.CutInTasksTableHandler.E4CutInTasksTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutInTasksTableHandler extends DIParameterizedHandler<E4CutInTasksTableHandler> {

    public CutInTasksTableHandler() {
        super(E4CutInTasksTableHandler.class);
    }

    public static class E4CutInTasksTableHandler {

        @Execute
        public void cutCellContent(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection,
                final RobotEditorCommandsStack commandsStack, final RedClipboard clipboard) {

            final boolean copiedToClipboard = new E4CopyInTasksTableHandler().copyContent(editor, selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteInCodeHoldersTableHandler().delete(selection, editor, commandsStack);
            }
        }
    }
}