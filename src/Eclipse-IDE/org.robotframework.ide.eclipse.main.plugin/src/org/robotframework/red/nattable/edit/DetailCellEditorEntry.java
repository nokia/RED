/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.robotframework.red.graphics.ColorsManager;

/**
 * @author Michal Anglart
 *
 */
public abstract class DetailCellEditorEntry<D> extends Canvas {

    protected static final int HOVER_BLOCK_WIDTH = 10;
    protected static final int SPACING_AROUND_LINE = 5;
    protected static final int LINE_WIDTH = 2;

    protected final int column;
    protected final int row;

    protected final Color hoverColor;
    protected final Color selectionColor;

    private boolean hovered = false;
    private boolean selected = false;
    private boolean underEdit = false;

    private Consumer<String> editorListener;

    protected CellEditorValueValidationJobScheduler<String> validationJobScheduler;

    public DetailCellEditorEntry(final Composite parent, final int column, final int row, final Color hoverColor,
            final Color selectionColor) {
        super(parent, SWT.NO_BACKGROUND);
        this.column = column;
        this.row = row;
        this.hoverColor = hoverColor;
        this.selectionColor = selectionColor;
        this.validationJobScheduler = new CellEditorValueValidationJobScheduler<>(getValidator());

        addMouseTrackListener(new MouseTrackAdapter() {
            @Override
            public void mouseHover(final MouseEvent e) {
                if (!isAnySiblingUnderEdit() && e.stateMask == 0) {
                    select(true, false);
                }
            }

            @Override
            public void mouseEnter(final MouseEvent e) {
                hovered = true;
                redraw();
            }

            @Override
            public void mouseExit(final MouseEvent e) {
                hovered = false;
                redraw();
            }
        });
    }

    protected abstract CellEditorValueValidator<String> getValidator();

    public void setEditorListener(final Consumer<String> listener) {
        editorListener = listener;
    }

    public void removeEditorListener() {
        setEditorListener(null);
    }


    protected boolean isHovered() {
        return hovered;
    }

    public boolean isSelected() {
        return selected;
    }

    public void select(final boolean deselectOther) {
        select(deselectOther, true);
    }

    private void select(final boolean deselectOther, final boolean gainFocus) {
        if (deselectOther) {
            deselectAllSiblings();
        }
        selected = true;
        if (gainFocus) {
            setFocus();
        }
        redraw();
    }

    private void deselectAllSiblings() {
        for (final Control child : getParent().getChildren()) {
            if (child instanceof DetailCellEditorEntry && child != this
                    && ((DetailCellEditorEntry<?>) child).isSelected()) {
                ((DetailCellEditorEntry<?>) child).deselect();
            }
        }
    }

    private boolean isAnySiblingUnderEdit() {
        for (final Control child : getParent().getChildren()) {
            if (child instanceof DetailCellEditorEntry && child != this
                    && ((DetailCellEditorEntry<?>) child).underEdit) {
                return true;
            }
        }
        return false;
    }

    public void deselect() {
        selected = false;
        hovered = false;
        redraw();
    }

    public boolean isEditorOpened() {
        return underEdit;
    }

    public void openForEditing() {
        underEdit = true;
    }

    public void commitEdit() {
        if (validationJobScheduler.canCloseCellEditor()) {
            final String newValue = getNewValue();
            closeEditing();
            editorListener.accept(newValue);
        }
    }

    protected abstract String getNewValue();

    public void cancelEdit() {
        closeEditing();
    }

    protected void closeEditing() {
        underEdit = false;
    }

    public abstract void update(final D detail);

    protected abstract class EntryControlPainter implements PaintListener {

        @Override
        public void paintControl(final PaintEvent e) {
            final Image bufferImage = new Image(e.display, e.width, e.height);
            final GC bufferGC = new GC(bufferImage);
            bufferGC.setBackground(e.gc.getBackground());
            bufferGC.setForeground(e.gc.getForeground());

            paintBackground(e.width, e.height, bufferGC);
            if (e.height == ((Control) e.widget).getSize().y
                    || ((Control) e.widget).getBounds().y < ((Control) e.widget).getSize().y) {
                // either the control is fully visible or it is not but is not the first entry
                paintHorizontalLine(e.width, e.height, bufferGC);
            }
            paintForeground(e.width, e.height, bufferGC);

            e.gc.drawImage(bufferImage, 0, 0);

            bufferImage.dispose();
            bufferGC.dispose();
        }

        private void paintBackground(final int width, final int height, final GC bufferGC) {
            Color bgColorInUse = bufferGC.getBackground();
            bufferGC.fillRectangle(0, 0, width, height);

            if (isSelected() && selectionColor != null) {
                bgColorInUse = selectionColor;
                bufferGC.setBackground(bgColorInUse);
                bufferGC.fillRectangle(0, 0, width, height);
            }
            if (isHovered() && hoverColor != null) {
                bufferGC.setBackground(hoverColor);
                bufferGC.fillRectangle(width - HOVER_BLOCK_WIDTH, 0, HOVER_BLOCK_WIDTH, height);
                bufferGC.setBackground(bgColorInUse);
            }
        }

        private void paintHorizontalLine(final int width, final int height, final GC bufferGC) {
            final Color originalFg = bufferGC.getForeground();

            bufferGC.setForeground(ColorsManager.getColor(220, 220, 220));
            bufferGC.drawLine(0, height - 1, width, height - 1);

            bufferGC.setForeground(originalFg);
        }

        protected abstract void paintForeground(final int width, final int height, final GC bufferGC);
    }

}
