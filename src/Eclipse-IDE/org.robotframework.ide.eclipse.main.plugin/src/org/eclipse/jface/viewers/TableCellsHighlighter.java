/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.eclipse.jface.viewers;

import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerRow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.robotframework.ide.eclipse.main.plugin.RedTheme;

public class TableCellsHighlighter extends FocusCellHighlighter {

    private final RowExposingTableViewer viewer;

    public TableCellsHighlighter(final RowExposingTableViewer viewer) {
        super(viewer);
        this.viewer = viewer;
    }

    @Override
    protected void init() {
        viewer.getControl().addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(final Event event) {
                if ((event.detail & SWT.SELECTED) > 0) {
                    final ViewerCell focusCell = getFocusCell();
                    final ViewerRow row = viewer.getViewerRowFromItem(event.item);
        
                    final ViewerCell cell = row.getCell(event.index);
                    if (focusCell == null || !cell.equals(focusCell)) {
                        removeSelectionInformation(event);
                    } else {
                        markFocusedCell(event, cell);
                    }
                }
            }
        });
    }

    private void removeSelectionInformation(final Event event) {
        paint(event, RedTheme.getHiglihtedRowColor(), getBlackColor());
    }

    private void markFocusedCell(final Event event, final ViewerCell cell) {
        final Color background = (cell.getControl().isFocusControl()) ? 
                RedTheme.getHighlightedCellColor() : RedTheme.getHiglihtedRowColor();
        paint(event, background, getBlackColor());
    }

    private void paint(final Event event, final Color background, final Color foreground) {
        final GC gc = event.gc;
        gc.setBackground(background);
        gc.setForeground(foreground);
        gc.fillRectangle(event.getBounds());

        event.detail &= ~SWT.SELECTED;
    }

    @Override
    protected void focusCellChanged(final ViewerCell newCell, final ViewerCell oldCell) {
        super.focusCellChanged(newCell, oldCell);

        // Redraw new area
        if (newCell != null) {
            final Rectangle rect = newCell.getBounds();
            final int x = newCell.getColumnIndex() == 0 ? 0 : rect.x;
            final int width = newCell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
            // 1 is a fix for Linux-GTK
            newCell.getControl().redraw(x, rect.y - 1, width, rect.height + 1, true);
        }
        if (oldCell != null) {
            final Rectangle rect = oldCell.getBounds();
            final int x = oldCell.getColumnIndex() == 0 ? 0 : rect.x;
            final int width = oldCell.getColumnIndex() == 0 ? rect.x + rect.width : rect.width;
            // 1 is a fix for Linux-GTK
            oldCell.getControl().redraw(x, rect.y - 1, width, rect.height + 1, true);
        }
    }

    private Color getBlackColor() {
        return viewer.getControl().getDisplay().getSystemColor(SWT.COLOR_BLACK);
    }
}