/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;

public class RedContentAssistProcessorTest {

    @Test
    public void whenNullProposalsAreFound_nullIsReturnedAsProposalsArray() {
        final RedContentAssistProcessor processor = createProcessorFindingProposalsForContentTypes(null);

        assertThat(processor.computeCompletionProposals(mock(ITextViewer.class), 0)).isNull();
    }

    @Test
    public void whenNoProposalsAreFound_emptyArrayIsReturnedAsProposalsArray() {
        final RedContentAssistProcessor processor = createProcessorFindingProposalsForContentTypes(
                new ArrayList<ICompletionProposal>());

        assertThat(processor.computeCompletionProposals(mock(ITextViewer.class), 0)).isEmpty();
    }

    @Test
    public void whenProposalsAreFound_theyAreReturnedInArray() {
        final ICompletionProposal proposal1 = mock(ICompletionProposal.class);
        final ICompletionProposal proposal2 = mock(ICompletionProposal.class);
        final RedContentAssistProcessor processor = createProcessorFindingProposalsForContentTypes(
                newArrayList(proposal1, proposal2));

        assertThat(processor.computeCompletionProposals(mock(ITextViewer.class), 0)).containsExactly(proposal1,
                proposal2);
    }

    @Test
    public void trueIsReturnedForLocationQuery_whenLocatedInApplicableContentType() throws Exception {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1", "ct2");
        final int offset = 5;
        final IDocument document = spy(new Document("0123456789"));
        when(document.getContentType(offset)).thenReturn("ct1");

        assertThat(processor.isInApplicableContentType(document, offset)).isTrue();
    }

    @Test
    public void falseIsReturnedForLocationQuery_whenLocatedInNonApplicableContentType() throws Exception {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1", "ct2");
        final int offset = 5;
        final IDocument document = spy(new Document("0123456789"));
        when(document.getContentType(offset)).thenReturn("ct3");

        assertThat(processor.isInApplicableContentType(document, offset)).isFalse();
    }

    @Test
    public void trueIsReturnedForLocationQuery_whenLocatedAtTheEndOfDocumentInDefualtCTNeighboringApplicableCT()
            throws Exception {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1", "ct2");
        final int offset = 10;
        final IDocument document = spy(new Document("0123456789"));
        when(document.getContentType(offset - 1)).thenReturn("ct2");
        when(document.getContentType(offset)).thenReturn(IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(processor.isInApplicableContentType(document, offset)).isTrue();
    }

    @Test
    public void falseIsReturnedForLocationQuery_whenLocatedAtTheEndOfDocumentInDefualtCTNeighboringNonApplicableCT()
            throws Exception {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1", "ct2");
        final int offset = 10;
        final IDocument document = spy(new Document("0123456789"));
        when(document.getContentType(offset - 1)).thenReturn("ct3");
        when(document.getContentType(offset)).thenReturn(IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(processor.isInApplicableContentType(document, offset)).isFalse();
    }

    @Test
    public void falseIsReturnedForLocationQuery_whenLocatedAtTheEndOfEmptyDocumentInDefualtCT()
            throws Exception {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1", "ct2");
        final int offset = 0;
        final IDocument document = spy(new Document());
        when(document.getContentType(offset)).thenReturn(IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(processor.isInApplicableContentType(document, offset)).isFalse();
    }

    @Test
    public void virtualContentTypeIsTheContentTypeItself_whenDifferentThanDefaultOne() throws BadLocationException {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1");
        final int offset = 5;
        final IDocument document = spy(new Document("0123456789"));
        when(document.getContentType(offset)).thenReturn("someCt");

        assertThat(processor.getVirtualContentType(document, offset)).isEqualTo("someCt");
    }

    @Test
    public void virtualContentTypeIsTheContentTypeItself_whenLocatedJustAfterNonDefaultOne()
            throws BadLocationException {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1");
        final int offset = 5;
        final IDocument document = spy(new Document("0123456789"));
        when(document.getContentType(offset - 1)).thenReturn("someCt");
        when(document.getContentType(offset)).thenReturn(IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(processor.getVirtualContentType(document, offset)).isEqualTo(IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Test
    public void virtualContentTypeIsTheNeighboringContentTypeItself_whenLocatedJustAfterNonDefaultOneAtTheDocumentEnd()
            throws BadLocationException {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1");
        final int offset = 10;
        final IDocument document = spy(new Document("0123456789"));
        when(document.getContentType(offset - 1)).thenReturn("someCt");
        when(document.getContentType(offset)).thenReturn(IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(processor.getVirtualContentType(document, offset)).isEqualTo("someCt");
    }

    @Test
    public void virtualContentTypeIsTheContentTypeItself_whenHavingEmptyDocument() throws Exception {
        final RedContentAssistProcessor processor = createProcessorForContentTypes("ct1");
        final int offset = 0;
        final IDocument document = spy(new Document());
        when(document.getContentType(offset)).thenReturn(IDocument.DEFAULT_CONTENT_TYPE);

        assertThat(processor.getVirtualContentType(document, offset)).isEqualTo(IDocument.DEFAULT_CONTENT_TYPE);
    }

    private static RedContentAssistProcessor createProcessorForContentTypes(final String... applicableContentTypes) {
        return createProcessor(newArrayList(applicableContentTypes), null);
    }

    private static RedContentAssistProcessor createProcessorFindingProposalsForContentTypes(
            final List<? extends ICompletionProposal> proposalsToFind) {
        return createProcessor(null, proposalsToFind);
    }

    private static RedContentAssistProcessor createProcessor(final List<String> applicableContentTypes,
            final List<? extends ICompletionProposal> foundProposals) {
        return new RedContentAssistProcessor() {

            @Override
            protected String getProposalsTitle() {
                return "Mock processor";
            }

            @Override
            protected List<String> getApplicableContentTypes() {
                return applicableContentTypes;
            }

            @Override
            protected List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
                return foundProposals;
            }
        };
    }
}
