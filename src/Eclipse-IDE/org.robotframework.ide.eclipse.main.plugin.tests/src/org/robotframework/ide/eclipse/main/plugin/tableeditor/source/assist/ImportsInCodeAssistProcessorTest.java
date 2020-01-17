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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class ImportsInCodeAssistProcessorTest {

    @Project
    static IProject project;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(project);
        robotProject.setStandardLibraries(Libraries.createStdLibs("Lib1", "Lib2", "Lib3"));

        createFile(project, "suite.robot",
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
        createFile(project, "with_template.robot",
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

    @AfterAll
    public static void afterSuite() {
        robotModel = null;
    }

    @Test
    public void importsInCodeProcessorIsValidOnlyForKeywordsOrCasesSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION, SuiteSourcePartitionScanner.TASKS_SECTION);
    }

    @Test
    public void importsInCodeProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanKeywords() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 115);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 116);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenSettingIsNotKeywordBased() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 158);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenSettingIsKeywordBased() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
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
        final ITextViewer viewer = createViewer(getFile(project, "with_template.robot"),
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "with_template.robot"));
        final ImportsInCodeAssistProcessor processor = new ImportsInCodeAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 33);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
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
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
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
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
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
        final ITextViewer viewer = createViewer(getFile(project, "suite.robot"),
                SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
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
        final IDocument document = spy(new Document(getFileContent(file)));
        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(anyInt())).thenReturn(contentType);
        return viewer;
    }
}
