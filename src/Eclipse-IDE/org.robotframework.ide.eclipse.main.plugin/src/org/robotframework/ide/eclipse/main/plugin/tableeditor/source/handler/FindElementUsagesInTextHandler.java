/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ui.ISources;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.DocumentUtilities;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.handler.FindElementUsagesInTextHandler.E4FindUsagesInTextHandler;
import org.robotframework.red.commands.DIParameterizedHandler;

public class FindElementUsagesInTextHandler extends DIParameterizedHandler<E4FindUsagesInTextHandler> {

    public FindElementUsagesInTextHandler() {
        super(E4FindUsagesInTextHandler.class);
    }

    public static class E4FindUsagesInTextHandler {

        @Execute
        public void findUsages(final @Named(ISources.ACTIVE_EDITOR_NAME) RobotFormEditor editor,
                @Named("org.robotframework.ide.eclipse.findElementUsages.place") final String place)
                throws BadLocationException {

            final SourceViewer viewer = editor.getSourceEditor().getViewer();

            final ITextSelection selection = (ITextSelection) viewer.getSelection();

            String selectedText = selection.getText();

            if (selectedText.isEmpty()) {

                final IDocument doc = viewer.getDocument();
                final int offset = selection.getOffset();

                final boolean isTsv = FindUsagesHandler.isTSV(editor);
                final IRegion token = DocumentUtilities.findCellRegion(doc, isTsv, offset).get();

                selectedText = doc.get(token.getOffset(), token.getLength());

            }
            if (!selectedText.isEmpty()) {
                FindUsagesHandler.findElements(place, editor, selectedText);
            }

        }

    }

}
