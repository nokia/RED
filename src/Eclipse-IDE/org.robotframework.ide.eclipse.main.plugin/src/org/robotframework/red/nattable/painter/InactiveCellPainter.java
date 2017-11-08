/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.painter;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

/**
 * @author wypych
 * @see BackgroundPainter
 */
public class InactiveCellPainter extends CellPainterWrapper {

    private final Color backgroundColor;

    public InactiveCellPainter(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void paintCell(final ILayerCell cell, final GC gc, final Rectangle bounds,
            final IConfigRegistry configRegistry) {
        final Color originalBackground = gc.getBackground();
        gc.setBackground(backgroundColor);
        gc.fillRectangle(bounds);
        gc.setBackground(originalBackground);

        super.paintCell(cell, gc, bounds, configRegistry);
    }
}
