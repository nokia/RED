/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CopyCasesHandler.E4CopyCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.CutCasesHandler.E4CutCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.cases.handler.DeleteCasesHandler.E4DeleteCasesHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutCasesHandler extends DIParameterizedHandler<E4CutCasesHandler> {

    public CutCasesHandler() {
        super(E4CutCasesHandler.class);
    }

    public static class E4CutCasesHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public void cutKeywords(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection,
                final RedClipboard clipboard) {
            
            final boolean copiedToClipboard = new E4CopyCasesHandler().copyCases(selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteCasesHandler().deleteCasesAndCalls(editor, commandsStack, selection);
            }
        }
    }
}
