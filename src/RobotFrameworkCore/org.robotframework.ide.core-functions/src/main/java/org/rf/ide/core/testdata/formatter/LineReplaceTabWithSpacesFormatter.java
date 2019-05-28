/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import com.google.common.base.Strings;

class LineReplaceTabWithSpacesFormatter implements ILineFormatter {

    private final String separator;

    LineReplaceTabWithSpacesFormatter(final int separatorLength) {
        this.separator = Strings.repeat(" ", separatorLength);
    }

    @Override
    public String format(final int lineNumber, final String line) {
        return line.replaceAll("\t", separator);
    }

}
