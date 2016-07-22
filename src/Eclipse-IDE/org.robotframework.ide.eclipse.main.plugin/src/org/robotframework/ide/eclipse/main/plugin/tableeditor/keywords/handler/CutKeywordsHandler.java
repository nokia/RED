/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotEditorCommandsStack;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.dnd.RedClipboard;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CopyKeywordsHandler.E4CopyKeywordsHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.CutKeywordsHandler.E4CutKeywordsHandler;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.keywords.handler.DeleteKeywordsHandler.E4DeleteKeywordsHandler;
import org.robotframework.red.commands.DIParameterizedHandler;
import org.robotframework.red.viewers.Selections;

public class CutKeywordsHandler extends DIParameterizedHandler<E4CutKeywordsHandler> {

    public CutKeywordsHandler() {
        super(E4CutKeywordsHandler.class);
    }

    public static class E4CutKeywordsHandler {

        @Inject
        private RobotEditorCommandsStack commandsStack;

        @Execute
        public void cutKeywords(@Named(ISources.ACTIVE_EDITOR_NAME) final RobotFormEditor editor,
                @Named(Selections.SELECTION) final IStructuredSelection selection, final RedClipboard clipboard) {

            final boolean copiedToClipboard = new E4CopyKeywordsHandler().copyKeywords(selection, clipboard);
            if (copiedToClipboard) {
                new E4DeleteKeywordsHandler().deleteKeywords(editor, commandsStack, selection);
            }
        }
    }
}
