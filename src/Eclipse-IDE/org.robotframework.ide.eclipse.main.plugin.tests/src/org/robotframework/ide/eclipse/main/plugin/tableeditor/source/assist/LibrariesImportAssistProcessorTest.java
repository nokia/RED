/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Assistant.createAssistant;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.applyToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;
import static org.robotframework.red.junit.jupiter.ProjectExtension.createFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFile;
import static org.robotframework.red.junit.jupiter.ProjectExtension.getFileContent;

import java.util.List;

import org.eclipse.core.resources.IProject;
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
public class LibrariesImportAssistProcessorTest {

    @Project
    static IProject project;

    private static RobotModel robotModel;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(project);
        robotProject.setStandardLibraries(Libraries.createStdLibs("StdLib1", "StdLib2"));
        robotProject.setReferencedLibraries(Libraries.createRefLibs("cRefLib"));

        createFile(project, "suite.robot",
                "*** Settings ***",
                "Resources  cell",
                "Library  ",
                "Library  cell1  cell2");
    }

    @AfterAll
    public static void afterSuite() {
        robotModel = null;
    }

    @Test
    public void librariesImportsProcessorIsValidOnlyForSettingsSection() {
        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void librariesImportsProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanSettings() throws Exception {
        final int offset = 42;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInOtherImportThanLibrary() throws Exception {
        final int offset = 28;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInThirdCellOfLibrarySetting() throws Exception {
        final int offset = 59;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndOfLibrarySettingLine() throws Exception {
        final int offset = 42;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).contains(
                new Document("*** Settings ***", "Resources  cell", "Library  StdLib1  ", "Library  cell1  cell2"),
                new Document("*** Settings ***", "Resources  cell", "Library  StdLib2  ", "Library  cell1  cell2"),
                new Document("*** Settings ***", "Resources  cell", "Library  cRefLib  ", "Library  cell1  cell2"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheBeginOfSecondCellInLibrarySettingLine() throws Exception {
        final int offset = 52;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).contains(
                new Document("*** Settings ***", "Resources  cell", "Library  ", "Library  StdLib1  cell2"),
                new Document("*** Settings ***", "Resources  cell", "Library  ", "Library  StdLib2  cell2"),
                new Document("*** Settings ***", "Resources  cell", "Library  ", "Library  cRefLib  cell2"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheSecondCellInLibrarySettingLine() throws Exception {
        final int offset = 53;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "suite.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(getFile(project, "suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal))
                .contains(new Document("*** Settings ***", "Resources  cell", "Library  ", "Library  cRefLib  cell2"));
    }
}
