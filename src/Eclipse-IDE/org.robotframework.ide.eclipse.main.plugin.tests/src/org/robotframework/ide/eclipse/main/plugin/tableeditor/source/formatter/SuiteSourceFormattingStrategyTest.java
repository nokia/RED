/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.formatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.formatter.FormattingContext;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;

public class SuiteSourceFormattingStrategyTest {

    @Test
    public void nothingHappens_whenStringIsFormatted() throws Exception {
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);
        final SuiteSourceFormattingStrategy strategy = new SuiteSourceFormattingStrategy(formatter);

        assertThat(strategy.format("", false, "", null)).isNull();
        verifyZeroInteractions(formatter);
    }

    @Test
    public void wholeDocumentIsFormatted_whenContextDocumentPropertyIsSetToTrue() throws Exception {
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);
        final IDocument document = new Document("line1", "line2", "line3");

        final FormattingContext context = new FormattingContext();
        context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, true);
        context.setProperty(FormattingContextProperties.CONTEXT_MEDIUM, document);

        format(formatter, context);

        verify(formatter).format(document, new Region(0, document.getLength()));
        verifyNoMoreInteractions(formatter);
    }

    @Test
    public void singleRegionLineIsFormatted_whenContextDocumentPropertyIsSetToFalse() throws Exception {
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);
        final IDocument document = new Document("line1", "line2", "line3");

        final FormattingContext context = new FormattingContext();
        context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, false);
        context.setProperty(FormattingContextProperties.CONTEXT_MEDIUM, document);
        context.setProperty(FormattingContextProperties.CONTEXT_REGION, new Region(13, 2));

        format(formatter, context);

        verify(formatter).format(document, new Region(12, 5));
        verifyNoMoreInteractions(formatter);
    }

    @Test
    public void multipleRegionLinesAreFormatted_whenContextDocumentPropertyIsSetToFalse() throws Exception {
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);
        final IDocument document = new Document("line1", "line2", "line3");

        final FormattingContext context = new FormattingContext();
        context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, false);
        context.setProperty(FormattingContextProperties.CONTEXT_MEDIUM, document);
        context.setProperty(FormattingContextProperties.CONTEXT_REGION, new Region(9, 5));

        format(formatter, context);

        verify(formatter).format(document, new Region(6, 11));
        verifyNoMoreInteractions(formatter);
    }

    @Test
    public void wholeDocumentIsFormatted_whenContextDocumentPropertyIsSetToFalseAndDocumentHasOneLine()
            throws Exception {
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);
        final IDocument document = new Document("singleLine");

        final FormattingContext context = new FormattingContext();
        context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, false);
        context.setProperty(FormattingContextProperties.CONTEXT_MEDIUM, document);
        context.setProperty(FormattingContextProperties.CONTEXT_REGION, new Region(3, 2));

        format(formatter, context);

        verify(formatter).format(document, new Region(0, document.getLength()));
        verifyNoMoreInteractions(formatter);
    }

    @Test
    public void wholeDocumentIsFormatted_whenContextDocumentPropertyIsSetToFalseAndIncorrectRegionIsSet()
            throws Exception {
        final SuiteSourceEditorFormatter formatter = mock(SuiteSourceEditorFormatter.class);
        final IDocument document = new Document("line1", "line2", "line3");

        final FormattingContext context = new FormattingContext();
        context.setProperty(FormattingContextProperties.CONTEXT_DOCUMENT, false);
        context.setProperty(FormattingContextProperties.CONTEXT_MEDIUM, document);
        context.setProperty(FormattingContextProperties.CONTEXT_REGION, new Region(document.getLength() - 2, 5));

        format(formatter, context);

        verify(formatter).format(document, new Region(0, document.getLength()));
        verifyNoMoreInteractions(formatter);
    }

    private void format(final SuiteSourceEditorFormatter formatter, final FormattingContext context) {
        final SuiteSourceFormattingStrategy strategy = new SuiteSourceFormattingStrategy(formatter);
        strategy.formatterStarts(context);
        strategy.format();
        strategy.formatterStops();
    }

}
