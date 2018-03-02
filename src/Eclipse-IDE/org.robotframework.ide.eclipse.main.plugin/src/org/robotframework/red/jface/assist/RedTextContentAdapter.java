/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.assist;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.robotframework.red.jface.assist.RedContentProposal.ModificationStrategy;

public class RedTextContentAdapter implements RedControlContentAdapter {

    @Override
    public String getControlContents(final Control control) {
        return ((Text) control).getText();
    }

    @Override
    public void setControlContents(final Control control, final RedContentProposal proposal) {
        final String content = proposal.getContent();
        final int cursorPosition = proposal.getCursorPosition();

        ((Text) control).setText(content);
        ((Text) control).setSelection(cursorPosition, cursorPosition);
    }

    @Override
    public void insertControlContents(final Control control, final RedContentProposal proposal) {
        proposal.getModificationStrategy().insert((Text) control, proposal);
    }

    @Override
    public int getCursorPosition(final Control control) {
        return ((Text) control).getCaretPosition();
    }

    @Override
    public Rectangle getInsertionBounds(final Control control) {
        final Text text = (Text) control;
        final Point caretOrigin = text.getCaretLocation();
        // We fudge the y pixels due to problems with getCaretLocation
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=52520
        return new Rectangle(caretOrigin.x + text.getClientArea().x, caretOrigin.y + text.getClientArea().y + 3, 1,
                text.getLineHeight());
    }

    @Override
    public void setCursorPosition(final Control control, final int position) {
        ((Text) control).setSelection(new Point(position, position));
    }

    @Override
    public Point getSelection(final Control control) {
        return ((Text) control).getSelection();
    }

    @Override
    public void setSelection(final Control control, final Point range) {
        ((Text) control).setSelection(range);
    }

    public abstract static class SubstituteTextModificationStrategy implements ModificationStrategy {

        @Override
        public void insert(final Text text, final IContentProposal proposal) {
            final String content = proposal.getContent();
            final int cursorPosition = proposal.getCursorPosition();

            final Point selection = text.getSelection();
            text.setText(content);
            text.setSelection(cursorPosition);
            // Insert will leave the cursor at the end of the inserted text. If this
            // is not what we wanted, reset the selection.
            if (cursorPosition < content.length()) {
                text.setSelection(selection.x + cursorPosition, selection.x + cursorPosition);
            }
        }

        @Override
        public void insert(final Combo combo, final IContentProposal proposal) {
            final String content = proposal.getContent();
            final int cursorPosition = proposal.getCursorPosition();

            final Point selection = combo.getSelection();
            combo.setText(content);
            combo.setSelection(new Point(cursorPosition, cursorPosition));
            // Insert will leave the cursor at the end of the inserted text. If this
            // is not what we wanted, reset the selection.
            if (cursorPosition < content.length()) {
                combo.setSelection(new Point(selection.x + cursorPosition, selection.x + cursorPosition));
            }
        }
    }
}
