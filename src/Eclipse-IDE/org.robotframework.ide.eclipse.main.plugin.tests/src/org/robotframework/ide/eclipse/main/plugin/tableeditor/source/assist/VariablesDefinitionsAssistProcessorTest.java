/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder.AcceptanceMode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;

public class VariablesDefinitionsAssistProcessorTest {

    @Test
    public void varDefsProcessorIsValidOnlyForVariablesSection() {
        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        assertThat(processor.getApplicableContentTypes()).containsExactly(SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Test
    public void varDefsProcessorHasTitleDefined() {
        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenUserIsInNonVariableSection() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1", "line2"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(6)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 6);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenIsInVariableSectionButNotInTheFirstCell() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1  cell", "line2"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(7)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 7);

        assertThat(proposals).isNull();
    }

    private static SuiteSourceAssistantContext createAssitant() {
        final RobotSuiteFile file = mock(RobotSuiteFile.class);
        when(file.getFileExtension()).thenReturn("robot");
        return new SuiteSourceAssistantContext(file, new AssistPreferences(AcceptanceMode.INSERT, true, "  "));
    }

}
