/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.formatter.IFormattingStrategyExtension;

/**
 * @author Michal Anglart
 *
 */
public class SuiteSourceFormattingStrategy implements IFormattingStrategy, IFormattingStrategyExtension {

    private final SuiteSourceEditorFormatter sourceFormatter;

    private IDocument documentToFormat;

    private IRegion regionToFormat;

    public SuiteSourceFormattingStrategy(final SuiteSourceEditorFormatter sourceFormatter) {
        this.sourceFormatter = sourceFormatter;
    }

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
                final String lastDelimiter = documentToFormat.getLineDelimiter(endLine);
                final int lastDelimiterLength = lastDelimiter != null ? lastDelimiter.length() : 0;

                regionToFormat = new Region(startLineBeginOffset,
                        endLineBeginOffset - startLineBeginOffset + lastDelimiterLength);
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
        sourceFormatter.format(documentToFormat, regionToFormat);
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
}
