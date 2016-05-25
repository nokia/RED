/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.edit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
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
import org.robotframework.red.swt.SwtThread;

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

    private DetailEditorListener<D> editorListener;

    private Job validationJob;

    private boolean cannotClose;

    public DetailCellEditorEntry(final Composite parent, final Color hoverColor, final Color selectionColor) {
        super(parent, SWT.NO_BACKGROUND);
        this.hoverColor = hoverColor;
        this.selectionColor = selectionColor;

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

    public void setEditorListener(final DetailEditorListener<D> listener) {
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
        waitForValidationJobToFinish();
        if (cannotClose) {
            return;
        }
        final D newValue = createNewValue();
        closeEditing();
        editorListener.editorApplied(newValue);
    }

    private void waitForValidationJobToFinish() {
        // there could be an NPE if validationJob would be checked for nullity and then in other
        // instruction
        final Job theJob = validationJob;
        if (theJob != null) {
            try {
                theJob.join();
            } catch (final InterruptedException e) {
                // nothing to do
            }
        }
    }

    protected abstract D createNewValue();

    public void cancelEdit() {
        closeEditing();
        unblockClosing();
    }

    protected void closeEditing() {
        underEdit = false;
    }

    public abstract void update(final D detail);

    protected final void rescheduleValidation() {
        if (validationJob != null && validationJob.getState() == Job.SLEEPING) {
            validationJob.cancel();
        }
        validationJob = createValidationJob();
        validationJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(final IJobChangeEvent event) {
                validationJob = null;
            }
        });
        validationJob.schedule(300);
    }

    private Job createValidationJob() {
        return new Job("validating input") {

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                SwtThread.syncExec(new Runnable() {

                    @Override
                    public void run() {
                        validate();
                    }
                });
                return Status.OK_STATUS;
            }
        };
    }

    protected void validate() {
        // nothing to do, reimplement if needed
    }

    protected void blockClosing() {
        cannotClose = true;
    }

    protected void unblockClosing() {
        cannotClose = false;
    }

    public static interface DetailEditorListener<D> {
        void editorApplied(D value);
    }

    protected abstract class EntryControlPainter implements PaintListener {

        @Override
        public void paintControl(final PaintEvent e) {
            final Image bufferImage = new Image(e.display, e.width, e.height);
            final GC bufferGC = new GC(bufferImage);
            
            paintBackground(e, bufferGC);
            paintForeground(e, bufferGC);

            e.gc.drawImage(bufferImage, 0, 0);

            bufferImage.dispose();
            bufferGC.dispose();
        }

        private void paintBackground(final PaintEvent e, final GC bufferGC) {
            Color bgColorInUse = e.gc.getBackground();

            if (isSelected() && selectionColor != null) {
                bgColorInUse = selectionColor;
                bufferGC.setBackground(bgColorInUse);
                bufferGC.fillRectangle(0, 0, e.width, e.height);
            }
            if (isHovered() && hoverColor != null) {
                bufferGC.setBackground(hoverColor);
                bufferGC.fillRectangle(e.width - HOVER_BLOCK_WIDTH, 0, HOVER_BLOCK_WIDTH, e.height);
                bufferGC.setBackground(bgColorInUse);
            }
        }

        protected abstract void paintForeground(final PaintEvent e, final GC bufferGC);
    }

}
