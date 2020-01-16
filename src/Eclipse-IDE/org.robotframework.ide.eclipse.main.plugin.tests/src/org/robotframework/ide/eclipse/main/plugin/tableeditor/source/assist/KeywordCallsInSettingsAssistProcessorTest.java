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
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFileContent;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotKeywordCall;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSettingsSection;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.jupiter.BooleanPreference;
import org.robotframework.red.junit.jupiter.PreferencesExtension;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith({ ProjectExtension.class, PreferencesExtension.class })
public class KeywordCallsInSettingsAssistProcessorTest {

    @Project
    static IProject project;

    private static RobotModel robotModel;

    private static IFile suite;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = RedPlugin.getModelManager().getModel();

        createFile(project, "res.robot", "*** Keywords ***", "kw");

        suite = createFile(project, "suite.robot",
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

    @AfterAll
    public static void afterSuite() {
        robotModel = null;
        suite = null;
        RedPlugin.getModelManager().dispose();
    }

    @Test
    public void keywordsInSettingsProcessorIsValidOnlyForSettingsSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void keywordsInSettingsProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanSettings() throws Exception {
        final int offset = 53;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInFirstCellOfKeywordBasedSetting() throws Exception {
        final int offset = 37;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInThirdCellOfKeywordBasedSetting() throws Exception {
        final int offset = 58;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfForceTagsSetting() throws Exception {
        final int offset = 203;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfDefaultTagsSetting() throws Exception {
        final int offset = 233;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfDocumentationSetting() throws Exception {
        final int offset = 263;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInSecondCellOfMetadataSetting() throws Exception {
        final int offset = 293;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfSuiteSetup() throws Exception {
        final int offset = 53;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).containsOnly(
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
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).containsOnly(
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
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).containsOnly(
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
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).containsOnly(
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
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).containsOnly(
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
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal))
                .containsOnly(new Document("*** Settings ***", "Resource  res.robot",
                        "Suite Setup     akeyword  def  ghi", "Suite Teardown  abc  def  ghi",
                        "Test Setup      abc  def  ghi", "Test Teardown   abc  def  ghi",
                        "Test Template   abc  def  ghi", "Force Tags      abc  def  ghi",
                        "Default Tags    abc  def  ghi", "Documentation   abc  def  ghi",
                        "Metadata        abc  def  ghi", "*** Keywords ***", "akeyword", "keyword"));
    }

    @BooleanPreference(key = RedPreferences.ASSISTANT_KEYWORD_FROM_NOT_IMPORTED_LIBRARY_ENABLED, value = true)
    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForNotAccessibleKeywordProposals() throws Exception {
        final RobotProject robotProject = robotModel.createRobotProject(project);
        robotProject.setReferencedLibraries(Libraries.createRefLib("LibNotImported", "kw1", "kw2"));

        final int offset = 53;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(5)
                .haveExactly(3, proposalWithImage(ImagesManager.getImage(RedImages.getUserKeywordImage())))
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getKeywordImage())))
                .haveExactly(3, proposalWithOperationsToPerformAfterAccepting(0))
                .haveExactly(2, proposalWithOperationsToPerformAfterAccepting(1));
    }

    @Test
    public void thereAreOperationsToPerformAfterAccepting_onlyForKeywordsWithArgumentsAndSettingIsNotTemplate()
            throws Exception {
        final IFile suite = createFile(project, "kw_based_settings_with_keywords_with_args.robot",
                "*** Settings ***",
                "Suite Setup  ",
                "Suite Teardown  ",
                "Test Setup  ",
                "Test Teardown  ",
                "Test Template  ",
                "*** Keywords ***",
                "kw_no_args",
                "kw_with_args",
                "  [Arguments]  ${arg1}  ${arg2}");

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(suite)));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(anyInt())).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(suite);
        final KeywordCallsInSettingsAssistProcessor processor = new KeywordCallsInSettingsAssistProcessor(
                createAssistant(model));

        final List<RobotKeywordCall> settings = model.findSection(RobotSettingsSection.class).get().getChildren();
        for (int i = 0; i < settings.size(); i++) {
            final int firstSettingLine = 1;
            final IRegion lineRegion = document.getLineInformation(i + firstSettingLine);
            final int offset = lineRegion.getOffset() + lineRegion.getLength();

            final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

            if (settings.get(i).getLinkedElement().getDeclaration().getText().equals("Test Template")) {
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
}
