/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Assistant.createAssistant;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.applyToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFileContent;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class CodeReservedWordsAssistProcessorTest {

    @Project
    static IProject project;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "suite.robot",
                "*** Keywords ***",
                "keyword",
                "  ",
                "  cell1  cell2",
                "  Giv");
        createFile(project, "suite_for.robot",
                "*** Keywords ***",
                "keyword",
                "  :FOR  ${x}  ",
                "  :FOR  ${x}  IN cell");
        createFile(project, "suite_settings.robot",
                "*** Test Cases ***",
                "case",
                "  [Setup]  Some Keyword  arg",
                "  [Template]  Not Defined",
                "  [Teardown]  ",
                "  [Documentation]  abc");
    }

    @Test
    public void codeReservedWordsProcessorIsValidOnlyForKeywordsOrCasesSections() {
        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION);
    }

    @Test
    public void codeReservedWordsProcessorHasTitleDefined() {
        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenIsNotInTheFirstCell() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 37);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenNotInApplicableContentType() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 27);

        assertThat(proposals).isNull();
    }

    @Test
    public void gherkinAndForProposalsAreProvided_whenAtTheEndOfFirstCellOfKeywordsSection() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 27);

        assertThat(proposals).hasSize(8).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal)).containsOnly(
                new Document("*** Keywords ***", "keyword", "  :FOR  ", "  cell1  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  END  ", "  cell1  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  FOR  ", "  cell1  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  Given ", "  cell1  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  When ", "  cell1  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  Then ", "  cell1  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  And ", "  cell1  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  But ", "  cell1  cell2", "  Giv"));
    }

    @Test
    public void gherkinAndForProposalsAreProvided_whenInsideTheCellAndProposalIsMatchingToInput() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 48);

        assertThat(proposals).hasSize(1).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal)).containsOnly(
                new Document("*** Keywords ***", "keyword", "  ", "  cell1  cell2", "  Given "));
    }

    @Test
    public void gherkinAndForProposalsAreProvided_whenAtTheBeginningEndOfFirstCellOfKeywordsSection() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 30);

        assertThat(proposals).hasSize(8).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal)).containsOnly(
                new Document("*** Keywords ***", "keyword", "  ", "  :FOR  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  ", "  END  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  ", "  FOR  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  ", "  Given  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  ", "  When  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  ", "  Then  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  ", "  And  cell2", "  Giv"),
                new Document("*** Keywords ***", "keyword", "  ", "  But  cell2", "  Giv"));
    }

    @Test
    public void forIteratorProposalsAreProvided_whenAtTheEndOfForLine() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_for.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_for.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 39);

        assertThat(proposals).hasSize(4).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal)).containsOnly(
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  IN  ", "  :FOR  ${x}  IN cell"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  IN RANGE  ", "  :FOR  ${x}  IN cell"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  IN ENUMERATE  ", "  :FOR  ${x}  IN cell"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  IN ZIP  ", "  :FOR  ${x}  IN cell"));
    }

    @Test
    public void forIteratorProposalsAreProvided_whenWhenInsideTheCellOfForLine() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_for.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_for.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 56);

        assertThat(proposals).hasSize(4).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal)).containsOnly(
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  ", "  :FOR  ${x}  IN"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  ", "  :FOR  ${x}  IN RANGE"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  ", "  :FOR  ${x}  IN ENUMERATE"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  ", "  :FOR  ${x}  IN ZIP"));
    }

    @Test
    public void forIteratorProposalsAreProvided_whenAtTheBeginningOfTheCellOfForLine() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_for.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_for.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 54);

        assertThat(proposals).hasSize(4).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal)).containsOnly(
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  ", "  :FOR  ${x}  IN"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  ", "  :FOR  ${x}  IN RANGE"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  ", "  :FOR  ${x}  IN ENUMERATE"),
                new Document("*** Keywords ***", "keyword", "  :FOR  ${x}  ", "  :FOR  ${x}  IN ZIP"));
    }

    @Test
    public void disableSettingProposalIsNotProvided_whenIsNotInSecondCell() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_settings.robot"),
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_settings.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 49);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void disableSettingProposalIsProvided_whenInApplicableContentType() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_settings.robot"),
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_settings.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 67);

        assertThat(proposals).hasSize(1).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(new Document("*** Test Cases ***", "case", "  [Setup]  Some Keyword  arg",
                        "  [Template]  NONE", "  [Teardown]  ", "  [Documentation]  abc"));
    }

    @Test
    public void disableSettingProposalIsProvided_whenInApplicableContentTypeAndMatchesInput() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_settings.robot"),
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_settings.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 69);

        assertThat(proposals).hasSize(1).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(new Document("*** Test Cases ***", "case", "  [Setup]  Some Keyword  arg",
                        "  [Template]  NONE", "  [Teardown]  ", "  [Documentation]  abc"));
    }

    @Test
    public void disableSettingProposalIsProvided_whenInApplicableContentTypeAndAtTheEndOfCell() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_settings.robot"),
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_settings.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 93);

        assertThat(proposals).hasSize(1).are(proposalWithImage(null));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(new Document("*** Test Cases ***", "case", "  [Setup]  Some Keyword  arg",
                        "  [Template]  Not Defined", "  [Teardown]  NONE", "  [Documentation]  abc"));
    }

    @Test
    public void disableSettingProposalIsNotProvided_whenInApplicableContentTypeButDoesNotMatchInput() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_settings.robot"),
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_settings.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 39);

        assertThat(proposals).isEmpty();
    }

    @Test
    public void disableSettingProposalIsNotProvided_whenInApplicableContentTypeButNotInDisableableSetting()
            throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite_settings.robot"),
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final CodeReservedWordsAssistProcessor processor = new CodeReservedWordsAssistProcessor(
                createAssistant(getFile(project, "suite_settings.robot")));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 113);

        assertThat(proposals).isEmpty();
    }

    private ITextViewer createViewer(final IFile file, final String contentType) throws BadLocationException {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(file)));
        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(anyInt())).thenReturn(contentType);
        return viewer;
    }
}
