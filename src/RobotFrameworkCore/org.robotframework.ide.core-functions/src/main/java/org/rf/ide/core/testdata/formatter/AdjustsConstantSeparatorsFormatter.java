/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import com.google.common.base.Strings;

public class AdjustsConstantSeparatorsFormatter implements ILineFormatter {

    private final String separator;

    public AdjustsConstantSeparatorsFormatter(final int separatorLength) {
        this.separator = Strings.repeat(" ", separatorLength);
    }

    @Override
    public String format(final String line) {
        return line.replaceAll("\\s{2,}", separator);
    }

}
