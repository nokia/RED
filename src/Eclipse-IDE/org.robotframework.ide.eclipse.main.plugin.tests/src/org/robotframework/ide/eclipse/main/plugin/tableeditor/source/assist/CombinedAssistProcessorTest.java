/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;

public class CombinedAssistProcessorTest {

    @Test
    public void combinedProcessorIsCalledSmart() {
        final CombinedAssistProcessor processor = new CombinedAssistProcessor();
        assertThat(processor.getProposalsTitle()).isEqualTo("Smart");
    }

    @Test
    public void combinedProcessorIsApplicableForAllRobotDocumentContentTypes() {
        final CombinedAssistProcessor processor = new CombinedAssistProcessor();
        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.LEGAL_CONTENT_TYPES);
    }

    @Test
    public void combinedProcessorDoesNotCareIfShouldShowProposalsIsCalled() throws Exception {
        // the shouldShowProposals method is inherited, but it is never called, because
        // computeProposals(ITextViewer, int) is overriden, so it simply return false
        final CombinedAssistProcessor processor = new CombinedAssistProcessor();

        final IDocument document = mock(IDocument.class);
        assertThat(processor.shouldShowProposals(document, 0, "content")).isFalse();

        verifyZeroInteractions(document);
    }

    @Test
    public void noProposalsAreProvidedUsingSpecializedComputeProposalsMethod() throws Exception {
        // the specialized computeProposals method is inherited, but it is never called, because
        // computeProposals(ITextViewer, int) is overriden, so it simply return null
        final CombinedAssistProcessor processor = new CombinedAssistProcessor();

        final IDocument document = mock(IDocument.class);
        assertThat(processor.computeProposals(document, 0, 5, "prefix", true)).isNull();

        verifyZeroInteractions(document);
    }

    @Test
    public void nullIsReturned_whenThereAreNoProcessors() {
        final CombinedAssistProcessor processor = new CombinedAssistProcessor();

        final ICompletionProposal[] proposals = processor.computeCompletionProposals(mock(ITextViewer.class), 0);
        assertThat(proposals).isNull();
    }

    @Test
    public void nullIsReturned_whenNestedProcessorsOnlyReturnsNullProposalsList() {
        final ICompletionProposal[] nullProposals = null;

        final CombinedAssistProcessor processor = new CombinedAssistProcessor(
                new MockProcessor(nullProposals),
                new MockProcessor(nullProposals));

        final ICompletionProposal[] proposals = processor.computeCompletionProposals(mock(ITextViewer.class), 0);
        assertThat(proposals).isNull();
    }

    @Test
    public void emptyProposalsListIsReturned_whenNestedProcessorsOnlyReturnsNullOrEmpty() {
        final ICompletionProposal[] nullProposals = null;
        final ICompletionProposal[] emptyProposals = new ICompletionProposal[0];

        final CombinedAssistProcessor processor = new CombinedAssistProcessor(
                new MockProcessor(nullProposals),
                new MockProcessor(emptyProposals),
                new MockProcessor(nullProposals),
                new MockProcessor(emptyProposals));

        final ICompletionProposal[] proposals = processor.computeCompletionProposals(mock(ITextViewer.class), 0);
        assertThat(proposals).isEmpty();
    }

    @Test
    public void combinedProposalsListIsReturned_whenNestedProcessorsReturnsProposals() {
        final ICompletionProposal[] emptyProposals = new ICompletionProposal[0];

        final ICompletionProposal proposal1 = mock(ICompletionProposal.class);
        final ICompletionProposal proposal2 = mock(ICompletionProposal.class);
        final ICompletionProposal proposal3 = mock(ICompletionProposal.class);
        final ICompletionProposal proposal4 = mock(ICompletionProposal.class);

        final CombinedAssistProcessor processor = new CombinedAssistProcessor(
                new MockProcessor(proposal1, proposal2),
                new MockProcessor(emptyProposals),
                new MockProcessor(proposal3, proposal4));

        final ICompletionProposal[] proposals = processor.computeCompletionProposals(mock(ITextViewer.class), 0);
        assertThat(proposals).containsExactly(proposal1, proposal2, proposal3, proposal4);
    }

    private static class MockProcessor extends RedContentAssistProcessor {

        private final List<? extends ICompletionProposal> proposalsToReturn;

        MockProcessor(final ICompletionProposal... proposalsToReturn) {
            super(null);
            this.proposalsToReturn = proposalsToReturn == null ? null : newArrayList(proposalsToReturn);
        }

        @Override
        protected List<String> getApplicableContentTypes() {
            return newArrayList();
        }

        @Override
        protected String getProposalsTitle() {
            return null;
        }

        @Override
        protected List<? extends ICompletionProposal> computeProposals(final ITextViewer viewer, final int offset) {
            return proposalsToReturn;
        }

        @Override
        protected List<? extends ICompletionProposal> computeProposals(final IDocument document, final int offset,
                final int cellLength, final String prefix, final boolean atTheEndOfLine) throws BadLocationException {
            return null;
        }

        @Override
        protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
                throws BadLocationException {
            return false;
        }
    }
}
