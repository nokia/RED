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
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.activatingAssistantAfterAccept;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.applyToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
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

public class ImportsInCodeAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ImportsInCodeAssistProcessorTest.class);

    private static RobotModel robotModel;

    private static IFile suite;

    private static IFile suiteWithTemplate;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(Libraries.createStdLibs("Lib1", "Lib2", "Lib3"));

        suite = projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Library  Lib1",
                "Library  Lib3",
                "Resource  res1.robot",
                "Resource  abcde.robot",
                "*** Keywords ***",
                "keyword",
                "  ",
                "  rst",
                "  acb  lkj",
                "  [Teardown]  ",
                "  [Tags]  ");
        suiteWithTemplate = projectProvider.createFile("with_template.robot",
                "*** Test Cases ***",
                "tc",
                "  Kw Call  ",
                "*** Settings ***",
                "Library  Lib1",
                "Library  Lib3",
                "Resource  res1.robot",
                "Resource  abcde.robot",
                "Test Template  Some Kw");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
        suite = null;
    }

    @Test
    public void importsInCodeProcessorIsValidOnlyForKeywordsOrCasesSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION);
    }

    @Test
    public void importsInCodeProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanKeywords() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 115);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 116);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenSettingIsNotKeywordBased() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 158);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenSettingIsKeywordBased() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 147);

        assertThat(proposals).hasSize(4)
                .are(activatingAssistantAfterAccept())
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
                .haveExactly(2,
                        proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  rst", "  acb  lkj",
                                "  [Teardown]  Lib1.", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  rst", "  acb  lkj",
                                "  [Teardown]  Lib3.", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  rst", "  acb  lkj",
                                "  [Teardown]  res1.", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  rst", "  acb  lkj",
                                "  [Teardown]  abcde.", "  [Tags]  "));
    }

    @Test
    public void noProposalsAreProvided_whenTemplateIsUsed() throws Exception {
        final ITextViewer viewer = createViewer(suiteWithTemplate, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suiteWithTemplate);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 33);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 115);

        assertThat(proposals).hasSize(4)
            .are(activatingAssistantAfterAccept())
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  Lib1.", "  rst",
                                "  acb  lkj", "  [Teardown]  ", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  Lib3.", "  rst",
                                "  acb  lkj", "  [Teardown]  ", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  res1.", "  rst",
                                "  acb  lkj", "  [Teardown]  ", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  abcde.", "  rst",
                                "  acb  lkj", "  [Teardown]  ", "  [Tags]  "));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheCellBeginInExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 118);

        assertThat(proposals).hasSize(4)
            .are(activatingAssistantAfterAccept())
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  Lib1.", "  acb  lkj",
                                "  [Teardown]  ", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  Lib3.", "  acb  lkj",
                                "  [Teardown]  ", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  res1.", "  acb  lkj",
                                "  [Teardown]  ", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  abcde.", "  acb  lkj",
                                "  [Teardown]  ", "  [Tags]  "));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInExecutionLine_1() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 119);

        assertThat(proposals).hasSize(1)
                .are(activatingAssistantAfterAccept())
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                        "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  res1.", "  acb  lkj",
                        "  [Teardown]  ", "  [Tags]  "));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInExecutionLine_2() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 130);

        assertThat(proposals).hasSize(2)
            .are(activatingAssistantAfterAccept())
            .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  rst", "  acb  Lib1.",
                                "  [Teardown]  ", "  [Tags]  "),
                        new Document("*** Settings ***", "Library  Lib1", "Library  Lib3", "Resource  res1.robot",
                                "Resource  abcde.robot", "*** Keywords ***", "keyword", "  ", "  rst", "  acb  Lib3.",
                                "  [Teardown]  ", "  [Tags]  "));
    }

    private ITextViewer createViewer(final IFile file, final String contentType) throws BadLocationException {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(file)));
        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(anyInt())).thenReturn(contentType);
        return viewer;
    }
}
