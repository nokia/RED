/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.columnSeparators;

import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;


public abstract class ALineSeparator {

    protected final int lineNumber;
    protected final String line;


    protected ALineSeparator(final int lineNumber, final String line) {
        this.lineNumber = lineNumber;
        this.line = line;
    }


    public abstract Separator next();


    public abstract boolean hasNext();


    public abstract SeparatorType getProducedType();


    public int getLineNumber() {
        return lineNumber;
    }


    public String getLine() {
        return line;
    }
}
