/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;

public class CombinedAssistProcessorTest {

    @Test
    public void nullIsReturnedWhenThereAreNoProcessors() {
        final CombinedAssistProcessor combinedProcessor = new CombinedAssistProcessor();
        
        final ICompletionProposal[] proposals = combinedProcessor.computeCompletionProposals(mock(ITextViewer.class),
                0);
        assertThat(proposals).isNull();
    }

    private static class MockProcessor extends RedContentAssistProcessor {

        private final List<? extends ICompletionProposal> proposalsToReturn;

        MockProcessor(final ICompletionProposal... proposalsToReturn) {
            this.proposalsToReturn = newArrayList(proposalsToReturn);
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
    }
}
