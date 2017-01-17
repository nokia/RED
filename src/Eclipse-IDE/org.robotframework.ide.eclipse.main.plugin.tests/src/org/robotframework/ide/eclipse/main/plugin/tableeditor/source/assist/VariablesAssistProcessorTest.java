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
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;

public class VariablesAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(VariablesAssistProcessorTest.class);

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();
        
        projectProvider.createFile("res.robot", "*** Keywords ***", "kw1", "kw2");

        projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                "case",
                "  call  abc  d${bcdef",
                "*** Keywords ***",
                "keyword",
                "  call  abc  d${abc}d",
                "*** Variables ***",
                "${a}  1",
                "${b}  2",
                "@{c}  1  2  3",
                "&{d}  k1=v1  k2=v2",
                "*** Settings ***",
                "Metadata  abc  def");
        projectProvider.createFile("keyword_args.robot",
                "*** Keywords ***",
                "keyword",
                "  [Arguments]  ${x}  ${y}",
                "  call  abc  def  ",
                "*** Test Cases ***");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
    }

    @Test
    public void variablesProcessorIsValidOnlyForKeywordsOrCasesSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(
                SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.SETTINGS_SECTION,
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Test
    public void variablesProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenContentTypeIsNotApplicable() throws Exception {
        final int offset = 33;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn("__ct");

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfCasesSection_1() throws Exception {
        final int offset = 19;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfCasesSection_2() throws Exception {
        final int offset = 24;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfKeywordsSection_1() throws Exception {
        final int offset = 63;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfKeywordsSection_2() throws Exception {
        final int offset = 71;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfVariablesSection_1() throws Exception {
        final int offset = 111;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfVariablesSection_2() throws Exception {
        final int offset = 127;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfSettingsSection_1() throws Exception {
        final int offset = 160;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfSettingsSection_2() throws Exception {
        final int offset = 177;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenThereIsNoActualPrefixVariable() throws Exception {
        final int offset = 33;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(4)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())))
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getRobotListVariableImage())))
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getRobotDictionaryVariableImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Test Cases ***", "case", "  call  a${a}bc  d${bcdef", "*** Keywords ***", "keyword",
                        "  call  abc  d${abc}d", "*** Variables ***", "${a}  1", "${b}  2", "@{c}  1  2  3",
                        "&{d}  k1=v1  k2=v2", "*** Settings ***", "Metadata  abc  def"),
                new Document("*** Test Cases ***", "case", "  call  a${b}bc  d${bcdef", "*** Keywords ***", "keyword",
                        "  call  abc  d${abc}d", "*** Variables ***", "${a}  1", "${b}  2", "@{c}  1  2  3",
                        "&{d}  k1=v1  k2=v2", "*** Settings ***", "Metadata  abc  def"),
                new Document("*** Test Cases ***", "case", "  call  a@{c}bc  d${bcdef", "*** Keywords ***", "keyword",
                        "  call  abc  d${abc}d", "*** Variables ***", "${a}  1", "${b}  2", "@{c}  1  2  3",
                        "&{d}  k1=v1  k2=v2", "*** Settings ***", "Metadata  abc  def"),
                new Document("*** Test Cases ***", "case", "  call  a&{d}bc  d${bcdef", "*** Keywords ***", "keyword",
                        "  call  abc  d${abc}d", "*** Variables ***", "${a}  1", "${b}  2", "@{c}  1  2  3",
                        "&{d}  k1=v1  k2=v2", "*** Settings ***", "Metadata  abc  def"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenThereIsActualPrefixVariable_1() throws Exception {
        final int offset = 41;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Test Cases ***", "case", "  call  abc  d${b}cdef", "*** Keywords ***", "keyword",
                        "  call  abc  d${abc}d", "*** Variables ***", "${a}  1", "${b}  2", "@{c}  1  2  3",
                        "&{d}  k1=v1  k2=v2", "*** Settings ***", "Metadata  abc  def"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenThereIsActualPrefixVariable_2() throws Exception {
        final int offset = 88;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("suite.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(new Document("*** Test Cases ***", "case",
                "  call  abc  d${bcdef", "*** Keywords ***", "keyword", "  call  abc  d${a}d", "*** Variables ***",
                "${a}  1", "${b}  2", "@{c}  1  2  3", "&{d}  k1=v1  k2=v2", "*** Settings ***", "Metadata  abc  def"));
    }

    @Test
    public void allProposalsFromKeywordArgumentsAreProvided_whenInsideTheKeyword_1() throws Exception {
        final int offset = 59;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("keyword_args.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("keyword_args.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2).haveExactly(2,
                proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Keywords ***", "keyword", "  [Arguments]  ${x}  ${y}", "  call  ${x}abc  def  ",
                        "*** Test Cases ***"),
                new Document("*** Keywords ***", "keyword", "  [Arguments]  ${x}  ${y}", "  call  ${y}abc  def  ",
                        "*** Test Cases ***"));
    }

    @Test
    public void allProposalsFromKeywordArgumentsAreProvided_whenInsideTheKeyword_2() throws Exception {
        final int offset = 69;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile("keyword_args.robot"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("keyword_args.robot"));
        final VariablesAssistProcessor processor = new VariablesAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2).haveExactly(2,
                proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Keywords ***", "keyword", "  [Arguments]  ${x}  ${y}", "  call  abc  def  ${x}",
                        "*** Test Cases ***"),
                new Document("*** Keywords ***", "keyword", "  [Arguments]  ${x}  ${y}", "  call  abc  def  ${y}",
                        "*** Test Cases ***"));
    }

    private static IDocument documentFromSuiteFile(final String fileName) throws Exception {
        final String content = projectProvider.getFileContent(fileName);
        return new Document(Splitter.on('\n').splitToList(content));
    }
}
