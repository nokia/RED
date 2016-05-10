/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.painter;

import java.util.Collection;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;

/**
 * Cell painter which paints text with matches styled by given styler.
 * 
 * @author Michal Anglart
 */
public class SearchMatchesTextPainter extends TextPainter {

    // paintCell method is taken from super class TextPainter with a little except that there is no
    // support for text wrapping currently

    private final Supplier<HeaderFilterMatchesCollection> matchesSupplier;

    private final Styler matchStyler;

    public SearchMatchesTextPainter(final Supplier<HeaderFilterMatchesCollection> matchesSupplier,
            final Styler matchStyler) {
        this.matchesSupplier = matchesSupplier;
        this.matchStyler = matchStyler;
    }

    @Override
    public void paintCell(final ILayerCell cell, final GC gc, final Rectangle rectangle,
            final IConfigRegistry configRegistry) {
        final boolean paintFg = this.paintFg;
        this.paintFg = false;
        super.paintCell(cell, gc, rectangle, configRegistry);
        this.paintFg = paintFg;

        if (this.paintFg) {
            final Rectangle originalClipping = gc.getClipping();
            gc.setClipping(rectangle.intersection(originalClipping));

            final IStyle cellStyle = CellStyleUtil.getCellStyle(cell, configRegistry);
            setupGCFromConfig(gc, cellStyle);

            final boolean underline = renderUnderlined(cellStyle);
            final boolean strikethrough = renderStrikethrough(cellStyle);

            final int fontHeight = gc.getFontMetrics().getHeight();
            String text = convertDataType(cell, configRegistry);

            text = getTextToDisplay(cell, gc, rectangle.width, text);

            // no wrapping so always 1
            final int numberOfNewLines = 1;

            // if the content height is bigger than the available row height
            // we're extending the row height (only if word wrapping is enabled)
            final int contentHeight = (fontHeight * numberOfNewLines) + (this.spacing * 2);
            final int contentToCellDiff = (cell.getBounds().height - rectangle.height);

            if (performRowResize(contentHeight, rectangle)) {
                final ILayer layer = cell.getLayer();
                layer.doCommand(new RowResizeCommand(layer, cell.getRowPosition(), contentHeight + contentToCellDiff));
            }

            final int contentWidth = Math.min(getLengthFromCache(gc, text), rectangle.width);

            final StyledString styledString = highlightMatches(text);

            final TextLayout layout = new TextLayout(gc.getDevice());
            layout.setText(text);
            for (final StyleRange range : styledString.getStyleRanges()) {
                layout.setStyle(range, range.start, range.start + range.length - 1);
            }
            layout.draw(gc,
                    rectangle.x + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth)
                            + this.spacing,
                    rectangle.y + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight)
                            + this.spacing,
                    -1, -1, null, null, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);
            layout.dispose();

            if (underline || strikethrough) {
                final int length = gc.textExtent(text).x;
                if (length > 0) {
                    final int xTextStart = rectangle.x
                            + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth)
                            + this.spacing;
                    final int yTextStart = rectangle.y
                            + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight)
                            + this.spacing;

                    if (underline) {
                        final int underlineY = yTextStart + fontHeight - (gc.getFontMetrics().getDescent() / 2);
                        gc.drawLine(xTextStart, underlineY, xTextStart + length, underlineY);
                    }
                    if (strikethrough) {
                        final int strikeY = yTextStart + (fontHeight / 2) + (gc.getFontMetrics().getLeading() / 2);
                        gc.drawLine(xTextStart, strikeY, xTextStart + length, strikeY);
                    }
                }
            }

            gc.setClipping(originalClipping);
        }
    }

    private final StyledString highlightMatches(final String label) {
        return highlightMatches(new StyledString(label), 0, label);
    }

    private final StyledString highlightMatches(final StyledString label, final int shift, final String modelContent) {
        if (label == null) {
            return new StyledString();
        }
        final HeaderFilterMatchesCollection matches = matchesSupplier.get();
        if (matches == null) {
            return label;
        }
        final Collection<Range<Integer>> ranges = matches.getRanges(modelContent);
        if (ranges == null) {
            return label;
        }
        for (final Range<Integer> range : ranges) {
            label.setStyle(range.lowerEndpoint() + shift, range.upperEndpoint() - range.lowerEndpoint(), matchStyler);
        }
        return label;
    }
}
