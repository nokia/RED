/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.texteditor.utils;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

public class TextEditorIndentLineAutoEditStrategy implements IAutoEditStrategy {

    private boolean isShiftPresssed;

    @Override
    public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
        if (c.length == 0 && c.text != null && TextUtilities.endsWith(d.getLegalLineDelimiters(), c.text) != -1)
            autoIndentAfterNewLine(d, c);
        else if (c.length > 0 && c.text != null && c.text.equals("\t")) {
            if (!isShiftPresssed)
                addIndentToSelectedText(d, c);
            else
                removeIndentFromSelectedText(d, c);
        }
    }

    private void addIndentToSelectedText(IDocument d, DocumentCommand c) {
        if (c.offset == -1 || d.getLength() == 0)
            return;

        try {
            StringBuffer buf = new StringBuffer(c.text);
            buf.append(d.get(c.offset, c.length));
            int newLineSearchStart = 0;
            int newLineIndex = 0;
            while ((newLineIndex = buf.indexOf("\n", newLineSearchStart)) > -1) {
                buf.insert(newLineIndex + 1, '\t');
                newLineSearchStart = newLineIndex + 1;
            }

            c.text = buf.toString();

        } catch (BadLocationException e) {
            // stop work
        }
    }

    private void removeIndentFromSelectedText(IDocument d, DocumentCommand c) {
        if (c.offset == -1 || d.getLength() == 0)
            return;

        try {
            StringBuffer buf = new StringBuffer();
            buf.append(d.get(c.offset, c.length));

            if (buf.charAt(0) == '\t') {
                buf.deleteCharAt(0);
                int newLineSearchStart = 0;
                int newLineIndex = 0;
                while ((newLineIndex = buf.indexOf("\n", newLineSearchStart)) > -1) {
                    if (buf.charAt(newLineIndex + 1) == '\t') {
                        buf.deleteCharAt(newLineIndex + 1);
                    }
                    newLineSearchStart = newLineIndex + 1;
                }
                c.text = buf.substring(0, buf.length());
            } else if (d.get(c.offset - 1, 1).equals("\t")) {
                d.replace(c.offset - 1, 1, "");
                c.offset--;

                int newLineSearchStart = 0;
                int newLineIndex = 0;
                while ((newLineIndex = buf.indexOf("\n", newLineSearchStart)) > -1) {
                    if (buf.charAt(newLineIndex + 1) == '\t') {
                        buf.deleteCharAt(newLineIndex + 1);
                    }
                    newLineSearchStart = newLineIndex + 1;
                }
                c.text = buf.substring(0, buf.length());
            }

        } catch (BadLocationException e) {
            // stop work
        }
    }

    private void autoIndentAfterNewLine(IDocument d, DocumentCommand c) {

        if (c.offset == -1 || d.getLength() == 0)
            return;

        try {
            // find start of line
            int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);
            IRegion info = d.getLineInformationOfOffset(p);
            int start = info.getOffset();

            // find white spaces
            int end = findEndOfWhiteSpace(d, start, c.offset);

            StringBuffer buf = new StringBuffer(c.text);
            if (end > start) {
                // append to input
                buf.append(d.get(start, end - start));
            }

            c.text = buf.toString();

        } catch (BadLocationException excp) {
            // stop work
        }
    }

    protected int findEndOfWhiteSpace(IDocument document, int offset, int end) throws BadLocationException {
        while (offset < end) {
            char c = document.getChar(offset);
            if (c != ' ' && c != '\t') {
                return offset;
            }
            offset++;
        }
        return end;
    }

    public boolean isShiftPresssed() {
        return isShiftPresssed;
    }

    public void setShiftPresssed(boolean isShiftPresssed) {
        this.isShiftPresssed = isShiftPresssed;
    }

}
