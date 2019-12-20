/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;


import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RedTemplateContextType;

public class RedTemplateAssistProcessorTest {

    @Test
    public void processorReturns_itsContextType() {
        final AssistantContext assistantContext = mock(AssistantContext.class);
        when(assistantContext.isTsvFile()).thenReturn(false);
        final RedTemplateAssistProcessor processor = new DumbRedTemplateAssistProcessor(assistantContext);

        assertThat(processor.getContextType(null, null).getName()).isEqualTo("New test");
    }

    @Test
    public void processorReturns_itsTemplates() {
        final AssistantContext assistantContext = mock(AssistantContext.class);
        when(assistantContext.isTsvFile()).thenReturn(false);
        final RedTemplateAssistProcessor processor = new DumbRedTemplateAssistProcessor(assistantContext);

        assertThat(processor.getTemplates(processor.getContextTypeId())).isNotEmpty();
    }

    @Test
    public void processorReturns_itsImage() {
        final AssistantContext assistantContext = mock(AssistantContext.class);
        when(assistantContext.isTsvFile()).thenReturn(false);
        final RedTemplateAssistProcessor processor = new DumbRedTemplateAssistProcessor(assistantContext);

        assertThat(processor.getImage(processor.getTemplates(processor.getContextTypeId())[0])).isNotNull();
    }

    @Test
    public void processorRecognizes_whenIsInApplicableContent() throws BadLocationException {
        final AssistantContext assistantContext = mock(AssistantContext.class);
        when(assistantContext.isTsvFile()).thenReturn(false);
        final RedTemplateAssistProcessor processor = new DumbRedTemplateAssistProcessor(assistantContext);
        final Document doc = spy(new Document());
        when(doc.getContentType(0)).thenReturn("correct_content");

        assertThat(processor.isInApplicableContentType(doc, 0)).isTrue();
        assertThat(processor.isInApplicableContentType(doc, 12)).isFalse();
    }

}

class DumbRedTemplateAssistProcessor extends RedTemplateAssistProcessor {

    public DumbRedTemplateAssistProcessor(final AssistantContext assistantContext) {
        super(assistantContext);
    }

    @Override
    public String getProposalsTitle() {
        return "Dumb proposals title";
    }

    @Override
    public List<String> getApplicableContentTypes() {
        return newArrayList("correct_content");
    }

    @Override
    protected String getContextTypeId() {
        return RedTemplateContextType.NEW_TEST_CONTEXT_TYPE;
    }

    @Override
    protected boolean shouldShowProposals(final IDocument document, final int offset, final String lineContent)
            throws BadLocationException {
        return true;
    }

}
