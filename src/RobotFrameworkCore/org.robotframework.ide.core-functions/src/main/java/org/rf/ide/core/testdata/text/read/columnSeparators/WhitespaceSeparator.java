/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testData.text.read.columnSeparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rf.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;


public class WhitespaceSeparator extends ALineSeparator {

    private static final SeparatorType TYPE = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
    public static final Pattern WHITESPACE_SEPARATOR = Pattern
            .compile("([ ]?\\t|[ ]{2,})+");
    private final Matcher matcher;


    public WhitespaceSeparator(final int lineNumber, final String line) {
        super(lineNumber, line);
        this.matcher = WHITESPACE_SEPARATOR.matcher(line);
    }


    @Override
    protected Separator nextSeparator() {
        final int start = matcher.start();
        final int end = matcher.end();

        final Separator s = new Separator();
        s.setType(SeparatorType.TABULATOR_OR_DOUBLE_SPACE);
        s.setStartColumn(start);
        s.setText(line.substring(start, end));
        s.setRaw(s.getText());
        s.setLineNumber(getLineNumber());

        return s;
    }


    @Override
    protected boolean hasNextSeparator() {
        return matcher.find();
    }


    @Override
    public SeparatorType getProducedType() {
        return TYPE;
    }
}
