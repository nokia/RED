/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.formatter.IFormattingStrategyExtension;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;

/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceFormattingStrategy implements IFormattingStrategy, IFormattingStrategyExtension {

    private IDocument documentToFormat;

    private Region regionToFormat;

    @Override
    public void formatterStarts(final IFormattingContext context) {
        final Boolean formatWholeDocument = (Boolean) context.getProperty(FormattingContextProperties.CONTEXT_DOCUMENT);
        documentToFormat = (IDocument) context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM);
        if (formatWholeDocument) {
            regionToFormat = new Region(0, documentToFormat.getLength());
        } else {
            try {
                final IRegion region = (IRegion) context.getProperty(FormattingContextProperties.CONTEXT_REGION);

                final int startLine = documentToFormat.getLineOfOffset(region.getOffset());
                final int endLine = documentToFormat.getLineOfOffset(region.getOffset() + region.getLength());

                final int startLineBeginOffset = documentToFormat.getLineInformation(startLine).getOffset();
                final IRegion endLineRegion = documentToFormat.getLineInformation(endLine);
                final int endLineBeginOffset = endLineRegion.getOffset() + endLineRegion.getLength();
                final String lastDelimiter = documentToFormat.getLineDelimiter(startLine);

                regionToFormat = new Region(startLineBeginOffset,
                        endLineBeginOffset - startLineBeginOffset + lastDelimiter.length());
            } catch (final BadLocationException e) {
                regionToFormat = new Region(0, documentToFormat.getLength());
            }
        }
    }

    @Override
    public void formatterStarts(final String initialIndentation) {
        // nothing to do here
    }

    @Override
    public void format() {
        try {
            final int length = Math.min(documentToFormat.getLength(), regionToFormat.getLength());
            final String content = documentToFormat.get(regionToFormat.getOffset(), length);
            final String formattedText = format(content, documentToFormat.getLineDelimiter(0), getProperties());
            documentToFormat.replace(regionToFormat.getOffset(), length, formattedText);
        } catch (final BadLocationException e) {
            e.printStackTrace();
        }
    }

    private FormatterProperties getProperties() {
        return new FormatterProperties(SeparatorType.DYNAMIC, 4);
    }

    @VisibleForTesting
    String format(final String content, final String delimiter, final FormatterProperties properties) {
        switch (properties.separator) {
            case CONSTANT:
                return formatWithConstantSeparator(content, delimiter, properties);
            case DYNAMIC:
                return formatWithDynamicSeparator(content, delimiter, properties);
            default:
                throw new IllegalStateException("Unrecognized formatting mode");
        }
    }

    private String formatWithConstantSeparator(final String content, final String delimiter,
            final FormatterProperties properties) {
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            final StringBuilder formattedContent = new StringBuilder();

            String line = reader.readLine();
            while (line != null) {
                formattedContent.append(formatLineWithConstantSeparator(line, properties.separatorLength));
                formattedContent.append(delimiter);
                line = reader.readLine();
            }
            return formattedContent.toString();
        } catch (final IOException e) {
            return content;
        }
    }

    private String formatLineWithConstantSeparator(final String line, final int separatorLength) {
        final String lineWithoutTabs = line.replaceAll("\t", "  ");
        final String constantlySeparatedLine = lineWithoutTabs.replaceAll("  +", Strings.repeat(" ", separatorLength));
        final String withTrimmedEnd = constantlySeparatedLine.replaceFirst(" +$", "");
        return withTrimmedEnd;
    }

    private String formatWithDynamicSeparator(final String content, final String delimiter, final FormatterProperties properties) {
        String tabsFreeContent;
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            final StringBuilder contentWithoutTabs = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                contentWithoutTabs.append(line.replaceAll("\t", "  "));
                contentWithoutTabs.append(delimiter);
                line = reader.readLine();
            }
            tabsFreeContent = contentWithoutTabs.toString();
        } catch (final IOException e) {
            return content;
        }

        final Map<Integer, Integer> columnsLength = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new StringReader(tabsFreeContent))) {
            String line = reader.readLine();
            while (line != null) {
                updateCellLengths(line, columnsLength);

                line = reader.readLine();
            }
        } catch (final IOException e) {
            return content;
        }

        try (BufferedReader reader = new BufferedReader(new StringReader(tabsFreeContent))) {
            final StringBuilder formattedContent = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                int column = 0;
                final StringBuilder formattedLine = new StringBuilder();
                for (final String cell : Splitter.onPattern("  +").splitToList(line)) {
                    formattedLine.append(Strings.padEnd(cell, columnsLength.get(column), ' '));
                    formattedLine.append(Strings.repeat(" ", properties.separatorLength));
                    column++;
                }
                formattedContent.append(formattedLine.toString().replaceAll(" +$", ""));
                formattedContent.append(delimiter);

                line = reader.readLine();
            }
            return formattedContent.toString();

        } catch (final IOException e) {
            return content;
        }
    }

    private void updateCellLengths(final String line, final Map<Integer, Integer> columnsLength) {
        int column = 0;
        for (final Integer length : getCellLengths(line)) {
            final Integer maxLength = columnsLength.get(column);
            if (maxLength == null || maxLength.intValue() < length.intValue()) {
                columnsLength.put(column, length);
            }
            column++;
        }

    }

    private List<Integer> getCellLengths(final String line) {
        final List<String> cells = Splitter.onPattern("  +").splitToList(line);
        return cells.stream().map(String::length).collect(Collectors.toList());
    }

    @Override
    public String format(final String content, final boolean isLineStart, final String indentation,
            final int[] positions) {
        // nothing to do here
        return null;
    }

    @Override
    public void formatterStops() {
        documentToFormat = null;
        regionToFormat = null;
    }

    static class FormatterProperties {

        private final SeparatorType separator;

        private final int separatorLength;

        public FormatterProperties(final SeparatorType separatorType, final int separatorLength) {
            this.separator = separatorType;
            this.separatorLength = separatorLength;
        }
    }

    enum SeparatorType {
        DYNAMIC,
        CONSTANT
    }
}
