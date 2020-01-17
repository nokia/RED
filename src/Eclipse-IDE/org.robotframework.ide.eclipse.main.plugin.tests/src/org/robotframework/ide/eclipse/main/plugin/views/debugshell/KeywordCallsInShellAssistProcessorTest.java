/*
 * Copyright 2019 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.views.debugshell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Assistant.createAssistant;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.applyToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class KeywordCallsInShellAssistProcessorTest {

    @Project
    static IProject project;

    private static IFile suite;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "res.robot",
                "*** Keywords ***",
                "kw1",
                "kw2",
                "  [Arguments]  ${x}");
        suite = createFile(project, "suite.robot",
                "*** Test Cases ***",
                "*** Settings ***",
                "Resource  res.robot");
    }

    @AfterAll
    public static void afterSuite() {
        suite = null;
    }

    @Test
    public void keywordsProcessorIsValidOnlyForDefaultSection() {
        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant((RobotSuiteFile) null));

        assertThat(processor.getApplicableContentTypes()).containsOnly(IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Test
    public void noProposalsAreProvided_whenInVariableMode() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().changeMode(ExpressionType.VARIABLE).get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);

        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInPythonMode() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().changeMode(ExpressionType.PYTHON).get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);

        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfPromptInAlreadyExecutedExpression() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final ShellDocumentSession session = new ShellDocumentSession();
        final int currentEndOffset = session.get().getLength();
        final IDocument shellDoc = session.execute("result").get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);


        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, currentEndOffset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfPromptContinuation() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().type("keyword").continueExpr().get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);


        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfPrompt() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().type("keyword  k").get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);

        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenNothingIsWrittenInFirstCellOfPrompt() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);

        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).hasSize(2)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(shellDoc, proposal))
                .containsOnly(
                        new Document("ROBOT> kw1"),
                        new Document("ROBOT> kw2  x"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenThereIsATextInFirstCellOfPrompt() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().type("1").get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);


        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).hasSize(1)
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(shellDoc, proposal))
                .containsOnly(new Document("ROBOT> kw1"));
    }

    @Test
    public void emptyProposalsAreProvided_whenNothingMatchesWithATextInFirstCellOfPrompt() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().type("3").get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);

        final KeywordCallsInShellAssistProcessor processor = new KeywordCallsInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).isEmpty();
    }
}
