/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Assistant.createAssistant;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.applyToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RedTemplateContextType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

public class NewKeywordTemplateAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(NewKeywordTemplateAssistProcessorTest.class);

    private static IFile suite;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        suite = projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "setting",
                "*** Keywords ***",
                "keyword",
                "  ",
                "  kw1  arg",
                "new other kw",
                "");
    }

    @Test
    public void newKeywordTemplateProcessorIsValidOnlyForKeywordsSection() throws Exception {
        final NewKeywordTemplateAssistProcessor processor = new NewKeywordTemplateAssistProcessor(
                createAssistant(suite));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.KEYWORDS_SECTION);
    }

    @Test
    public void newKeywordTemplateProcessorHasTitleDefined() throws Exception {
        final NewKeywordTemplateAssistProcessor processor = new NewKeywordTemplateAssistProcessor(
                createAssistant(suite));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void newKeywordTemplateProcessorHasContextTypeDefined() throws Exception {
        final NewKeywordTemplateAssistProcessor processor = new NewKeywordTemplateAssistProcessor(
                createAssistant(suite));
        assertThat(processor.getContextTypeId()).isEqualTo(RedTemplateContextType.NEW_KEYWORD_CONTEXT_TYPE);
    }

    @Test
    public void noProposalsAreProvided_whenNotInApplicableContentType() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(17)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewKeywordTemplateAssistProcessor processor = new NewKeywordTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 17);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_whenPrefixIsEmptyAndRegionOffsetIsSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(50)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewKeywordTemplateAssistProcessor processor = new NewKeywordTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 50);

        assertThat(proposals).hasSize(1).are(proposalWithImage(ImagesManager.getImage(RedImages.getTemplateImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer, 50, (TemplateProposal) proposal))
                .containsOnly(new Document(
                        "*** Settings ***",
                        "setting",
                        "*** Keywords ***",
                        "keyword",
                        "Keyword",
                        "    [Documentation]",
                        "    [Tags]",
                        "    [Arguments]",
                        "    [Timeout]",
                        "    [Teardown]",
                        "    [Return]  ",
                        "  kw1  arg",
                        "new other kw",
                        ""));
    }

    @Test
    public void noProposalsAreProvided_whenPrefixIsEmptyAndRegionOffsetIsNotSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(52)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewKeywordTemplateAssistProcessor processor = new NewKeywordTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 52);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_whenPrefixIsNotEmptyAndRegionOffsetIsSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(66)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewKeywordTemplateAssistProcessor processor = new NewKeywordTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 66);

        assertThat(proposals).hasSize(1).are(proposalWithImage(ImagesManager.getImage(RedImages.getTemplateImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer, 66, (TemplateProposal) proposal))
                .containsOnly(new Document(
                        "*** Settings ***",
                        "setting",
                        "*** Keywords ***",
                        "keyword",
                        "  ",
                        "  kw1  arg",
                        "Keyword",
                        "    [Documentation]",
                        "    [Tags]",
                        "    [Arguments]",
                        "    [Timeout]",
                        "    [Teardown]",
                        "    [Return]w other kw",
                        ""));
    }

    @Test
    public void noProposalsAreProvided_whenPrefixIsNotEmptyAndRegionOffsetIsNotSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(62)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewKeywordTemplateAssistProcessor processor = new NewKeywordTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 62);

        assertThat(proposals).isEmpty();
    }
}
