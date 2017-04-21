/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.painter;

import static com.google.common.base.Predicates.notNull;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
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

            final StyledString styledString = highlightMatches(new StyledString(text), originalText, data,
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

    @VisibleForTesting
    final StyledString highlightMatches(final StyledString label, final String orignalText,
            final TableCellStringData data, final Styler defaultStyler,
            final Supplier<HeaderFilterMatchesCollection> matchesSupplier) {
        if (label == null || label.length() == 0) {
            return new StyledString();
        }
        final RangeSet<Integer> hyperlinks = getHyperlinks(data, label.getString(), orignalText);
        final RangeSet<Integer> matches = getMatches(label.getString(), orignalText, matchesSupplier);

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

    private RangeSet<Integer> getHyperlinks(final TableCellStringData data, final String text,
            final String originalText) {
        final List<TableCellStringData> datas = data == null ? new ArrayList<>()
                : Lists.<TableCellStringData> newArrayList(data);
        final List<Range<Integer>> hyperlinkRanges = datas.stream()
                .map(TableCellStringData::getHyperlinkRegion)
                .filter(notNull())
                .collect(toList());

        final RangeSet<Integer> hyperlinksRanges = mapOriginalRangesToCurrentLabel(hyperlinkRanges, text, originalText);
        if (hyperlinksRanges.isEmpty()) {
            hyperlinksRanges.add(Range.closed(-1, -1));
        }
        return hyperlinksRanges;
    }

    private RangeSet<Integer> getMatches(final String text, final String originalText,
            final Supplier<HeaderFilterMatchesCollection> matchesSupplier) {
        final HeaderFilterMatchesCollection matches = matchesSupplier.get();
        final Collection<Range<Integer>> matchRanges = matches == null ? new ArrayList<>()
                : matches.getRanges(originalText);

        return mapOriginalRangesToCurrentLabel(matchRanges, text, originalText);
    }

    private RangeSet<Integer> mapOriginalRangesToCurrentLabel(final Collection<Range<Integer>> ranges,
            final String text, final String originalText) {
        final RangeSet<Integer> mappedRanges = TreeRangeSet.create();
        if (wrapText && calculateByTextLength) {
            mappedRanges
                    .addAll(ranges.stream()
                            .flatMap(r -> splitRangesForWrappedLabel(
                                    calculateOriginalToWrappedLabelMapping(text, originalText), text, r))
                            .collect(toList()));
        } else {
            mappedRanges.addAll(ranges);
        }
        return mappedRanges;
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

    // returned array maps indexes of characters in original text into indexes of characters in
    // wrapped text
    private int[] calculateOriginalToWrappedLabelMapping(final String text, final String originalText) {
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
                } else if (originalText.charAt(i) == '\n') {

                }
                j++;
            }
            map[i] = j;
        }
        return map;
    }

    private static enum CellTextRegions {
        NORMAL,
        MATCH,
        HYPERLINK,
        HYPERLINK_MATCH
    }
}
