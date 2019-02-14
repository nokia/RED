/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.text.read.separators;

import java.util.regex.Pattern;

import org.rf.ide.core.testdata.model.FileFormat;

import com.google.common.annotations.VisibleForTesting;

public class TokenSeparatorBuilder {

    private static final Pattern PIPE_SEPARATOR_BEGIN = Pattern.compile("(^[|]\\s+)|(^[|]$)");

    private final FileFormat format;

    public TokenSeparatorBuilder(final FileFormat fileFormat) {
        this.format = fileFormat;
    }

    public ALineSeparator createSeparator(final int lineNumber, final String line) {
        if (format == FileFormat.TXT_OR_ROBOT) {
            return isPipeSeparated(line) ? new PipeSeparator(lineNumber, line)
                    : new WhitespaceSeparator(lineNumber, line);

        } else if (format == FileFormat.TSV) {
            return new StrictTsvTabulatorSeparator(lineNumber, line);

        } else {
            return null;
        }
    }

    @VisibleForTesting
    boolean isPipeSeparated(final String line) {
        return format == FileFormat.TXT_OR_ROBOT && PIPE_SEPARATOR_BEGIN.matcher(line).find();
    }
}
