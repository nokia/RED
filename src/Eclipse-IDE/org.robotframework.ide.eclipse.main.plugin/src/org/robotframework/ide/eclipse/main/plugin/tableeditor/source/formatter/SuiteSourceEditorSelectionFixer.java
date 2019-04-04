/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;

public class SuiteSourceEditorSelectionFixer {

    private final ISelectionProvider selectionProvider;

    private int startLine;

    private int endLine;

    private int startOffsetInLine;

    private int endOffsetInLine;

    public SuiteSourceEditorSelectionFixer(final ISelectionProvider selectionProvider) {
        this.selectionProvider = selectionProvider;
    }

    public void saveSelection(final IDocument oldDocument) throws BadLocationException {
        final ITextSelection selection = (ITextSelection) selectionProvider.getSelection();
        startLine = selection.getStartLine();
        endLine = selection.getEndLine();
        startOffsetInLine = selection.getOffset() - oldDocument.getLineOffset(startLine);
        endOffsetInLine = selection.getOffset() + selection.getLength() - oldDocument.getLineOffset(endLine);
    }

    public void fixSelection(final IDocument newDocument) {
        final int numberOfLines = newDocument.getNumberOfLines();
        final int startLine = fixLine(this.startLine, numberOfLines);
        final int endLine = fixLine(this.endLine, numberOfLines);
        final int startOffsetInLine = fixOffset(newDocument, this.startOffsetInLine, startLine);
        final int endOffsetInLine = fixOffset(newDocument, this.endOffsetInLine, endLine);

        selectionProvider
                .setSelection(new TextSelection(newDocument, startOffsetInLine, endOffsetInLine - startOffsetInLine));
    }

    private int fixLine(final int line, final int numberOfLines) {
        return line > numberOfLines - 1 ? numberOfLines - 1 : line;
    }

    private int fixOffset(final IDocument document, final int offset, final int line) {
        final int minOffset = getOffset(document, line);
        final int maxOffset = minOffset + getLength(document, line) - getDelimiterLength(document, line);
        return minOffset + offset > maxOffset ? maxOffset : minOffset + offset;
    }

    private int getOffset(final IDocument document, final int line) {
        try {
            return document.getLineOffset(line);
        } catch (final BadLocationException e) {
            return 0;
        }
    }

    private int getLength(final IDocument document, final int line) {
        try {
            return document.getLineLength(line);
        } catch (final BadLocationException e) {
            return 0;
        }
    }

    private int getDelimiterLength(final IDocument document, final int line) {
        try {
            final String delimiter = document.getLineDelimiter(line);
            return delimiter == null ? 0 : delimiter.length();
        } catch (final BadLocationException e) {
            return 0;
        }
    }

}
