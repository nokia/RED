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
import static org.mockito.Mockito.withSettings;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Assistant.createAssistant;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.applyToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.RedTemplateContextType;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

public class KeywordCallTemplateAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(KeywordCallTemplateAssistProcessorTest.class);

    private static IFile suite;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        suite = projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "setting",
                "*** Test Cases ***",
                "test",
                "  ",
                "  kw1  arg",
                "new other test",
                "  formal keyword",
                "");
    }

    @Test
    public void keywordCallTemplateProcessorIsValidForRobotSections() throws Exception {
        final KeywordCallTemplateAssistProcessor processor = new KeywordCallTemplateAssistProcessor(
                createAssistant(suite));

        assertThat(processor.getApplicableContentTypes()).containsExactly(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION,
                SuiteSourcePartitionScanner.SETTINGS_SECTION, SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Test
    public void keywordCallTemplateProcessorHasTitleDefined() throws Exception {
        final KeywordCallTemplateAssistProcessor processor = new KeywordCallTemplateAssistProcessor(
                createAssistant(suite));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void keywordCallTemplateProcessorHasContextTypeDefined() throws Exception {
        final KeywordCallTemplateAssistProcessor processor = new KeywordCallTemplateAssistProcessor(
                createAssistant(suite));
        assertThat(processor.getContextTypeId()).isEqualTo(RedTemplateContextType.KEYWORD_CALL_CONTEXT_TYPE);
    }

    @Test
    public void noProposalsAreProvided_whenPrefixIsEmptyAndRegionOffsetIsSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(49)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final KeywordCallTemplateAssistProcessor processor = new KeywordCallTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 49);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_whenPrefixIsEmptyAndRegionOffsetIsNotSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class,
                withSettings().extraInterfaces(ITextViewerExtension.class, ITextViewerExtension2.class,
                        IPostSelectionProvider.class));
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));
        final StyledText widget = mock(StyledText.class);
        final Shell shell = mock(Shell.class);

        when(widget.isDisposed()).thenReturn(true);
        when(widget.getShell()).thenReturn(shell);
        when(viewer.getDocument()).thenReturn(document);
        when(viewer.getTextWidget()).thenReturn(widget);
        when(document.getContentType(51)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        when(document.getLegalContentTypes()).thenReturn(new String[0]);
        when(document.getPositionCategories()).thenReturn(new String[0]);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final KeywordCallTemplateAssistProcessor processor = new KeywordCallTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 51);

        assertThat(proposals).hasSize(1).are(proposalWithImage(ImagesManager.getImage(RedImages.getTemplateImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer, 51, (TemplateProposal) proposal))
                .containsOnly(new Document("*** Settings ***",
                        "setting",
                        "*** Test Cases ***",
                        "test",
                        "  FOR    ${element}    IN    @{elements_list}",
                        "        Keyword    arg",
                        "    END",
                        "  kw1  arg",
                        "new other test",
                        "  formal keyword",
                        ""));
    }

    @Test
    public void noProposalsAreProvided_whenPrefixIsNotEmptyAndRegionOffsetIsSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(65)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final KeywordCallTemplateAssistProcessor processor = new KeywordCallTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 65);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void proposalsAreProvided_whenPrefixIsNotEmptyAndRegionOffsetIsNotSameAsLineOffset() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class, withSettings().extraInterfaces(ITextViewerExtension.class,
                ITextViewerExtension2.class, IPostSelectionProvider.class));
        final ISelectionProvider selectionProvider = mock(ISelectionProvider.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(suite)));
        final StyledText widget = mock(StyledText.class);
        final Shell shell = mock(Shell.class);

        when(widget.isDisposed()).thenReturn(true);
        when(widget.getShell()).thenReturn(shell);
        when(viewer.getDocument()).thenReturn(document);
        when(viewer.getTextWidget()).thenReturn(widget);
        when(document.getContentType(83)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);
        when(document.getLegalContentTypes()).thenReturn(new String[0]);
        when(document.getPositionCategories()).thenReturn(new String[0]);
        when(viewer.getSelectionProvider()).thenReturn(selectionProvider);
        when(selectionProvider.getSelection()).thenReturn(mock(ITextSelection.class));

        final KeywordCallTemplateAssistProcessor processor = new KeywordCallTemplateAssistProcessor(
                createAssistant(suite));
        final ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 83);

        assertThat(proposals).hasSize(1).are(proposalWithImage(ImagesManager.getImage(RedImages.getTemplateImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer, 83, (TemplateProposal) proposal))
                .containsOnly(new Document("*** Settings ***",
                        "setting",
                        "*** Test Cases ***",
                        "test",
                        "  ",
                        "  kw1  arg",
                        "new other test",
                        "  FOR    ${element}    IN    @{elements_list}",
                        "        Keyword    arg",
                        "    ENDmal keyword",
                        ""));
    }
}
