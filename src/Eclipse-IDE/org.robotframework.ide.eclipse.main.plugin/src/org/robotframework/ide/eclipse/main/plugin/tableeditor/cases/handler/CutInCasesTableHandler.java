/*
* Copyright 2016 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyInCasesTableHandler.E4CopyInCasesTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CutInCasesTableHandler.E4CutInCasesTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.DeleteInCasesTableHandler.E4DeleteInCasesTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutInCasesTableHandler extends DIParameterizedHandler<E4CutInCasesTableHandler> {

    public CutInCasesTableHandler() {
        super(E4CutInCasesTableHandler.class);
    }

    public static class E4CutInCasesTableHandler {

        @Execute
        public void cutCellContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack, final RedClipboard clipboard) {

            final boolean copiedToClipboard = new E4CopyInCasesTableHandler().copyContent(editor, selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteInCasesTableHandler().delete(selection, editor, commandsStack);
            }
        }
    }
}