/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

class LineRightTrimFormatter implements ILineFormatter {

    @Override
    public String format(final String line) {
        return line.replaceFirst("\\s+$", "");
    }

}
