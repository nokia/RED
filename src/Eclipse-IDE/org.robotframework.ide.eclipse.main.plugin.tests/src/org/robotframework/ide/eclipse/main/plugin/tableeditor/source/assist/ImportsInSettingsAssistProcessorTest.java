/*
 * Copyright 2017 Nokia Solutions and Networks
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
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.editor.libraries.Libraries;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;

public class ImportsInSettingsAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(
            ImportsInSettingsAssistProcessorTest.class);

    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setReferencedLibraries(Libraries.createRefLibs("lib", "lib2"));
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
                "Library  lib",
                "Library  lib2  WITH NAME  alias");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
    }

    @Test
    public void importsInSettingsProcessorIsValidOnlyForSettingsSections() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
                createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void importsInSettingsProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
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
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
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
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
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
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
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
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
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
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
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
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
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
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
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
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getRobotFileImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     alias.  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     lib.  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     res.  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfSuiteTeardown() throws Exception {
        final int offset = 83;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getRobotFileImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  alias.  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  lib.  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  res.  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfTestSetup() throws Exception {
        final int offset = 113;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getRobotFileImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      alias.  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      lib.  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      res.  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfTestTeardown() throws Exception {
        final int offset = 143;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getRobotFileImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   alias.  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   lib.  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   res.  def  ghi", "Test Template   abc  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheSecondCellBeginOfTestTemplate() throws Exception {
        final int offset = 173;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3)
                .haveExactly(2, proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())))
                .haveExactly(1, proposalWithImage(ImagesManager.getImage(RedImages.getRobotFileImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments)
                .containsOnly(
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   alias.  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   lib.  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"),
                        new Document("*** Settings ***", "Resource  res.robot", "Suite Setup     abc  def  ghi",
                                "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                                "Test Teardown   abc  def  ghi", "Test Template   res.  def  ghi",
                                "Force Tags      abc  def  ghi", "Default Tags    abc  def  ghi",
                                "Documentation   abc  def  ghi", "Metadata        abc  def  ghi", "Library  lib",
                                "Library  lib2  WITH NAME  alias"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheCellInKeywordBasedSetting() throws Exception {
        final int offset = 54;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final ImportsInSettingsAssistProcessor processor = new ImportsInSettingsAssistProcessor(
                createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(new Document("*** Settings ***", "Resource  res.robot",
                "Suite Setup     alias.  def  ghi", "Suite Teardown  abc  def  ghi", "Test Setup      abc  def  ghi",
                "Test Teardown   abc  def  ghi", "Test Template   abc  def  ghi", "Force Tags      abc  def  ghi",
                "Default Tags    abc  def  ghi", "Documentation   abc  def  ghi", "Metadata        abc  def  ghi",
                "Library  lib", "Library  lib2  WITH NAME  alias"));
    }

    private static IDocument documentFromSuiteFile() throws Exception {
        final String content = projectProvider.getFileContent("suite.robot");
        return new Document(Splitter.on('\n').splitToList(content));
    }
}
