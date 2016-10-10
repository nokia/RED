/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.painter;

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.resize.command.RowResizeCommand;
import org.eclipse.nebula.widgets.nattable.style.CellStyleUtil;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.IStyle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.red.nattable.ITableStringsDecorationsSupport;
import org.robotframework.red.nattable.TableCellStringData;
import org.robotframework.red.nattable.TableCellsStrings;

import com.google.common.base.Supplier;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

/**
 * Cell painter which paints text with matches and hyperlinks using stylers.
 * 
 * @author Michal Anglart
 */
public class RedTableTextPainter extends TextPainter {

    // paintCell method is taken from super class TextPainter with a little except that there is no
    // support for text wrapping currently

    public RedTableTextPainter() {
        this(0);
    }

    public RedTableTextPainter(final int spacing) {
        super(false, true, spacing);
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

            final TableCellsStrings tableStrings = configRegistry
                    .getConfigAttribute(ITableStringsDecorationsSupport.TABLE_STRINGS, DisplayMode.NORMAL);
            final TableCellStringData data = tableStrings.get(cell.getColumnPosition(), cell.getRowPosition());

            final Supplier<HeaderFilterMatchesCollection> matchesSupplier = configRegistry
                    .getConfigAttribute(ITableStringsDecorationsSupport.MATCHES_SUPPLIER, DisplayMode.NORMAL);

            final StyledString styledString = highlightMatches(new StyledString(text), data,
                    Stylers.withFont(gc.getFont()), matchesSupplier);
            final TextLayout layout = new TextLayout(gc.getDevice());
            layout.setText(text);
            for (final StyleRange range : styledString.getStyleRanges()) {
                layout.setStyle(range, range.start, range.start + range.length - 1);
            }
            final Point extent = gc.textExtent(text);
            final int textXCoord = rectangle.x
                    + CellStyleUtil.getHorizontalAlignmentPadding(cellStyle, rectangle, contentWidth) + this.spacing;
            final int textYCoord = rectangle.y
                    + CellStyleUtil.getVerticalAlignmentPadding(cellStyle, rectangle, contentHeight) + this.spacing;
            layout.draw(gc, textXCoord, textYCoord, -1, -1, null, null, SWT.DRAW_TRANSPARENT | SWT.DRAW_DELIMITER);
            layout.dispose();
            if (!text.isEmpty()) {
                tableStrings.put(cell.getColumnPosition(), cell.getRowPosition(),
                        new TableCellStringData(text, new Point(textXCoord, textYCoord), extent));
            }

            gc.setClipping(originalClipping);
        }
    }

    private final StyledString highlightMatches(final StyledString label, final TableCellStringData data,
            final Styler defaultStyler, final Supplier<HeaderFilterMatchesCollection> matchesSupplier) {
        if (label == null || label.length() == 0) {
            return new StyledString();
        }
        final RangeSet<Integer> hyperlinks = getHyperlinks(data);
        final RangeSet<Integer> matches = getMatches(label.getString(), matchesSupplier);

        final Map<CellTextRegions, Styler> stylersByRegion = stylersByRegion(defaultStyler);

        Styler previous = null;
        for (int i = 0, start = 0; i <= label.length(); i++) {
            Styler styler;
            if (hyperlinks.contains(i) && matches.contains(i)) {
                styler = stylersByRegion.get(CellTextRegions.HYPERLINK_MATCH);
            } else if (hyperlinks.contains(i)) {
                styler = stylersByRegion.get(CellTextRegions.HYPERLINK);
            } else if (matches.contains(i)) {
                styler = stylersByRegion.get(CellTextRegions.MATCH);
            } else {
                styler = stylersByRegion.get(CellTextRegions.NORMAL);
            }

            if (i == label.length() || previous != null && previous != styler) {
                label.setStyle(start, i - start, previous);
                start = i;
            }
            previous = styler;
        }
        return label;
    }

    private Map<CellTextRegions, Styler> stylersByRegion(final Styler defaultStyler) {
        final EnumMap<CellTextRegions, Styler> stylers = new EnumMap<>(CellTextRegions.class);
        stylers.put(CellTextRegions.NORMAL, defaultStyler);
        stylers.put(CellTextRegions.HYPERLINK, Stylers.mixingStyler(defaultStyler, Stylers.Common.HYPERLINK_STYLER));
        stylers.put(CellTextRegions.MATCH, Stylers.mixingStyler(defaultStyler, Stylers.Common.MATCH_STYLER));
        stylers.put(CellTextRegions.HYPERLINK_MATCH,
                Stylers.mixingStyler(defaultStyler, Stylers.Common.MATCH_STYLER, Stylers.Common.HYPERLINK_STYLER));
        return stylers;
    }

    private RangeSet<Integer> getHyperlinks(final TableCellStringData data) {
        Range<Integer> hyperlink = data == null ? null : data.getHyperlinkRegion();
        hyperlink = hyperlink == null ? Range.closed(-1, -1) : hyperlink;
        
        final RangeSet<Integer> hyperlinks = TreeRangeSet.create();
        hyperlinks.add(hyperlink);
        return hyperlinks;
    }

    private RangeSet<Integer> getMatches(final String text,
            final Supplier<HeaderFilterMatchesCollection> matchesSupplier) {
        final RangeSet<Integer> matchesRanges = TreeRangeSet.create();
        final HeaderFilterMatchesCollection matches = matchesSupplier.get();
        if (matches == null) {
            return matchesRanges;
        }
        for (final Range<Integer> range : matches.getRanges(text)) {
            matchesRanges.add(range);
        }
        return matchesRanges;
    }

    private static enum CellTextRegions {
        NORMAL,
        MATCH,
        HYPERLINK,
        HYPERLINK_MATCH
    }
}
