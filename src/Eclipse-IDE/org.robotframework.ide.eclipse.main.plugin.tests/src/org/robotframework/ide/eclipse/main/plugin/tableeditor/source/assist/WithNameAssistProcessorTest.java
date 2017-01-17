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
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Assistant.createAssistant;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;

public class WithNameAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(WithNameAssistProcessorTest.class);

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createFile("suite.robot",
                "*** Settings ***", "Library  FooBar  ", "Resource  res.robot", "Library  FooBar  with xyz");
    }

    @AfterClass
    public static void afterSuite() {
        projectProvider = null;
    }

    @Test
    public void withNameProcessorIsValidOnlyForSettingsSection() {
        final WithNameAssistProcessor processor = new WithNameAssistProcessor(
                createAssistant(projectProvider.getFile("suite.robot")));
        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void withNameProcessorHasTitleDefined() {
        final WithNameAssistProcessor processor = new WithNameAssistProcessor(
                createAssistant(projectProvider.getFile("suite.robot")));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenIsBeforeThirdCell() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        // use real offset and probably mock settingsgroup
        when(document.getContentType(30)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final WithNameAssistProcessor processor = new WithNameAssistProcessor(
                createAssistant(projectProvider.getFile("suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 30);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenNotInApplicableContentType() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(72)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final WithNameAssistProcessor processor = new WithNameAssistProcessor(
                createAssistant(projectProvider.getFile("suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 72);

        assertThat(proposals).isNull();
    }

    @Test
    public void withNameProposalIsProvided_whenInApplicableContentType() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(72)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final WithNameAssistProcessor processor = new WithNameAssistProcessor(
                createAssistant(projectProvider.getFile("suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 72);

        assertThat(proposals).isNotNull();
        assertThat(proposals).hasSize(1);
        assertThat(proposals.get(0).getDisplayString()).isEqualTo("WITH NAME");
    }

    @Test
    public void withNameProposalIsProvided_whenInApplicableContentTypeAndMatchesPrefix() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(74)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final WithNameAssistProcessor processor = new WithNameAssistProcessor(
                createAssistant(projectProvider.getFile("suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 74);

        assertThat(proposals).isNotNull();
        assertThat(proposals).hasSize(1);
        assertThat(proposals.get(0).getDisplayString()).isEqualTo("WITH NAME");
    }

    @Test
    public void withNameProposalIsProvided_whenInApplicableContentTypeAndAtTheEndOfCell() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(34)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final WithNameAssistProcessor processor = new WithNameAssistProcessor(
                createAssistant(projectProvider.getFile("suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 34);

        assertThat(proposals).isNotNull();
        assertThat(proposals).hasSize(1);
        assertThat(proposals.get(0).getDisplayString()).isEqualTo("WITH NAME");
    }

    @Test
    public void noProposalsAreProvided_whenInApplicableContentTypeButDoesntMatchPrefix() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(80)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final WithNameAssistProcessor processor = new WithNameAssistProcessor(
                createAssistant(projectProvider.getFile("suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 80);

        assertThat(proposals).isEmpty();
    }

    private IDocument documentFromFile(final String fileName) throws Exception {
        final String content = projectProvider.getFileContent(fileName);
        return new Document(Splitter.on('\n').splitToList(content));
    }
}
