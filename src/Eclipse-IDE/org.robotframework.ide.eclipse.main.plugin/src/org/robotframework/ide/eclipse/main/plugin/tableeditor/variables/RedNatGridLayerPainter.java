/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.variables;

import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatLayerPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.robotframework.red.graphics.ColorsManager;


/**
 * @author Michal Anglart
 *
 */
public class RedNatGridLayerPainter extends NatLayerPainter {

    private final Color gridColor;

    private int defaultRowHeight = 0;

    public RedNatGridLayerPainter(final NatTable natTable, final Color gridColor, final int defaultRowHeight) {
        super(natTable);
        this.gridColor = gridColor;
        setDefaultRowHeight(defaultRowHeight);
    }

    @Override
    protected void paintBackground(final ILayer natLayer, final GC gc, final int xOffset, final int yOffset,
            final Rectangle rectangle, final IConfigRegistry configRegistry) {
        super.paintBackground(natLayer, gc, xOffset, yOffset, rectangle, configRegistry);

        final Color gColor = configRegistry.getConfigAttribute(CellConfigAttributes.GRID_LINE_COLOR,
                DisplayMode.NORMAL);
        gc.setForeground(gColor != null ? gColor : this.gridColor);

        drawHorizontalLines(natLayer, gc, rectangle);
        drawVerticalLines(natLayer, gc, rectangle);
    }

    private void drawHorizontalLines(final ILayer natLayer, final GC gc, final Rectangle rectangle) {
        final int endX = rectangle.x + rectangle.width;

        final int rowPositionByY = natLayer.getRowPositionByY(rectangle.y + rectangle.height);
        final int maxRowPosition = rowPositionByY > 0 ? Math.min(natLayer.getRowCount(), rowPositionByY)
                : natLayer.getRowCount();

        int y = 0;
        for (int rowPosition = natLayer.getRowPositionByY(rectangle.y); rowPosition < maxRowPosition; rowPosition++) {
            y = natLayer.getStartYOfRowPosition(rowPosition) + natLayer.getRowHeightByPosition(rowPosition) - 1;
            gc.drawLine(rectangle.x, y, endX, y);
        }

        // render fake row lines to the bottom
        if (this.defaultRowHeight > 0) {
            final int endY = rectangle.y + rectangle.height;
            while (y < endY) {
                y += this.defaultRowHeight;
                gc.drawLine(rectangle.x, y, endX, y);
            }
        }
    }

    private void drawVerticalLines(final ILayer natLayer, final GC gc, final Rectangle rectangle) {
        final int endY = rectangle.y + rectangle.height;

        final int columnPositionByX = natLayer.getColumnPositionByX(rectangle.x + rectangle.width);
        final int maxColumnPosition = columnPositionByX > 0 ? Math.min(natLayer.getColumnCount(), columnPositionByX)
                : natLayer.getColumnCount();
        for (int columnPosition = natLayer
                .getColumnPositionByX(rectangle.x); columnPosition < maxColumnPosition; columnPosition++) {
            
            final int x = natLayer.getStartXOfColumnPosition(columnPosition)
                    + natLayer.getColumnWidthByPosition(columnPosition) - 1;
            if (columnPosition == 0) {
                final Color fg = gc.getForeground();
                final int width = gc.getLineWidth();

                gc.setForeground(ColorsManager.getColor(220, 220, 220));
                gc.setLineWidth(2);
                gc.drawLine(x - 1, rectangle.y, x - 1, endY);

                gc.setLineWidth(width);
                gc.setForeground(fg);
            }
            gc.drawLine(x, rectangle.y, x, endY);
        }
    }

    /**
     * @return The currently used height that is used to render fake rows. The
     *         pixel value is locally stored scaled.
     */
    public int getDefaultRowHeight() {
        return this.defaultRowHeight;
    }

    /**
     * @param defaultRowHeight
     *            The value that should be used to render fake rows. The value
     *            needs to be given in pixels, as the scaling calculation is
     *            done in here.
     */
    public void setDefaultRowHeight(final int defaultRowHeight) {
        this.defaultRowHeight = GUIHelper.convertVerticalPixelToDpi(defaultRowHeight);
    }
}
