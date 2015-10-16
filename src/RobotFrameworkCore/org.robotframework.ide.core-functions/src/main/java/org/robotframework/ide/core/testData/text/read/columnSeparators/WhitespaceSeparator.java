/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.columnSeparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.robotframework.ide.core.testData.text.read.columnSeparators.Separator.SeparatorType;


public class WhitespaceSeparator extends ALineSeparator {

    private static final SeparatorType TYPE = SeparatorType.TABULATOR_OR_DOUBLE_SPACE;
    public static final Pattern WHITESPACE_SEPARATOR = Pattern
            .compile("([ ]?\\t|[ ]{2,})+");
    private final Matcher matcher;


    public WhitespaceSeparator(int lineNumber, String line) {
        super(lineNumber, line);
        this.matcher = WHITESPACE_SEPARATOR.matcher(line);
    }


    @Override
    protected Separator nextSeparator() {
        int start = matcher.start();
        int end = matcher.end();

        Separator s = new Separator();
        s.setType(SeparatorType.TABULATOR_OR_DOUBLE_SPACE);
        s.setStartColumn(start);
        s.setText(new StringBuilder().append(line.substring(start, end)));
        s.setRaw(new StringBuilder(s.getText()));
        s.setLineNumber(getLineNumber());

        return s;
    }


    @Override
    protected boolean hasNextSeparator() {
        return matcher.find();
    }


    public SeparatorType getProducedType() {
        return TYPE;
    }
}
