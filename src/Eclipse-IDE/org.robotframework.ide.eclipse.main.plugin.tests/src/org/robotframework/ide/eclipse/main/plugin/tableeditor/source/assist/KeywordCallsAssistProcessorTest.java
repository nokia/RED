/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.byApplyingToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.viewers.StyledString;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.testdata.model.search.keyword.KeywordScope;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.assist.AssistProposal;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.model.locators.KeywordEntity;
import org.robotframework.ide.eclipse.main.plugin.project.library.ArgumentsDescriptor;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;
import com.google.common.base.Supplier;

public class KeywordCallsAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(KeywordCallsAssistProcessorTest.class);

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();
        
        projectProvider.createFile("res.robot", "*** Keywords ***", "kw1", "kw2");

        projectProvider.createFile("suite.robot",
                "*** Test Cases ***",
                "case",
                "  ",
                "  rst",
                "  acb  kjm",
                "*** Keywords ***",
                "akeyword",
                "keyword",
                "*** Settings ***",
                "Resource  res.robot");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
    }

    @Test
    public void keywordsProcessorIsValidOnlyForKeywordsOrCasesSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.KEYWORDS_SECTION,
                SuiteSourcePartitionScanner.TEST_CASES_SECTION);
    }

    @Test
    public void keywordsProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanTestCases() throws Exception {
        final int offset = 26;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfExecutionLine() throws Exception {
        final int offset = 24;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndExecutionLine() throws Exception {
        final int offset = 26;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.TEST_CASES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(4)
                .haveExactly(4, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Test Cases ***", "case", "  akeyword", "  rst", "  acb  kjm", "*** Keywords ***",
                        "akeyword",
                        "keyword", "*** Settings ***", "Resource  res.robot"),
                new Document("*** Test Cases ***", "case", "  keyword", "  rst", "  acb  kjm", "*** Keywords ***",
                        "akeyword",
                        "keyword", "*** Settings ***", "Resource  res.robot"),
                new Document("*** Test Cases ***", "case", "  kw1", "  rst", "  acb  kjm", "*** Keywords ***",
                        "akeyword",
                        "keyword", "*** Settings ***", "Resource  res.robot"),
                new Document("*** Test Cases ***", "case", "  kw2", "  rst", "  acb  kjm", "*** Keywords ***",
                        "akeyword",
                        "keyword", "*** Settings ***", "Resource  res.robot"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheCellBeginInExecutionLine() throws Exception {
        final int offset = 29;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(4).haveExactly(4,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Test Cases ***", "case", "  ", "  akeyword", "  acb  kjm", "*** Keywords ***",
                        "akeyword", "keyword", "*** Settings ***", "Resource  res.robot"),
                new Document("*** Test Cases ***", "case", "  ", "  keyword", "  acb  kjm", "*** Keywords ***",
                        "akeyword", "keyword", "*** Settings ***", "Resource  res.robot"),
                new Document("*** Test Cases ***", "case", "  ", "  kw1", "  acb  kjm", "*** Keywords ***", "akeyword",
                        "keyword", "*** Settings ***", "Resource  res.robot"),
                new Document("*** Test Cases ***", "case", "  ", "  kw2", "  acb  kjm", "*** Keywords ***", "akeyword",
                        "keyword", "*** Settings ***", "Resource  res.robot"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInExecutionLine_1() throws Exception {
        final int offset = 36;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(new Document("*** Test Cases ***", "case", "  ", "  rst", "  akeyword  kjm",
                        "*** Keywords ***", "akeyword", "keyword", "*** Settings ***", "Resource  res.robot"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInExecutionLine_2() throws Exception {
        final int offset = 41;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  keyword", "*** Keywords ***",
                        "akeyword", "keyword", "*** Settings ***", "Resource  res.robot"),
                new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  kw1", "*** Keywords ***", "akeyword",
                        "keyword", "*** Settings ***", "Resource  res.robot"),
                new Document("*** Test Cases ***", "case", "  ", "  rst", "  acb  kw2", "*** Keywords ***", "akeyword",
                        "keyword", "*** Settings ***", "Resource  res.robot"));
    }

    @Test
    public void regionsForLiveEditOfEmbeddedKeyword_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final KeywordEntity entity = new MockProposal("keyword ${e1} with ${e2} args");
        final Collection<IRegion> regions = processor.calculateRegionsForLinkedMode(entity, 100, "");

        assertThat(regions).containsOnly(new Region(108, 5), new Region(119, 5));
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithoutArguments_areEmpty() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final KeywordEntity entity = new MockProposal("keyword");
        final Collection<IRegion> regions = processor.calculateRegionsForLinkedMode(entity, 100, "");

        assertThat(regions).isEmpty();
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithSingleArgument_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final KeywordEntity entity = new MockProposal("keyword", "arg1");
        final Collection<IRegion> regions = processor.calculateRegionsForLinkedMode(entity, 100, "");

        assertThat(regions).containsOnly(new Region(109, 4));
    }

    @Test
    public void regionsForLiveEditOfRegularKeywordWithManyArgument_areProperlyCalculated() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsAssistProcessor processor = new KeywordCallsAssistProcessor(createAssitant(model));

        final KeywordEntity entity = new MockProposal("keyword", "arg1", "arg2");
        final Collection<IRegion> regions = processor.calculateRegionsForLinkedMode(entity, 100, "");

        assertThat(regions).containsOnly(new Region(109, 4), new Region(115, 4));
    }

    private static IDocument documentFromSuiteFile() throws Exception {
        final String content = projectProvider.getFileContent("suite.robot");
        return new Document(Splitter.on('\n').splitToList(content));
    }

    private static SuiteSourceAssistantContext createAssitant(final RobotSuiteFile model) {
        return new SuiteSourceAssistantContext(new Supplier<RobotSuiteFile>() {

            @Override
            public RobotSuiteFile get() {
                return model;
            }
        }, new AssistPreferences(new MockRedPreferences(false, "  ")));
    }

    private static class MockProposal extends KeywordEntity implements AssistProposal {

        private final List<String> arguments;

        protected MockProposal(final String keywordName, final String... arguments) {
            super(KeywordScope.LOCAL, "source", keywordName, "source", false, ArgumentsDescriptor.createDescriptor(),
                    null);
            this.arguments = newArrayList(arguments);
        }

        @Override
        public String getContent() {
            return getNameFromDefinition();
        }

        @Override
        public List<String> getArguments() {
            return arguments;
        }

        @Override
        public ImageDescriptor getImage() {
            return null;
        }

        @Override
        public String getLabel() {
            return getNameFromDefinition();
        }

        @Override
        public StyledString getStyledLabel() {
            return new StyledString(getNameFromDefinition());
        }

        @Override
        public boolean hasDescription() {
            return false;
        }

        @Override
        public String getDescription() {
            return "";
        }

    }
}
