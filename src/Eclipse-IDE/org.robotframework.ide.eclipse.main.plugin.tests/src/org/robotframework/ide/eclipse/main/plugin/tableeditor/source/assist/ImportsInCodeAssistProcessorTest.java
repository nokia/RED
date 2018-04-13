/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Assistant.createAssistant;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.activatingAssistantAfterAccept;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.byApplyingToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;

public class ImportsInCodeAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ImportsInCodeAssistProcessorTest.class);

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLibs("Lib1", "Lib2", "Lib3"));

        projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  Lib1",
                "Library  Lib3",
                "Resource  res1.robot",
                "Resource  abcde.robot",
                "*** Keywords ***",
                "keyword",
                "  ",
                "  rst",
                "  acb  lkj");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
    }

    @Test
    public void importsInCodeProcessorIsValidOnlyForKeywordsOrCasesSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);
    }

    @Test
    public void importsInCodeProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanKeywords() throws Exception {
        final int offset = 115;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfExecutionLine() throws Exception {
        final int offset = 116;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndExecutionLine() throws Exception {
        final int offset = 115;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(4)
            .are(activatingAssistantAfterAccept())
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  Lib1.", "  rst", "  acb  lkj"),
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  Lib3.", "  rst", "  acb  lkj"),
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  res1.", "  rst", "  acb  lkj"),
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  abcde.", "  rst", "  acb  lkj"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheCellBeginInExecutionLine() throws Exception {
        final int offset = 118;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(4)
            .are(activatingAssistantAfterAccept())
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  Lib1.", "  acb  lkj"),
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  Lib3.", "  acb  lkj"),
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  res1.", "  acb  lkj"),
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  abcde.", "  acb  lkj"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInExecutionLine_1() throws Exception {
        final int offset = 119;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1)
            .are(activatingAssistantAfterAccept())
            .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  res1.", "  acb  lkj"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInExecutionLine_2() throws Exception {
        final int offset = 130;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2)
            .are(activatingAssistantAfterAccept())
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  rst", "  acb  Lib1."),
                new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  rst", "  acb  Lib3."));
    }

    private static IDocument documentFromSuiteFile() throws Exception {
        final String content = projectProvider.getFileContent("suite.robot");
        return new Document(Splitter.on('\n').splitToList(content));
    }
}
