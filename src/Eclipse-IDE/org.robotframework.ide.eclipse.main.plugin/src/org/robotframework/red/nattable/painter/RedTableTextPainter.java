/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.painter;

import static org.eclipse.jface.viewers.Stylers.mixingStyler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeMap;
import com.google.common.collect.TreeRangeSet;

/**
 * Cell painter which paints text with matches and hyperlinks using stylers.
 * 
 * @author Michal Anglart
 */
public class RedTableTextPainter extends TextPainter {

    private static final Pattern NEW_LINE_PATTERN = Pattern.compile(NEW_LINE_REGEX);

    // paintCell method is taken from super class TextPainter

    public RedTableTextPainter(final boolean wrapContent) {
        this(wrapContent, 0);
    }

    public RedTableTextPainter(final boolean wrapContent, final int spacing) {
        super(wrapContent, true, spacing, wrapContent, true);
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
            final String originalText = convertDataType(cell, configRegistry);

            final String text = getTextToDisplay(cell, gc, rectangle.width, originalText);

            final int numberOfNewLines = getNumberOfNewLines(text);

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
            final Function<String, RangeMap<Integer, Styler>> stylesSupplier = cellStyle
                    .getAttributeValue(ITableStringsDecorationsSupport.RANGES_STYLES);

            final StyledString styledString = highlightMatches(new StyledString(text), originalText,
                    Stylers.withFont(gc.getFont()), data, matchesSupplier, stylesSupplier);
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

    @VisibleForTesting
    final StyledString highlightMatches(final StyledString label, final String originalText,
            final Styler defaultStyler, final TableCellStringData data,
            final Supplier<HeaderFilterMatchesCollection> matchesSupplier,
            final Function<String, RangeMap<Integer, Styler>> stylesSupplier) {

        if (label == null || label.length() == 0) {
            return new StyledString();
        }
        final String text = label.getString();

        final Supplier<int[]> mappingSupplier = originalToWrappedMapping(text, originalText);

        final RangeSet<Integer> hyperlinks = getHyperlinks(mappingSupplier, data, text);
        final RangeSet<Integer> matches = getMatches(mappingSupplier, matchesSupplier, text, originalText);
        final RangeMap<Integer, Styler> additionalStyles = getAdditionalStyles(mappingSupplier, stylesSupplier, text,
                originalText);

        List<Styler> previousStylers = new ArrayList<>();
        for (int i = 0, start = 0; i <= label.length(); i++) {

            final List<Styler> stylers = new ArrayList<>();

            stylers.add(defaultStyler);
            if (hyperlinks.contains(i)) {
                stylers.add(Stylers.Common.HYPERLINK_STYLER);
            }
            if (matches.contains(i)) {
                stylers.add(Stylers.Common.MATCH_STYLER);
            }
            if (additionalStyles.get(i) != null) {
                stylers.add(additionalStyles.get(i));
            }

            if (!containsSameObjects(stylers, previousStylers) || i == label.length()) {
                label.setStyle(start, i - start, mixingStyler(previousStylers));
                start = i;
            }
            previousStylers = stylers;
        }
        return label;
    }

    private static <T> boolean containsSameObjects(final List<T> l, final List<T> r) {
        if (l.size() != r.size()) {
            return false;
        }
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i) != r.get(i)) {
                return false;
            }
        }
        return true;
    }

    private RangeSet<Integer> getHyperlinks(final Supplier<int[]> originalToWrappedMapping,
            final TableCellStringData data, final String text) {
        final RangeSet<Integer> hyperlinkRanges = TreeRangeSet.create();
        if (data != null && data.getHyperlinkRegion() != null) {
            hyperlinkRanges.add(data.getHyperlinkRegion());
        }

        return mapOriginalRangesToCurrentLabel(hyperlinkRanges, originalToWrappedMapping, text);
    }

    private RangeSet<Integer> getMatches(final Supplier<int[]> originalToWrappedMapping,
            final Supplier<HeaderFilterMatchesCollection> matchesSupplier, final String text,
            final String originalText) {
        final HeaderFilterMatchesCollection matches = matchesSupplier.get();
        final RangeSet<Integer> matchRanges = matches == null ? TreeRangeSet.create() : matches.getRanges(originalText);

        return mapOriginalRangesToCurrentLabel(matchRanges, originalToWrappedMapping, text);
    }

    private RangeMap<Integer, Styler> getAdditionalStyles(final Supplier<int[]> originalToWrappedMapping,
            final Function<String, RangeMap<Integer, Styler>> stylesSupplier, final String text,
            final String originalText) {
        final RangeMap<Integer, Styler> additionalStylesRanges = stylesSupplier == null ? TreeRangeMap.create()
                : stylesSupplier.apply(originalText);

        return mapOriginalRangesToCurrentLabel(additionalStylesRanges, originalToWrappedMapping, text);
    }

    private RangeSet<Integer> mapOriginalRangesToCurrentLabel(final RangeSet<Integer> ranges,
            final Supplier<int[]> originalToWrappedMapping, final String text) {
        if (wrapText && calculateByTextLength) {
            final RangeSet<Integer> mappedRanges = TreeRangeSet.create();
            for (final Range<Integer> range : ranges.asRanges()) {
                splitRangesForWrappedLabel(originalToWrappedMapping.get(), text, range)
                        .forEach(r -> mappedRanges.add(r));
            }
            return mappedRanges;
        } else {
            return ranges;
        }
    }

    private RangeMap<Integer, Styler> mapOriginalRangesToCurrentLabel(final RangeMap<Integer, Styler> ranges,
            final Supplier<int[]> originalToWrappedMapping, final String text) {
        if (wrapText && calculateByTextLength) {
            final TreeRangeMap<Integer, Styler> mappedRanges = TreeRangeMap.create();
            for (final Entry<Range<Integer>, Styler> range : ranges.asMapOfRanges().entrySet()) {
                splitRangesForWrappedLabel(originalToWrappedMapping.get(), text, range.getKey())
                        .forEach(r -> mappedRanges.put(r, range.getValue()));
            }
            return mappedRanges;
        } else {
            return ranges;
        }
    }

    private Stream<Range<Integer>> splitRangesForWrappedLabel(final int[] map, final String text,
            final Range<Integer> range) {
        final int lower = range.lowerEndpoint();
        final int upper = range.upperEndpoint() - 1;

        final int lowerEndpoint = map[lower] == -1 ? map[lower + 1] : map[lower];
        final int upperEndpoint = map[upper] == -1 ? map[upper - 1] : map[upper];
        final List<Range<Integer>> transformedRanges = new ArrayList<>();
        if (lowerEndpoint <= upperEndpoint) {
            final Matcher matcher = NEW_LINE_PATTERN.matcher(text.substring(lowerEndpoint, upperEndpoint + 1));

            int lastLower = lowerEndpoint;
            while (matcher.find()) {
                transformedRanges.add(Range.closedOpen(lastLower, matcher.start() + lowerEndpoint));
                lastLower = matcher.end() + lowerEndpoint;
            }
            transformedRanges.add(Range.closedOpen(lastLower, upperEndpoint + 1));
        }
        return transformedRanges.stream();
    }

    private static Supplier<int[]> originalToWrappedMapping(final String text, final String originalText) {
        // we use memoized supplier so that the mapping is not recalculated all the time
        return Suppliers.memoize(() -> calculateOriginalToWrappedLabelMapping(text, originalText));
    }

    // returned array maps indexes of characters in original text into indexes of characters in
    // wrapped text
    private static int[] calculateOriginalToWrappedLabelMapping(final String text, final String originalText) {
        final int[] map = new int[originalText.length()];
        for (int i = 0, j = 0; i < map.length; i++, j++) {
            // there is either \r\n or \n\r
            if (text.charAt(j) == '\r' || text.charAt(j) == '\n') {
                if (j < text.length() - 1 && (text.charAt(j + 1) == '\r' || text.charAt(j + 1) == '\n')
                        && text.charAt(j) != text.charAt(j + 1)) {
                    j++;
                }
                if (originalText.charAt(i) == ' ') {
                    map[i] = -1;
                    i++;
                }
                j++;
            }
            map[i] = j;
        }
        return map;
    }
}
