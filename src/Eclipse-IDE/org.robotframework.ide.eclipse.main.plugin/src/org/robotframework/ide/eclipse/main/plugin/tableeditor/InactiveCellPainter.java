/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.robotframework.red.graphics.ColorsManager;

/**
 * @author wypych
 */
public class InactiveCellPainter extends CellPainterWrapper {

    @Override
    public void paintCell(ILayerCell cell, GC gc, Rectangle bounds, IConfigRegistry configRegistry) {
        Color backgroundColor = ColorsManager.getColor(GUIHelper.COLOR_WIDGET_LIGHT_SHADOW.getRGB());
        if (backgroundColor != null) {
            Color originalBackground = gc.getBackground();

            gc.setBackground(backgroundColor);
            gc.fillRectangle(bounds);

            gc.setBackground(originalBackground);
        }

        super.paintCell(cell, gc, bounds, configRegistry);
    }
}
