/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.red.nattable;

import java.util.Objects;

import org.eclipse.swt.graphics.Point;

import com.google.common.collect.Range;

public final class TableCellStringData {

    private String drawnString;

    private Point coordinate;

    private Point extent;

    private Range<Integer> hyperlinkRange;

    public TableCellStringData(final String drawnString, final Point coordinate, final Point extent) {
        this.drawnString = drawnString;
        this.coordinate = coordinate;
        this.extent = extent;
    }

    public void rewriteFrom(final TableCellStringData that) {
        this.drawnString = that.drawnString;
        this.coordinate = that.coordinate;
        this.extent = that.extent;
        this.hyperlinkRange = this.hyperlinkRange != null ? this.hyperlinkRange : that.hyperlinkRange;
    }

    public String getString() {
        return drawnString;
    }

    public Point getCoordinate() {
        return coordinate;
    }

    public Point getExtent() {
        return extent;
    }

    public Range<Integer> getHyperlinkRegion() {
        return hyperlinkRange;
    }

    public void createHyperlinkAt(final int startIndex, final int endIndex) {
        this.hyperlinkRange = Range.closedOpen(startIndex, endIndex);
    }

    public void removeHyperlink() {
        this.hyperlinkRange = null;
    }

    public int getCharacterIndexFrom(final int x, final int y) {
        if (coordinate.x <= x && x <= coordinate.x + extent.x && coordinate.y <= y && y < coordinate.y + extent.y) {
            // the (x,y) position is over the string

            int beginX = coordinate.x;
            int begin = 0;
            int endX = beginX + extent.x;
            int end = drawnString.length();

            while (beginX < (endX - 1)) {
                final int midX = (beginX + endX) /2;
                if (x >= midX) {
                    beginX = midX;
                    begin = (begin + end) / 2;
                } else {
                    endX = midX;
                    end = (begin + end) / 2;
                }
            }
            return begin;
        }
        return -1;
    }

    @Override
    public String toString() {
        // for better readability when debugging
        return "['" + drawnString + "', coordinate: (" + coordinate.x + ", " + coordinate.y + "), extent: ("
                + extent.x + ", " + extent.y + ")]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(drawnString, coordinate, extent, hyperlinkRange);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof TableCellStringData) {
            final TableCellStringData that = (TableCellStringData) obj;
            return this.drawnString.equals(that.drawnString) && this.coordinate.equals(that.coordinate)
                    && this.extent.equals(that.extent) && Objects.equals(this.hyperlinkRange, that.hyperlinkRange);
        }
        return false;
    }
}