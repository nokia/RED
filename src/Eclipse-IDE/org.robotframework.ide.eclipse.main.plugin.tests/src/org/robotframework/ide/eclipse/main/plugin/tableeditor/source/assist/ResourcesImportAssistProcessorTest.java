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
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;

public class ResourcesImportAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(ResourcesImportAssistProcessorTest.class);

    private static IFile importingFile;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        projectProvider.createDir("dir1");
        projectProvider.createDir("dir1_1");
        projectProvider.createDir("dir2");

        importingFile = projectProvider.createFile("importing_file.robot",
                "*** Settings ***",
                "Library   cell",
                "Resource  ",
                "Resource  cell1  cell2",
                "Resource  dir1cell");
        projectProvider.createFile("dir1/res1.robot", "*** Variables ***");
        projectProvider.createFile("dir1_1/lib.py");
        projectProvider.createFile("dir1_1/vars.py");
        projectProvider.createFile("dir2/res2.robot", "*** Variables ***");
        projectProvider.createFile("dir2/tests.robot", "*** Test Cases ***");
    }

    @AfterClass
    public static void afterSuite() {
        importingFile = null;
    }

    @Test
    public void resourcesImportsProcessorIsValidOnlyForSettingsSection() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final ResourcesImportAssistProcessor processor = new ResourcesImportAssistProcessor(createAssistant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void resourcesImportsProcessorHasTitleDefined() {
        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final ResourcesImportAssistProcessor processor = new ResourcesImportAssistProcessor(createAssistant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanSettings() throws Exception {
        final int offset = 42;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final ResourcesImportAssistProcessor processor = new ResourcesImportAssistProcessor(createAssistant(model));

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
        final ResourcesImportAssistProcessor processor = new ResourcesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInThirdCellOfResourcesSetting() throws Exception {
        final int offset = 60;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final ResourcesImportAssistProcessor processor = new ResourcesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndOfResourceSettingLine() throws Exception {
        final int offset = 42;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final ResourcesImportAssistProcessor processor = new ResourcesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2).haveExactly(2,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library   cell", "Resource  dir1/res1.robot",
                        "Resource  cell1  cell2", "Resource  dir1cell"),
                new Document("*** Settings ***", "Library   cell", "Resource  dir2/res2.robot",
                        "Resource  cell1  cell2", "Resource  dir1cell"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheBeginOfSecondCellInResourceSettingLine() throws Exception {
        final int offset = 76;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final ResourcesImportAssistProcessor processor = new ResourcesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(2).haveExactly(2,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library   cell", "Resource  ", "Resource  cell1  cell2",
                        "Resource  dir1/res1.robot"),
                new Document("*** Settings ***", "Library   cell", "Resource  ", "Resource  cell1  cell2",
                        "Resource  dir2/res2.robot"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenAtInsideTheSecondCellInResourceSettingLine() throws Exception {
        final int offset = 80;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromImportingFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotModel().createSuiteFile(importingFile);
        final ResourcesImportAssistProcessor processor = new ResourcesImportAssistProcessor(createAssistant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getImageForFileWithExtension("robot"))));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(new Document("*** Settings ***", "Library   cell",
                "Resource  ", "Resource  cell1  cell2", "Resource  dir1/res1.robot"));
    }

    private static IDocument documentFromImportingFile() throws Exception {
        final String content = projectProvider.getFileContent(importingFile.getProjectRelativePath());
        return new Document(Splitter.on('\n').splitToList(content));
    }
}
