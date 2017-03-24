/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable.painter;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Stylers;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.HeaderFilterMatchesCollection;
import org.robotframework.red.nattable.TableCellStringData;

public class RedTableTextPainterTest {

    @Test
    public void singleLinePainterProperlyMarksMatches() {
        final RedTableTextPainter painter = new RedTableTextPainter(false);
        
        final String orignalText = "label";
        final StyledString label = new StyledString(orignalText);

        final TableCellStringData data = new TableCellStringData("label", new Point(0, 0), new Point(25, 15));
        painter.highlightMatches(label, orignalText, data, Stylers.Common.EMPTY_STYLER, () -> matches("label", "l"));

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

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground).isNull();
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[1].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(3);

        assertThat(label.getStyleRanges()[2].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[2].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(1);
    }

    @Test
    public void singleLinePainterProperlyMarksHyperlink() {
        final RedTableTextPainter painter = new RedTableTextPainter(false);

        final String orignalText = "label";
        final StyledString label = new StyledString(orignalText);

        final TableCellStringData data = new TableCellStringData("label", new Point(0, 0), new Point(25, 15));
        data.createHyperlinkAt(1, 3);
        painter.highlightMatches(label, orignalText, data, Stylers.Common.EMPTY_STYLER, () -> emptyMatches());

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

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].underline).isTrue();
        assertThat(label.getStyleRanges()[1].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[1].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(2);

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(3);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(2);
    }

    @Test
    public void singleLinePainterProperlyMarksMatchesAndHyperlink() {
        final RedTableTextPainter painter = new RedTableTextPainter(false);

        final String orignalText = "label";
        final StyledString label = new StyledString(orignalText);

        final TableCellStringData data = new TableCellStringData("label", new Point(0, 0), new Point(25, 15));
        data.createHyperlinkAt(0, 2);
        painter.highlightMatches(label, orignalText, data, Stylers.Common.EMPTY_STYLER, () -> matches("label", "l"));

        final TextStyle hyperlinkStyle = new TextStyle();
        Stylers.Common.HYPERLINK_STYLER.applyStyles(hyperlinkStyle);

        final TextStyle matchStyle = new TextStyle();
        Stylers.Common.MATCH_STYLER.applyStyles(matchStyle);

        assertThat(label.getString()).isEqualTo("label");
        assertThat(label.getStyleRanges()).hasSize(4);
        assertThat(label.getStyleRanges()[0].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[0].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[0].borderColor).isNull();
        assertThat(label.getStyleRanges()[0].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[0].underline).isTrue();
        assertThat(label.getStyleRanges()[0].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[0].start).isEqualTo(0);
        assertThat(label.getStyleRanges()[0].length).isEqualTo(1);

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[1].underline).isTrue();
        assertThat(label.getStyleRanges()[1].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[1].start).isEqualTo(1);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(1);

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(2);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(2);

        assertThat(label.getStyleRanges()[3].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[3].foreground).isNull();
        assertThat(label.getStyleRanges()[3].borderColor).isNull();
        assertThat(label.getStyleRanges()[3].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[3].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[3].length).isEqualTo(1);
    }

    @Test
    public void lineWrappingPainterProperlyMarksMatcheBetweenLines() {
        final RedTableTextPainter painter = new RedTableTextPainter(true);

        final String orignalText = "line line";
        final StyledString label = new StyledString("line\nline");

        final TableCellStringData data = new TableCellStringData("line\nline", new Point(0, 0), new Point(25, 15));
        painter.highlightMatches(label, orignalText, data, Stylers.Common.EMPTY_STYLER,
                () -> matches("line line", "ne li"));

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

        assertThat(label.getStyleRanges()[1].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[1].foreground).isNull();
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[1].start).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(2);

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[2].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(1);

        assertThat(label.getStyleRanges()[3].background.getRGB()).isEqualTo(matchStyle.background.getRGB());
        assertThat(label.getStyleRanges()[3].foreground).isNull();
        assertThat(label.getStyleRanges()[3].borderColor).isNull();
        assertThat(label.getStyleRanges()[3].borderStyle).isEqualTo(matchStyle.borderStyle);
        assertThat(label.getStyleRanges()[3].start).isEqualTo(5);
        assertThat(label.getStyleRanges()[3].length).isEqualTo(2);

        assertThat(label.getStyleRanges()[4].background).isNull();
        assertThat(label.getStyleRanges()[4].foreground).isNull();
        assertThat(label.getStyleRanges()[4].borderColor).isNull();
        assertThat(label.getStyleRanges()[4].borderStyle).isEqualTo(0);
        assertThat(label.getStyleRanges()[4].start).isEqualTo(7);
        assertThat(label.getStyleRanges()[4].length).isEqualTo(2);
    }

    @Test
    public void lineWrappingPainterProperlyMarksHyperlinkBetweenLines() {
        final RedTableTextPainter painter = new RedTableTextPainter(true);

        final String orignalText = "line line";
        final StyledString label = new StyledString("line\r\nline");

        final TableCellStringData data = new TableCellStringData("line\r\nline", new Point(0, 0), new Point(25, 15));
        data.createHyperlinkAt(2, 7);
        painter.highlightMatches(label, orignalText, data, Stylers.Common.EMPTY_STYLER, () -> emptyMatches());

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

        assertThat(label.getStyleRanges()[1].background).isNull();
        assertThat(label.getStyleRanges()[1].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[1].borderColor).isNull();
        assertThat(label.getStyleRanges()[1].underline).isTrue();
        assertThat(label.getStyleRanges()[1].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[1].start).isEqualTo(2);
        assertThat(label.getStyleRanges()[1].length).isEqualTo(2);

        assertThat(label.getStyleRanges()[2].background).isNull();
        assertThat(label.getStyleRanges()[2].foreground).isNull();
        assertThat(label.getStyleRanges()[2].borderColor).isNull();
        assertThat(label.getStyleRanges()[2].underline).isFalse();
        assertThat(label.getStyleRanges()[2].underlineColor).isNull();
        assertThat(label.getStyleRanges()[2].start).isEqualTo(4);
        assertThat(label.getStyleRanges()[2].length).isEqualTo(2);

        assertThat(label.getStyleRanges()[3].background).isNull();
        assertThat(label.getStyleRanges()[3].foreground.getRGB()).isEqualTo(hyperlinkStyle.foreground.getRGB());
        assertThat(label.getStyleRanges()[3].borderColor).isNull();
        assertThat(label.getStyleRanges()[3].underline).isTrue();
        assertThat(label.getStyleRanges()[3].underlineColor.getRGB()).isEqualTo(hyperlinkStyle.underlineColor.getRGB());
        assertThat(label.getStyleRanges()[3].start).isEqualTo(6);
        assertThat(label.getStyleRanges()[3].length).isEqualTo(2);

        assertThat(label.getStyleRanges()[4].background).isNull();
        assertThat(label.getStyleRanges()[4].foreground).isNull();
        assertThat(label.getStyleRanges()[4].borderColor).isNull();
        assertThat(label.getStyleRanges()[4].underline).isFalse();
        assertThat(label.getStyleRanges()[4].underlineColor).isNull();
        assertThat(label.getStyleRanges()[4].start).isEqualTo(8);
        assertThat(label.getStyleRanges()[4].length).isEqualTo(2);
    }

    private HeaderFilterMatchesCollection matches(final String label, final String filter) {
        final Matches matches = new Matches(filter, label);
        matches.collect();
        return matches;
    }

    private HeaderFilterMatchesCollection emptyMatches() {
        return new Matches("", "");
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
