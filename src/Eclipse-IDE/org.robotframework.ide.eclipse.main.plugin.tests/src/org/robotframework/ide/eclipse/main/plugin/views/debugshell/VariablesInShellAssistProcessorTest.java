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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.execution.server.response.EvaluateExpression.ExpressionType;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.project.RobotProjectConfig.ExecutionEnvironment;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

public class VariablesInShellAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(VariablesInShellAssistProcessorTest.class);

    private static IFile suite;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        suite = projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                "*** Variables ***",
                "${x}",
                "${y}");

        // skipping global variables
        final RobotProjectConfig config = RobotProjectConfig.create();
        config.setExecutionEnvironment(ExecutionEnvironment.create("", null));
        projectProvider.configure(config);
    }

    @AfterClass
    public static void afterSuite() {
        suite = null;
    }

    @Test
    public void variablesProcessorIsValidOnlyForDefaultSection() {
        final VariablesInShellAssistProcessor processor = new VariablesInShellAssistProcessor(
                createAssistant((RobotSuiteFile) null));

        assertThat(processor.getApplicableContentTypes()).containsOnly(IDocument.DEFAULT_CONTENT_TYPE);
    }

    @Test
    public void noProposalsAreProvided_whenInPythonMode() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().changeMode(ExpressionType.PYTHON).get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);

        final VariablesInShellAssistProcessor processor = new VariablesInShellAssistProcessor(
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


        final VariablesInShellAssistProcessor processor = new VariablesInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, currentEndOffset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenNothingIsWrittenInFirstCellOfPrompt() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().changeMode(ExpressionType.VARIABLE).get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);

        final VariablesInShellAssistProcessor processor = new VariablesInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).hasSize(2)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(shellDoc, proposal))
                .containsOnly(
                        new Document("VARIABLE> ${x}"),
                        new Document("VARIABLE> ${y}"));
    }

    @Test
    public void allProposalsAreProvided_whenInFirstCellOfPromptContinuation() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession("\n").type("keyword").continueExpr().get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);


        final VariablesInShellAssistProcessor processor = new VariablesInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).hasSize(2)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(shellDoc, proposal))
                .containsOnly(
                        new Document("ROBOT> keyword", "...... ${x}"),
                        new Document("ROBOT> keyword", "...... ${y}"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenThereIsATextInFirstCellOfPrompt() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().type("${x").get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);


        final VariablesInShellAssistProcessor processor = new VariablesInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).hasSize(1)
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(shellDoc, proposal))
                .containsOnly(new Document("ROBOT> ${x}"));
    }

    @Test
    public void emptyProposalsAreProvided_whenNothingMatchesWithATextInFirstCellOfPrompt() throws Exception {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(suite);

        final IDocument shellDoc = new ShellDocumentSession().type("3").get();

        final ITextViewer viewer = mock(ITextViewer.class);
        when(viewer.getDocument()).thenReturn(shellDoc);

        final VariablesInShellAssistProcessor processor = new VariablesInShellAssistProcessor(
                createAssistant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, shellDoc.getLength());

        assertThat(proposals).isEmpty();
    }
}
