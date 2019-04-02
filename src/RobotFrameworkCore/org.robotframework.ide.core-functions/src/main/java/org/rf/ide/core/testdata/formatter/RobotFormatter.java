/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Strings;

public class RobotFormatter {

    private final String lineDelimiter;

    public RobotFormatter(final String lineDelimiter) {
        this.lineDelimiter = Strings.nullToEmpty(lineDelimiter);
    }

    public String format(final String content, final ILineFormatter... lineFormatters) throws IOException {
        return format(content, Arrays.asList(lineFormatters));
    }

    public String format(final String content, final List<ILineFormatter> lineFormatters) throws IOException {
        if (lineFormatters.isEmpty()) {
            return content;
        }

        final StringBuilder formattedContent = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            String line = reader.readLine();
            while (line != null) {
                for (final ILineFormatter formatter : lineFormatters) {
                    line = formatter.format(line);
                }
                formattedContent.append(line);
                formattedContent.append(lineDelimiter);
                line = reader.readLine();
            }
        }
        return formattedContent.toString();
    }

}
