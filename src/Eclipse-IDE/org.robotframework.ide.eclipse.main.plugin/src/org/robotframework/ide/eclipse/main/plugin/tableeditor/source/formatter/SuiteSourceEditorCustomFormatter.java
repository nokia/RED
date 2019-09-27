/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import static java.util.stream.Collectors.toSet;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.rf.ide.core.testdata.formatter.RedFormatter;
import org.rf.ide.core.testdata.formatter.RedFormatter.FormatterSettings;
import org.rf.ide.core.testdata.formatter.RedFormatter.FormattingSeparatorType;
import org.rf.ide.core.testdata.formatter.RobotSourceFormatter;
import org.rf.ide.core.testdata.model.FilePosition;
import org.rf.ide.core.testdata.text.read.RobotLine;
import org.rf.ide.core.testdata.text.read.recognizer.RobotToken;
import org.rf.ide.core.testdata.text.read.recognizer.RobotTokenType;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RobotDocument;

import com.google.common.base.Strings;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public class SuiteSourceEditorCustomFormatter implements SourceDocumentFormatter {

    private final RedPreferences preferences;

    SuiteSourceEditorCustomFormatter(final RedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public void format(final IDocument document, final List<Integer> lines) throws BadLocationException {
        inRewriteSession(document, () -> {
            final RangeSet<Integer> ranges = TreeRangeSet.create();
            lines.stream().map(line -> Range.closedOpen(line, line + 1)).forEach(ranges::add);

            final Set<Integer> forBodyLines = forLoopLinesToIndent(document, new HashSet<>(lines));
            for (final Range<Integer> range : ranges.asRanges()) {
                final int firstLine = range.lowerEndpoint();
                final int lastLine = range.upperEndpoint() - 1;

                final IRegion firstLineRegion = document.getLineInformation(firstLine);
                final IRegion lastLineRegion = document.getLineInformation(lastLine);

                final String lastDelimiter = document.getLineDelimiter(lastLine);
                final int lastDelimiterLength = lastDelimiter != null ? lastDelimiter.length() : 0;

                final IRegion regionToFormat = new Region(firstLineRegion.getOffset(), lastLineRegion.getOffset()
                        - firstLineRegion.getOffset() + lastLineRegion.getLength() + lastDelimiterLength);
                formatSimple(document, regionToFormat,
                        forBodyLines.stream().map(line -> line - firstLine).collect(toSet()));
            }
        });
    }

    private static void inRewriteSession(final IDocument document, final DocumentRunnable operation)
            throws BadLocationException {
        DocumentRewriteSession session = null;
        try {
            if (document instanceof IDocumentExtension4) {
                session = ((IDocumentExtension4) document).startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
            }
            operation.run();

        } finally {
            if (session != null) {
                ((IDocumentExtension4) document).stopRewriteSession(session);
            }
        }
    }

    @Override
    public void format(final IDocument document) throws BadLocationException {
        format(document, new Region(0, document.getLength()));
    }

    @Override
    public void format(final IDocument document, final IRegion region) throws BadLocationException {
        final int firstLine = document.getLineOfOffset(region.getOffset());
        final int noOfLines = document.getNumberOfLines(region.getOffset(), region.getLength());
        final Set<Integer> affectedLines = IntStream.range(firstLine, firstLine + noOfLines).boxed().collect(toSet());

        final Set<Integer> forBodyLines = forLoopLinesToIndent(document, new HashSet<>(affectedLines)).stream()
                .map(line -> line - firstLine)
                .collect(toSet());
        formatSimple(document, region, forBodyLines);
    }

    private void formatSimple(final IDocument document, final IRegion region, final Set<Integer> forBodyLines)
            throws BadLocationException {
        final String content = document.get(region.getOffset(), region.getLength());
        final RobotSourceFormatter robotFormatter = createFormatter(document, region, forBodyLines);
        final String formattedContent = robotFormatter.format(content);
        if (!content.equals(formattedContent)) {
            document.replace(region.getOffset(), region.getLength(), formattedContent);
        }
    }

    private RobotSourceFormatter createFormatter(final IDocument document, final IRegion region,
            final Set<Integer> forBodyLines) throws BadLocationException {
        final int regionLastLine = document.getLineOfOffset(region.getOffset() + region.getLength() - 1);
        final String lineDelimiter = Strings.nullToEmpty(document.getLineDelimiter(0));
        final boolean skipDelimiterInLastLine = Strings.isNullOrEmpty(document.getLineDelimiter(regionLastLine));
        return new RedFormatter(new RedFormatterSettings(lineDelimiter, skipDelimiterInLastLine), forBodyLines);
    }

    private static Set<Integer> forLoopLinesToIndent(final IDocument document, final Set<Integer> affectedLines)
            throws BadLocationException {
        final Set<Integer> linesToIndent = new HashSet<>();
        if (!(document instanceof RobotDocument)) {
            return linesToIndent;
        }
        try {
            final List<RobotLine> lines = ((RobotDocument) document).getNewestModel().getFileContent();

            FilePosition forPosition = null;
            for (int line = 0; line < lines.size(); line++) {
                final RobotLine currentLine = lines.get(line);
                final Optional<RobotToken> firstToken = currentLine.elementsStream()
                        .filter(RobotToken.class::isInstance)
                        .map(RobotToken.class::cast)
                        .findFirst();

                if (firstToken.isPresent() && firstToken.get().getTypes().contains(RobotTokenType.FOR_WITH_END)) {
                    forPosition = firstToken.get().getFilePosition();

                } else if (firstToken.isPresent()
                        && firstToken.get().getTypes().contains(RobotTokenType.FOR_END_TOKEN)) {
                    forPosition = null;

                } else if (forPosition != null && firstToken.isPresent() && affectedLines.contains(line)
                        && firstToken.get().getFilePosition().getColumn() <= forPosition.getColumn()) {
                    linesToIndent.add(line);
                }
            }

        } catch (final InterruptedException e) {
        }
        return linesToIndent;
    }

    @FunctionalInterface
    private static interface DocumentRunnable {
        void run() throws BadLocationException;
    }

    private class RedFormatterSettings implements FormatterSettings {

        private final String delimiter;

        private final boolean skipDelimiterInLastLine;

        public RedFormatterSettings(final String delimiter, final boolean skipDelimiterInLastLine) {
            this.delimiter = delimiter;
            this.skipDelimiterInLastLine = skipDelimiterInLastLine;
        }

        @Override
        public String getLineDelimiter() {
            return delimiter;
        }

        @Override
        public boolean shouldSkipDelimiterInLastLine() {
            return skipDelimiterInLastLine;
        }

        @Override
        public boolean isSeparatorAdjustmentEnabled() {
            return preferences.isFormatterSeparatorAdjustmentEnabled();
        }

        @Override
        public FormattingSeparatorType getSeparatorType() {
            return preferences.getFormatterSeparatorType();
        }

        @Override
        public int getSeparatorLength() {
            return preferences.getFormatterSeparatorLength();
        }

        @Override
        public int getIgnoredCellLengthLimit() {
            return preferences.getFormatterIgnoredCellLengthLimit();
        }

        @Override
        public boolean isRightTrimEnabled() {
            return preferences.isFormatterRightTrimEnabled();
        }

    }
}
