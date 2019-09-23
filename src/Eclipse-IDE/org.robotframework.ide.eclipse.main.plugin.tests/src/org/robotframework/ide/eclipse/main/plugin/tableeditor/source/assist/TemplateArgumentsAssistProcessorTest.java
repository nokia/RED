/*
 * Copyright 2019 Nokia Solutions and Networks
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

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.assist.RedTemplateArgumentsProposal;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

public class TemplateArgumentsAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(TemplateArgumentsAssistProcessorTest.class);

    private static RobotModel robotModel;

    private static IFile suite;

    private static IFile suiteWithEmbedded;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        projectProvider.createFile("res.robot",
                "*** Keywords ***",
                "Simple Keyword Name",
                "  [Arguments]  ${a1}  ${a2}  ${a3}",
                "  Log Many  ${a1}  ${a2}  ${a3}",
                "Embedded ${x1} Keyword ${x2} Name",
                "  Log Many  ${x1}  ${x2}");

        suite = projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                "case 1",
                "  [Template]  Simple Keyword Name",
                "  ",
                "  abc",
                "case 2",
                "  [Template]  NONE",
                "  [Setup]  ",
                "  ",
                "  abc",
                "*** Settings ***",
                "Resource  res.robot");

        suiteWithEmbedded = projectProvider.createFile("suite_embedded.robot",
                "*** Test Cases ***",
                "case 1",
                "  [Template]  Embedded ${x1} Keyword ${x2} Name",
                "  ",
                "  abc",
                "case 2",
                "  [Template]  Other ${a} Embedded ${b} Keyword ${c} Name",
                "  ",
                "  abc",
                "*** Settings ***",
                "Resource  res.robot");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
        suite = null;
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void templateArgumentsProcessorIsValidOnlyForTestCasesOrTasksSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.TASKS_SECTION);
    }

    @Test
    public void templateArgumentsProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanTestCasesOrTasks() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 26);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 60);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenExecutionLineIsNotEmpty() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 65);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenTemplateIsNotSpecified() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 109);

        assertThat(proposals).isNull();
    }

    @Test
    public void proposalsAreProvided_whenInsideEmptyLineOfTestCaseWithTemplate() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 62);

        assertThat(proposals).hasSize(1)
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getTemplatedKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(new Document("*** Test Cases ***", "case 1", "  [Template]  Simple Keyword Name",
                        "  a1  a2  a3", "  abc", "case 2", "  [Template]  NONE", "  [Setup]  ", "  ", "  abc",
                        "*** Settings ***", "Resource  res.robot"));
    }

    @Test
    public void proposalsAreProvided_whenInsideEmptyLineOfTestCaseWithTemplateWithEmbeddedArguments_1()
            throws Exception {
        final ITextViewer viewer = createViewer(suiteWithEmbedded, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suiteWithEmbedded);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 76);

        assertThat(proposals).hasSize(1)
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getTemplatedKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(new Document("*** Test Cases ***", "case 1",
                        "  [Template]  Embedded ${x1} Keyword ${x2} Name", "  x1  x2", "  abc", "case 2",
                        "  [Template]  Other ${a} Embedded ${b} Keyword ${c} Name", "  ", "  abc", "*** Settings ***",
                        "Resource  res.robot"));
    }

    @Test
    public void proposalsAreProvided_whenInsideEmptyLineOfTestCaseWithTemplateWithEmbeddedArguments_2()
            throws Exception {
        final ITextViewer viewer = createViewer(suiteWithEmbedded, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suiteWithEmbedded);
        final TemplateArgumentsAssistProcessor processor = new TemplateArgumentsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 149);

        assertThat(proposals).hasSize(1)
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getTemplatedKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "case 1", "  [Template]  Embedded ${x1} Keyword ${x2} Name",
                                "  ", "  abc", "case 2", "  [Template]  Other ${a} Embedded ${b} Keyword ${c} Name",
                                "  a  b  c", "  abc", "*** Settings ***", "Resource  res.robot"));
    }

    @Test
    public void regionsForLiveEditOfEmbeddedKeyword_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final int separatorLength = createAssistant(model).getSeparatorToFollow().length();
        final RedTemplateArgumentsProposal proposal = createTemplateArgumentsProposal(
                "keyword ${a} with ${bb} args ${ccc}", "a", "bb", "ccc");

        assertThat(TemplateArgumentsAssistProcessor.calculateRegionsForLinkedMode(proposal, 100, separatorLength))
                .containsOnly(new Region(100, 1), new Region(103, 2), new Region(107, 3));
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithoutArguments_areEmpty() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final int separatorLength = createAssistant(model).getSeparatorToFollow().length();
        final RedTemplateArgumentsProposal proposal = createTemplateArgumentsProposal("keyword");

        assertThat(TemplateArgumentsAssistProcessor.calculateRegionsForLinkedMode(proposal, 100, separatorLength))
                .isEmpty();
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithSingleArgument_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final int separatorLength = createAssistant(model).getSeparatorToFollow().length();
        final RedTemplateArgumentsProposal proposal = createTemplateArgumentsProposal("keyword", "a");

        assertThat(TemplateArgumentsAssistProcessor.calculateRegionsForLinkedMode(proposal, 100, separatorLength))
                .containsOnly(new Region(100, 1));
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithManyArgument_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final int separatorLength = createAssistant(model).getSeparatorToFollow().length();
        final RedTemplateArgumentsProposal proposal = createTemplateArgumentsProposal("keyword", "a", "bb", "ccc");

        assertThat(TemplateArgumentsAssistProcessor.calculateRegionsForLinkedMode(proposal, 100, separatorLength))
                .containsOnly(new Region(100, 1), new Region(103, 2), new Region(107, 3));
    }

    private RedTemplateArgumentsProposal createTemplateArgumentsProposal(final String name, final String... arguments) {
        final RedTemplateArgumentsProposal proposal = mock(RedTemplateArgumentsProposal.class);
        when(proposal.getNameFromDefinition()).thenReturn(name);
        when(proposal.getContent()).thenReturn(arguments.length > 0 ? arguments[0] : "");
        when(proposal.getArgumentsDescriptor()).thenReturn(ArgumentsDescriptor.createDescriptor(arguments));
        when(proposal.getArguments())
                .thenReturn(arguments.length > 1 ? Arrays.asList(Arrays.copyOfRange(arguments, 1, arguments.length))
                        : Arrays.asList());
        return proposal;
    }

    private ITextViewer createViewer(final IFile file, final String contentType) throws BadLocationException {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(projectProvider.getFileContent(file)));
        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(anyInt())).thenReturn(contentType);
        return viewer;
    }

}
