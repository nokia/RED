/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.painter;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.red.nattable.TableCellStringData;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

public class RedTableTextPainterTest {

    @Test
    public void singleLinePainterProperlyMarksMatches() {
        final RedTableTextPainter painter = new RedTableTextPainter(false);
        
        final String originalText = "label";
        final StyledString label = new StyledString(originalText);

        final TableCellStringData data = new TableCellStringData("label", new Point(0, 0), new Point(25, 15));
        painter.highlightMatches(label, originalText, Stylers.Common.EMPTY_STYLER, data, () -> matches("label", "l"),
                lbl -> TreeRangeMap.create());

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MATCH_STYLER.applyStyles(matchStyle);

        assertThat(label.getString()).isEqualTo("label");
        assertThat(label.getStyleRanges()).hasSize(3);
        assertThat(label.getStyleRanges()[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[0].foreground).isNull();
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[0].font).isNull();

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground).isNull();
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[1].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(3);
        assertThat(label.getStyleRanges()[1].font).isNull();

        assertThat(label.getStyleRanges()[2].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[2].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[2].font).isNull();
    }

    @Test
    public void singleLinePainterProperlyMarksHyperlink() {
        final RedTableTextPainter painter = new RedTableTextPainter(false);

        final String originalText = "label";
        final StyledString label = new StyledString(originalText);

        final TableCellStringData data = new TableCellStringData("label", new Point(0, 0), new Point(25, 15));
        data.createHyperlinkAt(1, 3);
        painter.highlightMatches(label, originalText, Stylers.Common.EMPTY_STYLER, data, () -> emptyMatches(),
                lbl -> TreeRangeMap.create());

        final TextStyle hyperlinkStyle = new TextStyle();
        Stylers.Common.HYPERLINK_STYLER.applyStyles(hyperlinkStyle);

        assertThat(label.getString()).isEqualTo("label");
        assertThat(label.getStyleRanges()).hasSize(3);
        assertThat(label.getStyleRanges()[0].background).isNull();
        assertThat(label.getStyleRanges()[0].foreground).isNull();
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].underline).isFalse();
        assertThat(label.getStyleRanges()[0].underlineColor).isNull();
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[0].font).isNull();

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].underline).isTrue();
        assertThat(label.getStyleRanges()[1].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[1].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].font).isNull();

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(3);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[2].font).isNull();
    }

    @Test
    public void singleLinePainterProperlyMarksAdditionalStyles() {
        final RedTableTextPainter painter = new RedTableTextPainter(false);

        final String originalText = "label";
        final StyledString label = new StyledString(originalText);

        final TableCellStringData data = new TableCellStringData("label", new Point(0, 0), new Point(25, 15));
        painter.highlightMatches(label, originalText, Stylers.Common.EMPTY_STYLER, data, () -> emptyMatches(),
                lbl -> additionalStylers(Range.closedOpen(1, 3), Stylers.Common.BOLD_STYLER));

        final TextStyle boldStyle = new TextStyle();
        Stylers.Common.BOLD_STYLER.applyStyles(boldStyle);

        assertThat(label.getString()).isEqualTo("label");
        assertThat(label.getStyleRanges()).hasSize(3);
        assertThat(label.getStyleRanges()[0].background).isNull();
        assertThat(label.getStyleRanges()[0].foreground).isNull();
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].underline).isFalse();
        assertThat(label.getStyleRanges()[0].underlineColor).isNull();
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[0].font).isNull();

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground).isNull();
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].underline).isFalse();
        assertThat(label.getStyleRanges()[1].underlineColor).isNull();
        assertThat(label.getStyleRanges()[1].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].font.getFontData()).isEqualTo(boldStyle.font.getFontData());

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(3);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[2].font).isNull();
    }

    @Test
    public void singleLinePainterProperlyMarksMatchesHyperlinkAndAdditionalStyles() {
        final RedTableTextPainter painter = new RedTableTextPainter(false);

        final String originalText = "label";
        final StyledString label = new StyledString(originalText);

        final TableCellStringData data = new TableCellStringData("label", new Point(0, 0), new Point(25, 15));
        data.createHyperlinkAt(0, 2);
        painter.highlightMatches(label, originalText, Stylers.Common.EMPTY_STYLER, data, () -> matches("label", "l"),
                lbl -> additionalStylers(Range.closedOpen(1, 3), Stylers.Common.BOLD_STYLER));

        final TextStyle hyperlinkStyle = new TextStyle();
        Stylers.Common.HYPERLINK_STYLER.applyStyles(hyperlinkStyle);

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MATCH_STYLER.applyStyles(matchStyle);

        final TextStyle boldStyle = new TextStyle();
        Stylers.Common.BOLD_STYLER.applyStyles(boldStyle);

        assertThat(label.getString()).isEqualTo("label");
        assertThat(label.getStyleRanges()).hasSize(5);
        assertThat(label.getStyleRanges()[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[0].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[0].underline).isTrue();
        assertThat(label.getStyleRanges()[0].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[0].font).isNull();

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[1].underline).isTrue();
        assertThat(label.getStyleRanges()[1].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[1].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[1].font.getFontData()).isEqualTo(boldStyle.font.getFontData());

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(2);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[2].font.getFontData()).isEqualTo(boldStyle.font.getFontData());

        assertThat(label.getStyleRanges()[3].background).isNull();
        assertThat(label.getStyleRanges()[3].foreground).isNull();
        assertThat(label.getStyleRanges()[3].borderColor).isNull();
        assertThat(label.getStyleRanges()[3].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[3].start).isEqualTo(3);
        assertThat(label.getStyleRanges()[3].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[3].font).isNull();

        assertThat(label.getStyleRanges()[4].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[4].foreground).isNull();
        assertThat(label.getStyleRanges()[4].borderColor).isNull();
        assertThat(label.getStyleRanges()[4].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[4].underline).isFalse();
        assertThat(label.getStyleRanges()[4].underlineColor).isNull();
        assertThat(label.getStyleRanges()[4].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[4].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[4].font).isNull();
    }

    @Test
    public void lineWrappingPainterProperlyMarksMatcheBetweenLines() {
        final RedTableTextPainter painter = new RedTableTextPainter(true);

        final String originalText = "line line";
        final StyledString label = new StyledString("line\nline");

        final TableCellStringData data = new TableCellStringData("line\nline", new Point(0, 0), new Point(25, 15));
        painter.highlightMatches(label, originalText, Stylers.Common.EMPTY_STYLER, data,
                () -> matches("line line", "ne li"), lbl -> TreeRangeMap.create());

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MATCH_STYLER.applyStyles(matchStyle);

        assertThat(label.getString()).isEqualTo("line\nline");
        assertThat(label.getStyleRanges()).hasSize(5);

        assertThat(label.getStyleRanges()[0].background).isNull();
        assertThat(label.getStyleRanges()[0].foreground).isNull();
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[0].font).isNull();

        assertThat(label.getStyleRanges()[1].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[1].foreground).isNull();
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[1].start).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].font).isNull();

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[2].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(1);
        assertThat(label.getStyleRanges()[2].font).isNull();

        assertThat(label.getStyleRanges()[3].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[3].foreground).isNull();
        assertThat(label.getStyleRanges()[3].borderColor).isNull();
        assertThat(label.getStyleRanges()[3].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[3].start).isEqualTo(5);
        assertThat(label.getStyleRanges()[3].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[3].font).isNull();

        assertThat(label.getStyleRanges()[4].background).isNull();
        assertThat(label.getStyleRanges()[4].foreground).isNull();
        assertThat(label.getStyleRanges()[4].borderColor).isNull();
        assertThat(label.getStyleRanges()[4].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[4].start).isEqualTo(7);
        assertThat(label.getStyleRanges()[4].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[4].font).isNull();
    }

    @Test
    public void lineWrappingPainterProperlyMarksHyperlinkBetweenLines() {
        final RedTableTextPainter painter = new RedTableTextPainter(true);

        final String originalText = "line line";
        final StyledString label = new StyledString("line\r\nline");

        final TableCellStringData data = new TableCellStringData("line\r\nline", new Point(0, 0), new Point(25, 15));
        data.createHyperlinkAt(2, 7);
        painter.highlightMatches(label, originalText, Stylers.Common.EMPTY_STYLER, data, () -> emptyMatches(),
                lbl -> TreeRangeMap.create());

        final TextStyle hyperlinkStyle = new TextStyle();
        Stylers.Common.HYPERLINK_STYLER.applyStyles(hyperlinkStyle);

        assertThat(label.getString()).isEqualTo("line\r\nline");
        assertThat(label.getStyleRanges()).hasSize(5);

        assertThat(label.getStyleRanges()[0].background).isNull();
        assertThat(label.getStyleRanges()[0].foreground).isNull();
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].underline).isFalse();
        assertThat(label.getStyleRanges()[0].underlineColor).isNull();
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[0].font).isNull();

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].underline).isTrue();
        assertThat(label.getStyleRanges()[1].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[1].start).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].font).isNull();

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[2].font).isNull();

        assertThat(label.getStyleRanges()[3].background).isNull();
        assertThat(label.getStyleRanges()[3].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[3].borderColor).isNull();
        assertThat(label.getStyleRanges()[3].underline).isTrue();
        assertThat(label.getStyleRanges()[3].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[3].start).isEqualTo(6);
        assertThat(label.getStyleRanges()[3].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[3].font).isNull();

        assertThat(label.getStyleRanges()[4].background).isNull();
        assertThat(label.getStyleRanges()[4].foreground).isNull();
        assertThat(label.getStyleRanges()[4].borderColor).isNull();
        assertThat(label.getStyleRanges()[4].underline).isFalse();
        assertThat(label.getStyleRanges()[4].underlineColor).isNull();
        assertThat(label.getStyleRanges()[4].start).isEqualTo(8);
        assertThat(label.getStyleRanges()[4].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[4].font).isNull();
    }

    @Test
    public void lineWrappingPainterProperlyMarksAdditionalStylesBetweenLines() {
        final RedTableTextPainter painter = new RedTableTextPainter(true);

        final String originalText = "line line";
        final StyledString label = new StyledString("line\r\nline");

        final TableCellStringData data = new TableCellStringData("line\r\nline", new Point(0, 0), new Point(25, 15));
        painter.highlightMatches(label, originalText, Stylers.Common.EMPTY_STYLER, data, () -> emptyMatches(),
                lbl -> additionalStylers(Range.closedOpen(2, 7), Stylers.Common.BOLD_STYLER));

        final TextStyle boldStyle = new TextStyle();
        Stylers.Common.BOLD_STYLER.applyStyles(boldStyle);

        assertThat(label.getString()).isEqualTo("line\r\nline");
        assertThat(label.getStyleRanges()).hasSize(5);

        assertThat(label.getStyleRanges()[0].background).isNull();
        assertThat(label.getStyleRanges()[0].foreground).isNull();
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].underline).isFalse();
        assertThat(label.getStyleRanges()[0].underlineColor).isNull();
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[0].font).isNull();

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground).isNull();
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].underline).isFalse();
        assertThat(label.getStyleRanges()[1].underlineColor).isNull();
        assertThat(label.getStyleRanges()[1].start).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].font.getFontData()).isEqualTo(boldStyle.font.getFontData());

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[2].font).isNull();

        assertThat(label.getStyleRanges()[3].background).isNull();
        assertThat(label.getStyleRanges()[3].foreground).isNull();
        assertThat(label.getStyleRanges()[3].borderColor).isNull();
        assertThat(label.getStyleRanges()[3].underline).isFalse();
        assertThat(label.getStyleRanges()[3].underlineColor).isNull();
        assertThat(label.getStyleRanges()[3].start).isEqualTo(6);
        assertThat(label.getStyleRanges()[3].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[3].font.getFontData()).isEqualTo(boldStyle.font.getFontData());

        assertThat(label.getStyleRanges()[4].background).isNull();
        assertThat(label.getStyleRanges()[4].foreground).isNull();
        assertThat(label.getStyleRanges()[4].borderColor).isNull();
        assertThat(label.getStyleRanges()[4].underline).isFalse();
        assertThat(label.getStyleRanges()[4].underlineColor).isNull();
        assertThat(label.getStyleRanges()[4].start).isEqualTo(8);
        assertThat(label.getStyleRanges()[4].length).isEqualTo(2);
        assertThat(label.getStyleRanges()[4].font).isNull();
    }

    private HeaderFilterMatchesCollection matches(final String label, final String filter) {
        final Matches matches = new Matches(filter, label);
        matches.collect();
        return matches;
    }

    private HeaderFilterMatchesCollection emptyMatches() {
        return new Matches("", "");
    }

    private RangeMap<Integer, Styler> additionalStylers(final Range<Integer> range, final Styler styler) {
        final RangeMap<Integer, Styler> mapping = TreeRangeMap.create();
        mapping.put(range, styler);
        return mapping;
    }

    private static final class Matches extends HeaderFilterMatchesCollection {

        private final String filter;

        private final String label;

        private Matches(final String filter, final String label) {
            this.filter = filter;
            this.label = label;
        }

        void collect() {
            collectMatches(filter, label);
        }
    }
}
