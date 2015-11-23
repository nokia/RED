/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.columnSeparators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rf.ide.core.testdata.text.read.columnSeparators.Separator.SeparatorType;


public class PipeSeparator extends ALineSeparator {

    private static final SeparatorType TYPE = SeparatorType.PIPE;
    private static final String REGEXP_DO_NOT_CONSUME_WHEN_WHITESPACE_SHOULD_BELONGS_TO_NEXT_PIPE = "((?=\\s+[|])|\\s+)";
    private static final Pattern PIPE_SEPARATOR = Pattern.compile("(^[ ]?[|]"
            + REGEXP_DO_NOT_CONSUME_WHEN_WHITESPACE_SHOULD_BELONGS_TO_NEXT_PIPE
            + ")|" + "(\\s+[|]"
            + REGEXP_DO_NOT_CONSUME_WHEN_WHITESPACE_SHOULD_BELONGS_TO_NEXT_PIPE
            + ")|" + "((\\s)+[|](\\s)+$)|" + "((\\s)+[|]$)|" + "(^[|]$)");

    private final Matcher matcher;


    public PipeSeparator(final int lineNumber, final String line) {
        super(lineNumber, line);
        this.matcher = PIPE_SEPARATOR.matcher(line);
    }


    @Override
    protected Separator nextSeparator() {
        final int start = matcher.start();
        final int end = matcher.end();
        final Separator s = new Separator();
        s.setType(TYPE);
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
