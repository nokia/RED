/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

class LineDynamicAdjustSeparatorsFormatter implements ILineFormatter {

    static LineDynamicAdjustSeparatorsFormatter create(final String tabsFreeContent, final int separatorLength) {
        return create(tabsFreeContent, separatorLength, -1);
    }

    static LineDynamicAdjustSeparatorsFormatter create(final String tabsFreeContent, final int separatorLength,
            final int cellLengthLimit) {
        return new LineDynamicAdjustSeparatorsFormatter(Strings.repeat(" ", separatorLength),
                countColumnLengths(tabsFreeContent, cellLengthLimit));
    }

    @VisibleForTesting
    static List<Integer> countColumnLengths(final String tabsFreeContent, final int cellLengthLimit) {
        final List<Integer> columnLengths = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(tabsFreeContent))) {
            String line = reader.readLine();
            while (line != null) {
                updateCellLengths(line, columnLengths, cellLengthLimit);
                line = reader.readLine();
            }
        } catch (final IOException e) {
            // this won't happen since StringReader only throws IOException when trying to read
            // after closing; similarly BufferedReader; additionally BufferedReader throws when
            // wrapped reader is throwing
            throw new IllegalStateException();
        }
        return columnLengths;
    }

    private static void updateCellLengths(final String line, final List<Integer> columnsLength,
            final int cellLengthLimit) {
        int column = 0;
        for (final int length : getCellLengths(line)) {
            if (column == columnsLength.size()) {
                columnsLength.add(-1);
            }
            if (columnsLength.get(column) < length && (cellLengthLimit == -1 || length <= cellLengthLimit)) {
                columnsLength.set(column, length);
            }
            column++;
        }
    }

    private static int[] getCellLengths(final String line) {
        return CELL_SPLITTER.splitToList(line).stream().mapToInt(String::length).toArray();
    }

    private static final Splitter CELL_SPLITTER = Splitter.onPattern("\\s{2,}");

    private final String separator;

    private final List<Integer> columnLengths;

    @VisibleForTesting
    LineDynamicAdjustSeparatorsFormatter(final String separator, final List<Integer> columnLengths) {
        this.separator = separator;
        this.columnLengths = columnLengths;
    }

    @Override
    public String format(final String line) {
        final List<String> cells = CELL_SPLITTER.splitToList(line);

        final StringBuilder formatted = new StringBuilder();
        int column = 0;
        for (final String cell : cells) {
            if (column == cells.size() - 1) {
                formatted.append(cell);
            } else {
                if (column > 0 || !cell.isEmpty()) {
                    formatted.append(Strings.padEnd(cell, columnLengths.get(column), ' '));
                }
                formatted.append(separator);
            }
            column++;
        }
        return formatted.toString();
    }
}
