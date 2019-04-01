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
import java.util.stream.Collectors;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

public class AdjustsDynamicSeparatorsFormatter implements ILineFormatter {

    private static final Splitter CELL_SPLITTER = Splitter.onPattern("\\s{2,}");

    private final String separator;

    private final List<Integer> columnLengths;

    public AdjustsDynamicSeparatorsFormatter(final int separatorLength, final List<Integer> columnLengths) {
        this.separator = Strings.repeat(" ", separatorLength);
        this.columnLengths = columnLengths;
    }

    @Override
    public String format(final String line) {
        int column = 0;
        final StringBuilder formatted = new StringBuilder();
        for (final String cell : CELL_SPLITTER.splitToList(line)) {
            formatted.append(Strings.padEnd(cell, columnLengths.get(column), ' '));
            formatted.append(separator);
            column++;
        }
        return formatted.toString();
    }

    public static List<Integer> countColumnLengths(final String tabsFreeContent) throws IOException {
        final List<Integer> columnLengths = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(tabsFreeContent))) {
            String line = reader.readLine();
            while (line != null) {
                updateCellLengths(line, columnLengths);
                line = reader.readLine();
            }
        }
        return columnLengths;
    }

    private static void updateCellLengths(final String line, final List<Integer> columnsLength) {
        int column = 0;
        for (final Integer length : getCellLengths(line)) {
            if (column == columnsLength.size()) {
                columnsLength.add(length);
            } else if (columnsLength.get(column) < length) {
                columnsLength.set(column, length);
            }
            column++;
        }
    }

    private static List<Integer> getCellLengths(final String line) {
        return CELL_SPLITTER.splitToList(line).stream().map(String::length).collect(Collectors.toList());
    }

}
