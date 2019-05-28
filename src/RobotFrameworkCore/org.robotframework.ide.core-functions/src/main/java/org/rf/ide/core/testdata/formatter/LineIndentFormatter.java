/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import java.util.Set;

import com.google.common.base.Strings;

public class LineIndentFormatter implements ILineFormatter {

    private final Set<Integer> linesToIndent;

    private final String separator;

    public LineIndentFormatter(final Set<Integer> linesToIndent, final int separatorLength) {
        this.linesToIndent = linesToIndent;
        this.separator = Strings.repeat(" ", separatorLength);
    }

    @Override
    public String format(final int lineNumber, final String line) {
        return linesToIndent.contains(lineNumber) ? separator + line : line;
    }

}
