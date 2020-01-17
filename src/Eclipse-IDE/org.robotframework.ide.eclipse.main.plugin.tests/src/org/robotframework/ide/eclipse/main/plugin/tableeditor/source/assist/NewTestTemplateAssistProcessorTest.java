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
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFileContent;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RedTemplateContextType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class NewTestTemplateAssistProcessorTest {

    @Project
    static IProject project;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "suite.robot",
                "*** Settings ***",
                "setting",
                "*** Test Cases ***",
                "test",
                "  ",
                "  kw1  arg",
                "new other test",
                "");
    }

    @Test
    public void newTestTemplateProcessorIsValidForTestsSection() throws Exception {
        final NewTestTemplateAssistProcessor processor = new NewTestTemplateAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
    }

    @Test
    public void newTestTemplateProcessorHasTitleDefined() throws Exception {
        final NewTestTemplateAssistProcessor processor = new NewTestTemplateAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void newTestTemplateProcessorHasContextTypeDefined() throws Exception {
        final NewTestTemplateAssistProcessor processor = new NewTestTemplateAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        assertThat(processor.getContextTypeId()).isEqualTo(RedTemplateContextType.NEW_TEST_CONTEXT_TYPE);
    }

    @Test
    public void noProposalsAreProvided_whenNotInApplicableContentType() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(17)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewTestTemplateAssistProcessor processor = new NewTestTemplateAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 17);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_whenPrefixIsEmptyAndRegionOffsetIsSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(49)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewTestTemplateAssistProcessor processor = new NewTestTemplateAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 49);

        assertThat(proposals).hasSize(1).are(proposalWithImage(ImagesManager.getImage(RedImages.getTemplateImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer, 49, (TemplateProposal) proposal))
                .containsOnly(new Document("*** Settings ***",
                        "setting",
                        "*** Test Cases ***",
                        "test",
                        "Test",
                        "    [Documentation]",
                        "    [Tags]",
                        "    [Timeout]",
                        "    [Setup]",
                        "    [Teardown]  ",
                        "  kw1  arg",
                        "new other test",
                        ""));
    }

    @Test
    public void noProposalsAreProvided_whenPrefixIsEmptyAndRegionOffsetIsNotSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(51)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewTestTemplateAssistProcessor processor = new NewTestTemplateAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 51);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_whenPrefixIsNotEmptyAndRegionOffsetIsSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(65)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewTestTemplateAssistProcessor processor = new NewTestTemplateAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 65);

        assertThat(proposals).hasSize(1).are(proposalWithImage(ImagesManager.getImage(RedImages.getTemplateImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer, 65, (TemplateProposal) proposal))
                .containsOnly(new Document("*** Settings ***",
                        "setting",
                        "*** Test Cases ***",
                        "test",
                        "  ",
                        "  kw1  arg",
                        "Test",
                        "    [Documentation]",
                        "    [Tags]",
                        "    [Timeout]",
                        "    [Setup]",
                        "    [Teardown]w other test",
                        ""));
    }

    @Test
    public void noProposalsAreProvided_whenPrefixIsNotEmptyAndRegionOffsetIsNotSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(70)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final NewTestTemplateAssistProcessor processor = new NewTestTemplateAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 70);

        assertThat(proposals).isEmpty();
    }
}
