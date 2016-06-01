/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

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

/**
 * @author Michal Anglart
 *
 */
public abstract class DetailCellEditorEntry<D> extends Canvas {

    protected static final int HOVER_BLOCK_WIDTH = 10;
    protected static final int SPACING_AROUND_LINE = 5;
    protected static final int LINE_WIDTH = 2;

    private final Color hoverColor;

    private final Color selectionColor;

    private boolean hovered = false;

    private boolean selected = false;

    private boolean underEdit = false;

    private DetailEditorListener editorListener;

    protected CellEditorValueValidationJobScheduler<String> validationJobScheduler;

    // private Job validationJob;
    //
    // private boolean cannotClose;

    public DetailCellEditorEntry(final Composite parent, final Color hoverColor, final Color selectionColor) {
        super(parent, SWT.NO_BACKGROUND);
        this.hoverColor = hoverColor;
        this.selectionColor = selectionColor;
        this.validationJobScheduler = new CellEditorValueValidationJobScheduler<>(getValidator());


        setBackground(null);
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

    public void setEditorListener(final DetailEditorListener listener) {
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
            editorListener.editorApplied(newValue);
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

    public static interface DetailEditorListener {
        void editorApplied(String value);
    }

    protected abstract class EntryControlPainter implements PaintListener {

        @Override
        public void paintControl(final PaintEvent e) {
            final Image bufferImage = new Image(e.display, e.width, e.height);
            final GC bufferGC = new GC(bufferImage);
            
            paintBackground(e.width, e.height, bufferGC);
            paintForeground(e.width, e.height, bufferGC);

            e.gc.drawImage(bufferImage, 0, 0);

            bufferImage.dispose();
            bufferGC.dispose();
        }

        private void paintBackground(final int width, final int height, final GC bufferGC) {
            Color bgColorInUse = bufferGC.getBackground();

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

        protected abstract void paintForeground(int width, int height, final GC bufferGC);
    }

}
