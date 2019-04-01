/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

@FunctionalInterface
public interface ILineFormatter {

    String format(String line);
}
