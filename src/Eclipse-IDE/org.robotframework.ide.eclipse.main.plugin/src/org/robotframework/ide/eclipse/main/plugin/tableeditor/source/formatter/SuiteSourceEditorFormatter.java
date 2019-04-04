/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.rf.ide.core.testdata.formatter.AdjustsConstantSeparatorsFormatter;
import org.rf.ide.core.testdata.formatter.AdjustsDynamicSeparatorsFormatter;
import org.rf.ide.core.testdata.formatter.ILineFormatter;
import org.rf.ide.core.testdata.formatter.ReplaceTabWithSpacesFormatter;
import org.rf.ide.core.testdata.formatter.RightTrimFormatter;
import org.rf.ide.core.testdata.formatter.RobotFormatter;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

public class SuiteSourceEditorFormatter {

    public void format(final IDocument document, final IRegion region) throws BadLocationException {
        final String content = document.get(region.getOffset(), region.getLength());
        final RobotFormatter robotFormatter = createFormatter(document, region);
        final String formattedContent = format(content, robotFormatter);
        if (!content.equals(formattedContent)) {
            document.replace(region.getOffset(), region.getLength(), formattedContent);
        }
    }

    private RobotFormatter createFormatter(final IDocument document, final IRegion region) throws BadLocationException {
        final int regionLastLine = document.getLineOfOffset(region.getOffset() + region.getLength() - 1);
        final String lineDelimiter = Strings.nullToEmpty(document.getLineDelimiter(0));
        final boolean skipDelimiterInLastLine = Strings.isNullOrEmpty(document.getLineDelimiter(regionLastLine));
        return new RobotFormatter(lineDelimiter, skipDelimiterInLastLine);
    }

    public void format(final IDocument document, final List<Integer> lines) throws BadLocationException {
        DocumentRewriteSession session = null;
        if (document instanceof IDocumentExtension4) {
            session = ((IDocumentExtension4) document).startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
        }

        try {
            for (int i = 0; i < lines.size(); i++) {
                final int line = lines.get(i);
                final IRegion region = document.getLineInformation(line);
                final String lastDelimiter = document.getLineDelimiter(line);
                final int lastDelimiterLength = lastDelimiter != null ? lastDelimiter.length() : 0;
                format(document, new Region(region.getOffset(), region.getLength() + lastDelimiterLength));
            }
        } finally {
            if (session != null) {
                ((IDocumentExtension4) document).stopRewriteSession(session);
            }
        }
    }

    @VisibleForTesting
    String format(final String content, final RobotFormatter formatter) {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        try {
            final List<ILineFormatter> lineFormatters = new ArrayList<>();
            String toFormat = content;

            if (preferences.isFormatterSeparatorAdjustmentEnabled()) {
                final int separatorLength = preferences.getFormatterSeparatorLength();
                toFormat = formatter.format(content, new ReplaceTabWithSpacesFormatter(separatorLength));
                switch (preferences.getFormatterSeparatorType()) {
                    case CONSTANT:
                        lineFormatters.add(new AdjustsConstantSeparatorsFormatter(separatorLength));
                        break;
                    case DYNAMIC:
                        final List<Integer> columnLengths = AdjustsDynamicSeparatorsFormatter
                                .countColumnLengths(toFormat);
                        lineFormatters.add(new AdjustsDynamicSeparatorsFormatter(separatorLength, columnLengths));
                        break;
                    default:
                        throw new IllegalStateException("Unrecognized formatting mode");
                }
            }

            if (preferences.isFormatterRightTrimEnabled()) {
                lineFormatters.add(new RightTrimFormatter());
            }

            return formatter.format(toFormat, lineFormatters);
        } catch (final IOException e) {
            return content;
        }
    }

    public enum FormattingSeparatorType {
        CONSTANT,
        DYNAMIC
    }
}
