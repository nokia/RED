/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData.text.read.columnSeparators;

import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;


public class TokenSeparatorBuilder {

    private static final Pattern PIPE_SEPARATOR_BEGIN = Pattern
            .compile("(^[|]\\s+)|(^[|]$)");


    public ALineSeparator createSeparator(final int lineNumber,
            final String line) {
        ALineSeparator thisLineSeparator = new WhitespaceSeparator(lineNumber,
                line);
        if (isPipeSeparated(line)) {
            thisLineSeparator = new PipeSeparator(lineNumber, line);
        }

        return thisLineSeparator;
    }


    @VisibleForTesting
    protected boolean isPipeSeparated(final String line) {
        return PIPE_SEPARATOR_BEGIN.matcher(line).find();
    }
}
