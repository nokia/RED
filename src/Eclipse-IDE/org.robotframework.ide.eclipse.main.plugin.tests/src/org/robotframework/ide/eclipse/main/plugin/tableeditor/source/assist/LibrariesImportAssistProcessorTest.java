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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.rf.ide.core.project.RobotProjectConfig.ReferencedLibrary;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotModel;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.project.library.LibrarySpecification;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;
import org.robotframework.red.graphics.ImagesManager;
import org.robotframework.red.junit.ProjectProvider;

import com.google.common.base.Splitter;
import com.google.common.base.Supplier;

public class LibrariesImportAssistProcessorTest {

    @ClassRule
    public static ProjectProvider projectProvider = new ProjectProvider(LibrariesImportAssistProcessorTest.class);
    private static RobotModel robotModel;

    @BeforeClass
    public static void beforeSuite() throws Exception {
        robotModel = new RobotModel();

        final RobotProject robotProject = robotModel.createRobotProject(projectProvider.getProject());
        robotProject.setStandardLibraries(createStandardLibraries());
        robotProject.setReferencedLibraries(createReferencedLibraries());

        projectProvider.createFile("suite.robot",
                "*** Settings ***",
                "Resources  cell",
                "Library  ",
                "Library  cell1  cell2");
    }

    @AfterClass
    public static void afterSuite() {
        robotModel = null;
    }

    @Test
    public void librariesImportsProcessorIsValidOnlyForSettingsSection() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssitant(model));

        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void librariesImportsProcessorHasTitleDefined() {
        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssitant(model));
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenInSectionDifferentThanSettings() throws Exception {
        final int offset = 42;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInOtherImportThanLibrary() throws Exception {
        final int offset = 28;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenInThirdCellOfLibrarySetting() throws Exception {
        final int offset = 59;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenAtTheEndOfLibrarySettingLine() throws Exception {
        final int offset = 42;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Resources  cell", "Library  StdLib1  ", "Library  cell1  cell2"),
                new Document("*** Settings ***", "Resources  cell", "Library  StdLib2  ", "Library  cell1  cell2"),
                new Document("*** Settings ***", "Resources  cell", "Library  cRefLib  ", "Library  cell1  cell2"));
    }

    @Test
    public void allProposalsAreProvided_whenAtTheBeginOfSecondCellInLibrarySettingLine() throws Exception {
        final int offset = 52;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(3).haveExactly(3,
                proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Resources  cell", "Library  ", "Library  StdLib1  cell2"),
                new Document("*** Settings ***", "Resources  cell", "Library  ", "Library  StdLib2  cell2"),
                new Document("*** Settings ***", "Resources  cell", "Library  ", "Library  cRefLib  cell2"));
    }

    @Test
    public void onlyMatchingProposalsAreProvided_whenInsideTheSecondCellInLibrarySettingLine() throws Exception {
        final int offset = 53;

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(documentFromSuiteFile());

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(offset)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = robotModel.createSuiteFile(projectProvider.getFile("suite.robot"));
        final LibrariesImportAssistProcessor processor = new LibrariesImportAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, offset);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalWithImage(ImagesManager.getImage(RedImages.getLibraryImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Resources  cell", "Library  ", "Library  cRefLib  cell2"));
    }

    private static Map<String, LibrarySpecification> createStandardLibraries() {
        final LibrarySpecification stdLib1 = new LibrarySpecification();
        stdLib1.setName("StdLib1");
        final LibrarySpecification stdLib2 = new LibrarySpecification();
        stdLib2.setName("StdLib2");
        final Map<String, LibrarySpecification> stdLibs = new HashMap<>();
        stdLibs.put(stdLib1.getName(), stdLib1);
        stdLibs.put(stdLib2.getName(), stdLib2);
        return stdLibs;
    }

    private static Map<ReferencedLibrary, LibrarySpecification> createReferencedLibraries() {
        final ReferencedLibrary refTestLib = new ReferencedLibrary();
        refTestLib.setName("cRefLib");
        final LibrarySpecification testLib = new LibrarySpecification();
        testLib.setName("cRefLib");
        final Map<ReferencedLibrary, LibrarySpecification> refLibs = new HashMap<>();
        refLibs.put(refTestLib, testLib);
        return refLibs;
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
        }, new AssistPreferences(new MockRedPreferences(true, "  ")));
    }
}
