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
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithOperationsToPerformAfterAccepting;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.PreferenceUpdater;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;

public class KeywordCallsInSettingsAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(
            KeywordCallsInSettingsAssistProcessorTest.class);

    @Rule
    public PreferenceUpdater preferenceUpdater = new PreferenceUpdater();

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        projectProvider.createFile("res.robot", "*** Keywords ***", "kw");

        projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Resource  res.robot",
                "Suite Setup     abc  def  ghi",
                "Suite Teardown  abc  def  ghi",
                "Test Setup      abc  def  ghi",
                "Test Teardown   abc  def  ghi",
                "Test Template   abc  def  ghi",
                "Force Tags      abc  def  ghi",
                "Default Tags    abc  def  ghi",
                "Documentation   abc  def  ghi",
                "Metadata        abc  def  ghi",
                "*** Keywords ***",
                "akeyword",
                "keyword");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void keywordsInSettingsProcessorIsValidOnlyForSettingsSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void keywordsInSettingsProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanSettings() throws Exception {
        final int offset = 53;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfKeywordBasedSetting() throws Exception {
        final int offset = 37;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInThirdCellOfKeywordBasedSetting() throws Exception {
        final int offset = 58;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfForceTagsSetting() throws Exception {
        final int offset = 203;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfDefaultTagsSetting() throws Exception {
        final int offset = 233;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfDocumentationSetting() throws Exception {
        final int offset = 263;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfMetadataSetting() throws Exception {
        final int offset = 293;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfSuiteSetup() throws Exception {
        final int offset = 53;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     akeyword  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     keyword  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     kw  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfSuiteTeardown() throws Exception {
        final int offset = 83;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  akeyword  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  keyword  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  kw  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfTestSetup() throws Exception {
        final int offset = 113;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      akeyword  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      keyword  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      kw  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfTestTeardown() throws Exception {
        final int offset = 143;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   akeyword  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   keyword  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   kw  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfTestTemplate() throws Exception {
        final int offset = 173;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   akeyword  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   keyword  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   kw  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "*** Keywords ***",
                                "akeyword", "keyword"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInKeywordBasedSetting() throws Exception {
        final int offset = 54;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(new Document("*** Settings ***", "Resource  res.robot",
                "Suite Setup     akeyword  def  ghi", "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi", "Force Tags      abc  def  ghi",
                "Default Tags    abc  def  ghi", "Documentation   abc  def  ghi", "Metadata        abc  def  ghi",
                "*** Keywords ***", "akeyword", "keyword"));
    }

    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForNotAccessibleKeywordProposals() throws Exception {
        preferenceUpdater.setValue(RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED, true);

        final RobotProject project = robotModel.createRobotProject(projectProvider.getProject());
        project.setReferencedLibraries(Libraries.createRefLib("LibNotImported", "kw1", "kw2"));

        final int offset = 53;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(5)
                .haveExactly(3, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())))
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getKeywordImage())))
                .haveExactly(3, proposalWithOperationsToPerformAfterAccepting(0))
                .haveExactly(2, proposalWithOperationsToPerformAfterAccepting(1));
    }

    private static IDocument documentFromSuiteFile() throws Exception {
        final String content = projectProvider.getFileContent("suite.robot");
        return new Document(Splitter.on('\n').splitToList(content));
    }
}
