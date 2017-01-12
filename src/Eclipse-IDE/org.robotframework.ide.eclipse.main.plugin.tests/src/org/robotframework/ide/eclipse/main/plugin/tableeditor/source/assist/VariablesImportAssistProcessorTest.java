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
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.byApplyingToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalWithImage;

import java.util.List;

import org.eclipse.core.resources.IFile;
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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;
import com.google.common.base.Supplier;

public class VariablesImportAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(VariablesImportAssistProcessorTest.class);

    private static IFile importingFile;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir1");
        projectProvider.createDir("dir1_1");
        projectProvider.createDir("dir2");

        importingFile = projectProvider.createFile("importing_file.robot",
                "*** Settings ***",
                "Library   cell",
                "Variables  ",
                "Variables  cell1  cell2",
                "Variables  " + projectProvider.getProject().getName() + "/dir1cell");
        projectProvider.createFile("dir1/res1.robot", "*** Variables ***");
        projectProvider.createFile("dir1_1/vars.py");
        projectProvider.createFile("dir2/lib.py");
        projectProvider.createFile("dir2/res2.robot", "*** Variables ***");
        projectProvider.createFile("dir2/tests.robot", "*** Test Cases ***");
    }

    @AfterClass
    public static void afterSuite() {
        importingFile = null;
    }

    @Test
    public void variablesImportsProcessorIsValidOnlyForSettingsSection() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssitant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void variablesImportsProcessorHasTitleDefined() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssitant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanSettings() throws Exception {
        final int offset = 43;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInOtherImportThanResources() throws Exception {
        final int offset = 27;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInThirdCellOfVariablesSetting() throws Exception {
        final int offset = 62;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndOfVariablesSettingLine() throws Exception {
        final String projectName = projectProvider.getProject().getName();
        final int offset = 43;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2).haveExactly(2,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("py"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library   cell", "Variables  dir1_1/vars.py",
                        "Variables  cell1  cell2", "Variables  " + projectName + "/dir1cell"),
                new Document("*** Settings ***", "Library   cell", "Variables  dir2/lib.py", "Variables  cell1  cell2",
                        "Variables  " + projectName + "/dir1cell"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheBeginOfSecondCellInVariablesSettingLine() throws Exception {
        final int offset = 79;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2).haveExactly(2,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("py"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library   cell", "Variables  ", "Variables  cell1  cell2",
                        "Variables  dir1_1/vars.py"),
                new Document("*** Settings ***", "Library   cell", "Variables  ", "Variables  cell1  cell2",
                        "Variables  dir2/lib.py"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenAtInsideTheSecondCellInVariablesSettingLine() throws Exception {
        final String projectName = projectProvider.getProject().getName();
        final int offset = 84 + projectName.length();

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final VariablesImportAssistProcessor processor = new VariablesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("py"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(new Document("*** Settings ***", "Library   cell", "Variables  ",
                "Variables  cell1  cell2", "Variables  dir1_1/vars.py"));
    }

    private static IDocument documentFromImportingFile() throws Exception {
        final String content = projectProvider.getFileContent(importingFile.getProjectRelativePath());
        return new Document(Splitter.on('\n').splitToList(content));
    }

    private static SuiteSourceAssistantContext createAssitant(final RobotSuiteFile model) {
        return new SuiteSourceAssistantContext(new Supplier<RobotSuiteFile>() {

            @Override
            public RobotSuiteFile get() {
                return model;
            }
        }, new AssistPreferences(new MockRedPreferences(true, "  ")));
    }
}
