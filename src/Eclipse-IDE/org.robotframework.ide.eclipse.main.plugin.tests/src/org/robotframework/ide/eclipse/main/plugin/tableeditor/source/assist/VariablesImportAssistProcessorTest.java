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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.jupiter.Project;
import org.robotframework.red.junit.jupiter.ProjectExtension;

@ExtendWith(ProjectExtension.class)
public class VariablesImportAssistProcessorTest {

    @Project(dirs = { "dir1", "dir1_1", "dir2" }, files = { "dir1_1/vars.py", "dir2/lib.py" })
    static IProject project;

    @BeforeAll
    public static void beforeSuite() throws Exception {
        createFile(project, "importing_file.robot",
                "*** Settings ***",
                "Library   cell",
                "Variables  ",
                "Variables  cell1  cell2",
                "Variables  dir1cell");
        createFile(project, "dir1/res1.robot", "*** Variables ***");
        createFile(project, "dir2/res2.robot", "*** Variables ***");
        createFile(project, "dir2/tests.robot", "*** Test Cases ***");
    }

    @Test
    public void variablesImportsProcessorIsValidOnlyForSettingsSection() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "importing_file.robot"));
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void variablesImportsProcessorHasTitleDefined() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "importing_file.robot"));
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanSettings() throws Exception {
        final int offset = 43;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "importing_file.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "importing_file.robot"));
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInOtherImportThanResources() throws Exception {
        final int offset = 27;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "importing_file.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "importing_file.robot"));
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInThirdCellOfVariablesSetting() throws Exception {
        final int offset = 62;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "importing_file.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "importing_file.robot"));
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndOfVariablesSettingLine() throws Exception {
        final int offset = 43;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "importing_file.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "importing_file.robot"));
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2).haveExactly(2,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("py"))));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).containsOnly(
                new Document("*** Settings ***", "Library   cell", "Variables  dir1_1/vars.py",
                        "Variables  cell1  cell2", "Variables  dir1cell"),
                new Document("*** Settings ***", "Library   cell", "Variables  dir2/lib.py", "Variables  cell1  cell2",
                        "Variables  dir1cell"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheBeginOfSecondCellInVariablesSettingLine() throws Exception {
        final int offset = 79;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "importing_file.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "importing_file.robot"));
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2).haveExactly(2,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("py"))));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal)).containsOnly(
                new Document("*** Settings ***", "Library   cell", "Variables  ", "Variables  cell1  cell2",
                        "Variables  dir1_1/vars.py"),
                new Document("*** Settings ***", "Library   cell", "Variables  ", "Variables  cell1  cell2",
                        "Variables  dir2/lib.py"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenAtInsideTheSecondCellInVariablesSettingLine() throws Exception {
        final int offset = 83;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(getFileContent(project, "importing_file.robot")));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(getFile(project, "importing_file.robot"));
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("py"))));

        assertThat(proposals).extracting(proposal -> applyToDocument(document, proposal))
                .containsOnly(new Document("*** Settings ***", "Library   cell", "Variables  ",
                        "Variables  cell1  cell2", "Variables  dir1_1/vars.py"));
    }
}
