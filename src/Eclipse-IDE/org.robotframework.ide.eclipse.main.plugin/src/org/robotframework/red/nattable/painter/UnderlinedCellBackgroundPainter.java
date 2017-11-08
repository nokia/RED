/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.painter;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.BackgroundPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.robotframework.red.graphics.ColorsManager;

/**
 * Cell painter which paints line in cell background on given side of cell
 * 
 * @author Michal Anglart
 */
public class UnderlinedCellBackgroundPainter extends BackgroundPainter {

    private static final int LINE_WIDTH = 1;

    private final Side side;

    private final RGB color;

    private final int lineWidth;

    public UnderlinedCellBackgroundPainter(final ICellPainter cellPainter, final Side side, final RGB underlineColor) {
        this(cellPainter, side, LINE_WIDTH, underlineColor);
    }

    public UnderlinedCellBackgroundPainter(final ICellPainter cellPainter, final Side side, final int lineWidth,
            final RGB underlineColor) {
        super(cellPainter);
        this.lineWidth = lineWidth;
        this.side = side;
        this.color = underlineColor;
    }

    @Override
    public void paintCell(final ILayerCell cell, final GC gc, final Rectangle bounds,
            final IConfigRegistry configRegistry) {
        super.paintCell(cell, gc, bounds, configRegistry);

        final Color originalFg = gc.getForeground();
        final int originalLineWidth = gc.getLineWidth();

        gc.setLineWidth(lineWidth);
        gc.setForeground(ColorsManager.getColor(color));
        final Point startPoint = side.getStartPoint(bounds, lineWidth);
        final Point endPoint = side.getEndPoint(bounds, lineWidth);
        gc.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);

        gc.setForeground(originalFg);
        gc.setLineWidth(originalLineWidth);
    }

    public enum Side {
        LEFT {
            @Override
            Point getStartPoint(final Rectangle bounds, final int lineWidth) {
                return new Point(bounds.x + (int) Math.floor(lineWidth / 2.0), bounds.y);
            }

            @Override
            Point getEndPoint(final Rectangle bounds, final int lineWidth) {
                return new Point(bounds.x + (int) Math.floor(lineWidth / 2.0), bounds.y + bounds.height);
            }
        },
        RIGHT {
            @Override
            Point getStartPoint(final Rectangle bounds, final int lineWidth) {
                return new Point(bounds.x + bounds.width - (int) Math.ceil(lineWidth / 2.0), bounds.y);
            }

            @Override
            Point getEndPoint(final Rectangle bounds, final int lineWidth) {
                return new Point(bounds.x + bounds.width - (int) Math.ceil(lineWidth / 2.0), bounds.y + bounds.height);
            }
        },
        TOP {
            @Override
            Point getStartPoint(final Rectangle bounds, final int lineWidth) {
                return new Point(bounds.x, bounds.y + (int) Math.floor(lineWidth / 2.0));
            }

            @Override
            Point getEndPoint(final Rectangle bounds, final int lineWidth) {
                return new Point(bounds.x + bounds.width, bounds.y + (int) Math.floor(lineWidth / 2.0));
            }
        },
        BOTTOM {
            @Override
            Point getStartPoint(final Rectangle bounds, final int lineWidth) {
                return new Point(bounds.x, bounds.y + bounds.height - (int) Math.ceil(lineWidth / 2.0));
            }

            @Override
            Point getEndPoint(final Rectangle bounds, final int lineWidth) {
                return new Point(bounds.x + bounds.width, bounds.y + bounds.height - (int) Math.ceil(lineWidth / 2.0));
            }
        };

        abstract Point getStartPoint(final Rectangle bounds, int lineWidth);
        abstract Point getEndPoint(final Rectangle bounds, int lineWidth);
    }
}