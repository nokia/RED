/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.jface.assist;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;

public class RedComboContentAdapter implements RedControlContentAdapter {


    @Override
    public String getControlContents(final Control control) {
        return ((Combo) control).getText();
    }

    @Override
    public void setControlContents(final Control control, final RedContentProposal proposal) {
        final String content = proposal.getContent();
        final int cursorPosition = proposal.getCursorPosition();

        ((Combo) control).setText(content);
        ((Combo) control).setSelection(new Point(cursorPosition, cursorPosition));
    }

    @Override
    public void insertControlContents(final Control control, final RedContentProposal proposal) {
        proposal.getModificationStrategy().insert((Combo) control, proposal);
    }

    @Override
    public int getCursorPosition(final Control control) {
        return ((Combo) control).getCaretPosition();
    }

    @Override
    public Rectangle getInsertionBounds(final Control control) {
        final Combo combo = (Combo) control;
        final Point caretOrigin = combo.getCaretLocation();
        // We fudge the y pixels due to problems with getCaretLocation
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=52520
        return new Rectangle(caretOrigin.x + combo.getClientArea().x, caretOrigin.y + combo.getClientArea().y + 3, 1,
                combo.getTextHeight());
    }

    @Override
    public void setCursorPosition(final Control control, final int position) {
        ((Combo) control).setSelection(new Point(position, position));
    }

    @Override
    public Point getSelection(final Control control) {
        return ((Combo) control).getSelection();
    }

    @Override
    public void setSelection(final Control control, final Point range) {
        ((Combo) control).setSelection(range);
    }
}
