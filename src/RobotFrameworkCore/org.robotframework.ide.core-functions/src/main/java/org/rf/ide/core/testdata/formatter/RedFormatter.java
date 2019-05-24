/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.formatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;

public class RedFormatter implements RobotSourceFormatter {

    private final FormatterSettings settings;


    public RedFormatter(final FormatterSettings settings) {
        this.settings = settings;
    }

    @Override
    public String format(final String content) {
        try {
            final List<ILineFormatter> lineFormatters = new ArrayList<>();
            String toFormat = content;

            if (settings.isSeparatorAdjustmentEnabled()) {
                final int separatorLength = settings.getSeparatorLength();
                toFormat = format(content, new LineReplaceTabWithSpacesFormatter(separatorLength));
                switch (settings.getSeparatorType()) {
                    case CONSTANT:
                        lineFormatters.add(new LineConstantAdjustSeparatorsFormatter(separatorLength));
                        break;
                    case DYNAMIC:
                        final int cellLengthLimit = settings.getIgnoredCellLengthLimit();
                        lineFormatters.add(LineDynamicAdjustSeparatorsFormatter.create(toFormat, separatorLength,
                                cellLengthLimit));
                        break;
                    default:
                        throw new IllegalStateException("Unrecognized formatting mode");
                }
            }

            if (settings.isRightTrimEnabled()) {
                lineFormatters.add(new LineRightTrimFormatter());
            }

            return format(toFormat, lineFormatters);
        } catch (final IOException e) {
            return content;
        }
    }

    @VisibleForTesting
    String format(final String content, final ILineFormatter... lineFormatters) throws IOException {
        return format(content, Arrays.asList(lineFormatters));
    }

    @VisibleForTesting
    String format(final String content, final List<ILineFormatter> lineFormatters) throws IOException {
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
                line = reader.readLine();
                if (line != null || !settings.shouldSkipDelimiterInLastLine()) {
                    formattedContent.append(settings.getLineDelimiter());
                }
            }
        }
        return formattedContent.toString();
    }

    public enum FormattingSeparatorType {
        CONSTANT,
        DYNAMIC
    }

    public static interface FormatterSettings {

        String getLineDelimiter();

        boolean shouldSkipDelimiterInLastLine();

        boolean isSeparatorAdjustmentEnabled();

        FormattingSeparatorType getSeparatorType();

        int getSeparatorLength();

        int getIgnoredCellLengthLimit();

        boolean isRightTrimEnabled();
    }
}
