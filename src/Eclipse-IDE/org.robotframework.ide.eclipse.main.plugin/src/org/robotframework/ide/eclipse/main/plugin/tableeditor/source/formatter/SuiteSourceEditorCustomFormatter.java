/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import java.util.List;

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
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

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
        DocumentRewriteSession session = null;
        if (document instanceof IDocumentExtension4) {
            session = ((IDocumentExtension4) document).startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
        }

        try {
            final RangeSet<Integer> ranges = TreeRangeSet.create();
            lines.stream().map(line -> Range.closedOpen(line, line + 1)).forEach(ranges::add);

            for (final Range<Integer> range : ranges.asRanges()) {
                final int firstLine = range.lowerEndpoint();
                final int lastLine = range.upperEndpoint() - 1;

                final IRegion firstLineRegion = document.getLineInformation(firstLine);
                final IRegion lastLineRegion = document.getLineInformation(lastLine);
                
                final String lastDelimiter = document.getLineDelimiter(lastLine);
                final int lastDelimiterLength = lastDelimiter != null ? lastDelimiter.length() : 0;

                final IRegion regionToFormat = new Region(firstLineRegion.getOffset(), lastLineRegion.getOffset()
                        - firstLineRegion.getOffset() + lastLineRegion.getLength() + lastDelimiterLength);
                format(document, regionToFormat);
            }

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
        final String content = document.get(region.getOffset(), region.getLength());
        final RobotSourceFormatter robotFormatter = createFormatter(document, region);
        final String formattedContent = robotFormatter.format(content);
        if (!content.equals(formattedContent)) {
            document.replace(region.getOffset(), region.getLength(), formattedContent);
        }
    }

    private RobotSourceFormatter createFormatter(final IDocument document, final IRegion region)
            throws BadLocationException {
        final int regionLastLine = document.getLineOfOffset(region.getOffset() + region.getLength() - 1);
        final String lineDelimiter = Strings.nullToEmpty(document.getLineDelimiter(0));
        final boolean skipDelimiterInLastLine = Strings.isNullOrEmpty(document.getLineDelimiter(regionLastLine));
        return new RedFormatter(new RedFormatterSettings(lineDelimiter, skipDelimiterInLastLine));
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
