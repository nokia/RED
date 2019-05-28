/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

@FunctionalInterface
interface ILineFormatter {

    String format(int lineNumber, String line);

    default String format(final String line) {
        return format(0, line);
    }
}
