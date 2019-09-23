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
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithOperationsToPerformAfterAccepting;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.rf.ide.core.libraries.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.assist.RedKeywordProposal;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotCasesSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;

public class KeywordCallsAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(KeywordCallsAssistProcessorTest.class);

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private static RobotModel robotModel;

    private static IFile suite;

    private static IFile suiteWithTemplate;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        projectProvider.createFile("res.robot", "*** Keywords ***", "kw1", "kw2");

        suite = projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                "case",
                "  ",
                "  rst",
                "  acb  kjm",
                "  [Teardown]  ",
                "  [Tags]  ",
                "*** Keywords ***",
                "abcdefgh",
                "keyword",
                "*** Settings ***",
                "Resource  res.robot");
        suiteWithTemplate = projectProvider.createFile("with_template.robot",
                "*** Test Cases ***",
                "tc",
                "  Kw Call  ",
                "*** Keywords ***",
                "abcdefgh",
                "keyword",
                "*** Settings ***",
                "Resource  res.robot",
                "Test Template  Some Kw");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
        suite = null;
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void keywordsProcessorIsValidOnlyForKeywordsOrCasesSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TASKS_SECTION, SuiteSourcePartitionScanner.TEST_CASES_SECTION);
    }

    @Test
    public void keywordsProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanTestCases() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 26);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 24);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenSettingIsNotKeywordBased() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 69);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenSettingIsKeywordBased() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 58);

        assertThat(proposals).hasSize(4)
                .haveExactly(4, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  kjm",
                                "  [Teardown]  abcdefgh", "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword",
                                "*** Settings ***", "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  kjm", "  [Teardown]  keyword",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  kjm", "  [Teardown]  kw1",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  kjm", "  [Teardown]  kw2",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"));
    }

    @Test
    public void noProposalsAreProvided_whenTemplateIsUsed() throws Exception {
        final ITextViewer viewer = createViewer(suiteWithTemplate, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suiteWithTemplate);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 33);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 26);

        assertThat(proposals).hasSize(4)
                .haveExactly(4, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "case", "  abcdefgh", "  rst", "  acb  kjm",
                                "  [Teardown]  ", "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword",
                                "*** Settings ***", "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  keyword", "  rst", "  acb  kjm", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  kw1", "  rst", "  acb  kjm", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  kw2", "  rst", "  acb  kjm", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheCellBeginInExecutionLine() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 29);

        assertThat(proposals).hasSize(4).haveExactly(4,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "case", "  ", "  abcdefgh", "  acb  kjm", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  ", "  keyword", "  acb  kjm", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  ", "  kw1", "  acb  kjm", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  ", "  kw2", "  acb  kjm", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInExecutionLine_1() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 36);

        assertThat(proposals).hasSize(1)
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(new Document("*** Test Cases ***", "case", "  ", "  rst", "  abcdefgh  kjm",
                        "  [Teardown]  ", "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                        "Resource  res.robot"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInExecutionLine_2() throws Exception {
        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 41);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(viewer.getDocument(), proposal))
                .containsOnly(
                        new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  keyword", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  kw1", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"),
                        new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  kw2", "  [Teardown]  ",
                                "  [Tags]  ", "*** Keywords ***", "abcdefgh", "keyword", "*** Settings ***",
                                "Resource  res.robot"));
    }

    @Test
    public void regionsForLiveEditOfEmbeddedKeyword_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final int separatorLength = createAssistant(model).getSeparatorToFollow().length();
        final RedKeywordProposal proposal = createKeywordProposal("keyword ${e1} with ${e2} args");

        assertThat(KeywordCallsAssistProcessor.calculateRegionsForLinkedMode(proposal, 100, separatorLength))
                .containsOnly(new Region(108, 5), new Region(119, 5));
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithoutArguments_areEmpty() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final int separatorLength = createAssistant(model).getSeparatorToFollow().length();
        final RedKeywordProposal proposal = createKeywordProposal("keyword");

        assertThat(KeywordCallsAssistProcessor.calculateRegionsForLinkedMode(proposal, 100, separatorLength)).isEmpty();
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithSingleArgument_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final int separatorLength = createAssistant(model).getSeparatorToFollow().length();
        final RedKeywordProposal proposal = createKeywordProposal("keyword", "arg1");

        assertThat(KeywordCallsAssistProcessor.calculateRegionsForLinkedMode(proposal, 100, separatorLength))
                .containsOnly(new Region(109, 4));
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithManyArgument_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final int separatorLength = createAssistant(model).getSeparatorToFollow().length();
        final RedKeywordProposal proposal = createKeywordProposal("keyword", "arg1", "arg2");

        assertThat(KeywordCallsAssistProcessor.calculateRegionsForLinkedMode(proposal, 100, separatorLength))
                .containsOnly(new Region(109, 4), new Region(115, 4));
    }

    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForNotAccessibleKeywordProposals() throws Exception {
        preferenceUpdater.setValue(RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED, true);

        final RobotProject project = robotModel.createRobotProject(projectProvider.getProject());
        project.setReferencedLibraries(Libraries.createRefLib("LibNotImported", "kw1", "kw2"));

        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 29);

        assertThat(proposals).hasSize(6)
                .haveExactly(4, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())))
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getKeywordImage())))
                .haveExactly(4, proposalWithOperationsToPerformAfterAccepting(0))
                .haveExactly(2, proposalWithOperationsToPerformAfterAccepting(1));
    }

    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForKeywordsWithArgumentsAndKeywordBasedSettingIsNotTemplate()
            throws Exception {
        final IFile suite = projectProvider.createFile("keywords_with_args_in_setting_suite.robot",
                "*** Test Cases ***",
                "tc",
                "  [Setup]  ",
                "  [Teardown]  ",
                "  [Template]  ",
                "*** Keywords ***",
                "kw_no_args",
                "kw_with_args",
                "  [Arguments]  ${arg1}  ${arg2}");

        final ITextViewer viewer = createViewer(suite, SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(
                createAssistant(model));

        final List<RobotKeywordCall> settings = model.findSection(RobotCasesSection.class)
                .get()
                .getChildren()
                .get(0)
                .getChildren();
        for (int i = 0; i < settings.size(); i++) {
            final int firstSettingLine = 2;
            final IRegion lineRegion = viewer.getDocument().getLineInformation(i + firstSettingLine);
            final int offset = lineRegion.getOffset() + lineRegion.getLength();

            final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

            if (settings.get(i).getLinkedElement().getDeclaration().getText().equals("[Template]")) {
                assertThat(proposals).hasSize(2)
                        .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())))
                        .haveExactly(2, proposalWithOperationsToPerformAfterAccepting(0));
            } else {
                assertThat(proposals).hasSize(2)
                        .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())))
                        .haveExactly(1, proposalWithOperationsToPerformAfterAccepting(0))
                        .haveExactly(1, proposalWithOperationsToPerformAfterAccepting(1));
            }
        }
    }

    private RedKeywordProposal createKeywordProposal(final String name, final String... arguments) {
        final RedKeywordProposal proposal = mock(RedKeywordProposal.class);
        when(proposal.getNameFromDefinition()).thenReturn(name);
        when(proposal.getContent()).thenReturn(name);
        when(proposal.getArgumentsDescriptor()).thenReturn(ArgumentsDescriptor.createDescriptor(arguments));
        when(proposal.getArguments()).thenReturn(Arrays.asList(arguments));
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
