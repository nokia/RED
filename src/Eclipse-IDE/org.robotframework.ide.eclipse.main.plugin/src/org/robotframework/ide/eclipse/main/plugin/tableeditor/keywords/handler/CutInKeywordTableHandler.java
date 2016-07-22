/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CopyInKeywordTableHandler.E4CopyInKeywordTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CutInKeywordTableHandler.E4CutInKeywordTableHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.DeleteInKeywordTableHandler.E4DeleteInKeywordTableHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutInKeywordTableHandler extends DIParameterizedHandler<E4CutInKeywordTableHandler> {

    public CutInKeywordTableHandler() {
        super(E4CutInKeywordTableHandler.class);
    }

    public static class E4CutInKeywordTableHandler {

        @Execute
        public void cutCellContent(@Named(Selections.SELECTION) final IStructuredSelection selection,
                @Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                final RobotEditorCommandsStack commandsStack, final RedClipboard clipboard) {

            final boolean copiedToClipboard = new E4CopyInKeywordTableHandler().copyContent(editor, selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteInKeywordTableHandler().delete(selection, editor, commandsStack);
            }
        }
    }
}
