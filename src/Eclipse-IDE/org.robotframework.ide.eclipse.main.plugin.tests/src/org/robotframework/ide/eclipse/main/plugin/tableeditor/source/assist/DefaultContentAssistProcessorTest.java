/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;

public class DefaultContentAssistProcessorTest {

    @Test
    public void noAutoActivationCharsAreDefined() {
        final DefaultContentAssistProcessor processor = createProcessor();
        assertThat(processor.getCompletionProposalAutoActivationCharacters()).isNull();
        assertThat(processor.getContextInformationAutoActivationCharacters()).isNull();
    }

    @Test
    public void noErrorMessageByDefault() {
        final DefaultContentAssistProcessor processor = createProcessor();
        assertThat(processor.getErrorMessage()).isNull();
    }

    @Test
    public void defaultValidatorIsDefined() {
        final DefaultContentAssistProcessor processor = createProcessor();
        assertThat(processor.getContextInformationValidator())
                .isInstanceOf(SuiteSourceContextInformationValidator.class);
    }

    @Test
    public void thereAreNoContextInformations() {
        final DefaultContentAssistProcessor processor = createProcessor();

        final ITextViewer viewer = mock(ITextViewer.class);
        assertThat(processor.computeContextInformation(viewer, 10)).isEmpty();

        verifyNoMoreInteractions(viewer);
    }

    private DefaultContentAssistProcessor createProcessor() {
        return new DefaultContentAssistProcessor() {
            @Override
            public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int offset) {
                return null;
            }
        };
    }
}
