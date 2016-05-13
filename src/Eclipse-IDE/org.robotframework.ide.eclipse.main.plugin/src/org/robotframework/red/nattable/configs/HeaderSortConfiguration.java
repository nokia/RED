/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.configs;

import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortIconPainter;
import org.eclipse.nebula.widgets.nattable.sort.painter.SortableHeaderTextPainter;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.nattable.painter.UnderlinedCellBackgroundPainter;
import org.robotframework.red.nattable.painter.UnderlinedCellBackgroundPainter.Side;

/**
 * @author Michal Anglart
 *
 */
public class HeaderSortConfiguration extends SingleClickSortConfiguration {

    public HeaderSortConfiguration() {
        super(new UnderlinedCellBackgroundPainter(
                new SortableHeaderTextPainter(new TextPainter(), CellEdgeEnum.RIGHT, new HeaderSortIconPainter()),
                Side.BOTTOM, 3, new RGB(120, 180, 170)));
    }

    private static class HeaderSortIconPainter extends SortIconPainter {

        public HeaderSortIconPainter() {
            super(true);
            final Image upImage = ImagesManager.getImage(RedImages.getSortUpImage());
            final Image downImage = ImagesManager.getImage(RedImages.getSortDownImage());
            setSortImages(upImage, upImage, upImage, downImage, downImage, downImage);
        }
    }
}
