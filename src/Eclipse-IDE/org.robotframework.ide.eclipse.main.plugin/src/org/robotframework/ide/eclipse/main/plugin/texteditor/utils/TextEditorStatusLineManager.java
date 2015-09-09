/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see licence.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * @author mmarzec
 *
 */
public class TextEditorStatusLineManager {

    private StatusLineContributionItem statusLineItem;

    private IDocument document;

    public TextEditorStatusLineManager(final IEditorPart editorPart, final IDocument document) {

        this.document = document;
        initStatusLineItem(editorPart);
    }

    public void updatePosition(final int currentCaretOffset) {
        try {
            final int lineNumber = document.getLineOfOffset(currentCaretOffset);
            final int columnNumber = currentCaretOffset - document.getLineOffset(lineNumber);
            statusLineItem.setText((lineNumber + 1) + ":" + (columnNumber + 1));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void initStatusLineItem(final IEditorPart editorPart) {
        final IStatusLineManager statusLineManager = editorPart.getEditorSite().getActionBars().getStatusLineManager();
        if (statusLineManager != null) {
            statusLineItem = (StatusLineContributionItem) statusLineManager.find(ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
            if (statusLineItem == null) {
                statusLineItem = new StatusLineContributionItem(
                        ITextEditorActionConstants.STATUS_CATEGORY_INPUT_POSITION);
                statusLineManager.add(statusLineItem);
            }
        }
    }
    
}
