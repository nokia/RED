/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.robotframework.red.graphics.ColorsManager;

import com.google.common.base.Preconditions;


public class SimpleProgressBar extends Canvas {

    private int currentWork;
    private int totalWork;

    private Color progressBarColor;

    public SimpleProgressBar(final Composite parent) {
        super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_SCROLL);
        addPaintListener(e -> {
            final Rectangle clipping = e.gc.getClipping();
            final Color oldBg = e.gc.getBackground();
            final Color oldFg = e.gc.getForeground();
            final int oldAlpha = e.gc.getAlpha();
            try {
                e.gc.setForeground(ColorsManager.getColor(SWT.COLOR_DARK_GRAY));
                e.gc.setBackground(getBarColor());
                e.gc.setAlpha(150);

                if (totalWork != 0) {
                    final double ratio = (double) currentWork / (double) totalWork;
                    e.gc.setClipping(clipping.x, clipping.y, (int) Math.round(ratio * clipping.width), clipping.height);

                    e.gc.fillRoundRectangle(clipping.x, clipping.y, clipping.width, clipping.height, 6, 6);
                }
                e.gc.setClipping(clipping);
                e.gc.drawRoundRectangle(clipping.x, clipping.y, clipping.width - 1, clipping.height - 1, 6, 6);

            } finally {
                e.gc.setClipping(clipping);
                e.gc.setBackground(oldBg);
                e.gc.setForeground(oldFg);
                e.gc.setAlpha(oldAlpha);
            }
        });
    }

    public void reset() {
        progressBarColor = null;
        setProgress(0, 0);
    }

    public void setProgress(final int progress, final int total) {
        Preconditions.checkArgument(0 <= progress && progress <= total);
        Preconditions.checkArgument(total >= 0);
        this.currentWork = progress;
        this.totalWork = total;

        redraw();
    }

    public double getProgress() {
        return totalWork == 0 ? 0.0 : (double) currentWork / (double) totalWork;
    }

    public void setBarColor(final Color progressBarColor) {
        this.progressBarColor = progressBarColor;
    }

    public Color getBarColor() {
        return progressBarColor == null ? getBackground() : progressBarColor;
    }
}
