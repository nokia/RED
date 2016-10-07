/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.eclipse.swt.graphics.Point;
import org.junit.Test;

import com.google.common.collect.Range;

public class TableCellStringDataTest {

    @Test
    public void hyperlinkRegionIsProperlyCreated() {
        final TableCellStringData data = new TableCellStringData("other", new Point(30, 10), new Point(20, 15));

        assertThat(data.getString()).isEqualTo("other");
        assertThat(data.getCoordinate()).isEqualTo(new Point(30, 10));
        assertThat(data.getExtent()).isEqualTo(new Point(20, 15));
        assertThat(data.getHyperlinkRegion()).isNull();

        data.createHyperlinkAt(0, 7);

        assertThat(data.getString()).isEqualTo("other");
        assertThat(data.getCoordinate()).isEqualTo(new Point(30, 10));
        assertThat(data.getExtent()).isEqualTo(new Point(20, 15));
        assertThat(data.getHyperlinkRegion()).isEqualTo(Range.closedOpen(0, 7));
    }

    @Test
    public void hyperlinkRegionIsProperlyRemoved() {
        final TableCellStringData data = new TableCellStringData("other", new Point(30, 10), new Point(20, 15));
        data.createHyperlinkAt(0, 7);

        assertThat(data.getString()).isEqualTo("other");
        assertThat(data.getCoordinate()).isEqualTo(new Point(30, 10));
        assertThat(data.getExtent()).isEqualTo(new Point(20, 15));
        assertThat(data.getHyperlinkRegion()).isEqualTo(Range.closedOpen(0, 7));

        data.removeHyperlink();

        assertThat(data.getString()).isEqualTo("other");
        assertThat(data.getCoordinate()).isEqualTo(new Point(30, 10));
        assertThat(data.getExtent()).isEqualTo(new Point(20, 15));
        assertThat(data.getHyperlinkRegion()).isNull();
    }

    @Test
    public void whenRewrittingGivenData_hyperlinkRegionsIsAlsoRewrittenIfIHaventExisted() {
        final TableCellStringData target = new TableCellStringData("string", new Point(20, 10), new Point(30, 15));

        final TableCellStringData data1 = new TableCellStringData("other", new Point(30, 10), new Point(20, 15));
        data1.createHyperlinkAt(0, 7);

        target.rewriteFrom(data1);
        assertThat(target.getString()).isEqualTo("other");
        assertThat(target.getCoordinate()).isEqualTo(new Point(30, 10));
        assertThat(target.getExtent()).isEqualTo(new Point(20, 15));
        assertThat(target.getHyperlinkRegion()).isEqualTo(Range.closedOpen(0, 7));
    }

    @Test
    public void whenRewrittingGivenData_hyperlinkRegionIsPersisted() {
        final TableCellStringData target = new TableCellStringData("string", new Point(20, 10), new Point(30, 15));
        target.createHyperlinkAt(1, 3);

        final TableCellStringData data1 = new TableCellStringData("other", new Point(30, 10), new Point(20, 15));
        data1.createHyperlinkAt(0, 7);

        final TableCellStringData data2 = new TableCellStringData("different", new Point(30, 20), new Point(40, 20));

        target.rewriteFrom(data1);
        assertThat(target.getString()).isEqualTo("other");
        assertThat(target.getCoordinate()).isEqualTo(new Point(30, 10));
        assertThat(target.getExtent()).isEqualTo(new Point(20, 15));
        assertThat(target.getHyperlinkRegion()).isEqualTo(Range.closedOpen(1, 3));

        target.rewriteFrom(data2);
        assertThat(target.getString()).isEqualTo("different");
        assertThat(target.getCoordinate()).isEqualTo(new Point(30, 20));
        assertThat(target.getExtent()).isEqualTo(new Point(40, 20));
        assertThat(target.getHyperlinkRegion()).isEqualTo(Range.closedOpen(1, 3));
    }

    @Test
    public void indexOfCharacterIsNotReturned_whenGivenCoordinateIsOutisdeOfLabelPosition() {
        final int xBegin = 10;
        final int xLength = 30;
        final int xEnd = xBegin + xLength;

        final int yBegin = 10;
        final int yLength = 15;
        final int yEnd = yBegin + yLength;

        final TableCellStringData data = new TableCellStringData("string", new Point(xBegin, yBegin),
                new Point(xLength, yLength));

        final Random r = new Random();
        // points before label on x axis
        for (int y = yBegin - 20; y <= yEnd + 20; y++) {
            assertThat(data.getCharacterIndexFrom(r.nextInt(20) - xBegin, y)).isEqualTo(-1);
        }
        // points after label on x axis
        for (int y = yBegin - 20; y <= yEnd + 20; y++) {
            assertThat(data.getCharacterIndexFrom(r.nextInt(20) + xEnd + 10, y)).isEqualTo(-1);
        }
        // points over label on y axis
        for (int x = xBegin - 20; x <= xEnd + 20; x++) {
            assertThat(data.getCharacterIndexFrom(x, r.nextInt(20) - 10)).isEqualTo(-1);
        }
        // points below label on y axis
        for (int x = xBegin - 20; x <= xEnd + 20; x++) {
            assertThat(data.getCharacterIndexFrom(x, r.nextInt(20) + yEnd + 10)).isEqualTo(-1);
        }
    }

    @Test
    public void indexOfCharacterIsReturned_whenGivenCoordinateIsInsideTheLabel() {
        final int yBegin = 10;
        final int yLength = 15;
        final int yEnd = yBegin + yLength;

        final TableCellStringData data = new TableCellStringData("string", new Point(10, yBegin),
                new Point(30, yLength));

        for (int y = yBegin; y < yEnd; y++) {
            assertThat(data.getCharacterIndexFrom(10, y)).isEqualTo(0);
            assertThat(data.getCharacterIndexFrom(12, y)).isEqualTo(0);
            assertThat(data.getCharacterIndexFrom(14, y)).isEqualTo(0);
            assertThat(data.getCharacterIndexFrom(16, y)).isEqualTo(0);
            assertThat(data.getCharacterIndexFrom(18, y)).isEqualTo(1);
            assertThat(data.getCharacterIndexFrom(20, y)).isEqualTo(1);
            assertThat(data.getCharacterIndexFrom(22, y)).isEqualTo(2);
            assertThat(data.getCharacterIndexFrom(24, y)).isEqualTo(2);
            assertThat(data.getCharacterIndexFrom(26, y)).isEqualTo(3);
            assertThat(data.getCharacterIndexFrom(28, y)).isEqualTo(3);
            assertThat(data.getCharacterIndexFrom(30, y)).isEqualTo(3);
            assertThat(data.getCharacterIndexFrom(32, y)).isEqualTo(4);
            assertThat(data.getCharacterIndexFrom(34, y)).isEqualTo(4);
            assertThat(data.getCharacterIndexFrom(36, y)).isEqualTo(5);
            assertThat(data.getCharacterIndexFrom(38, y)).isEqualTo(5);
            assertThat(data.getCharacterIndexFrom(40, y)).isEqualTo(5);
        }
    }
}
