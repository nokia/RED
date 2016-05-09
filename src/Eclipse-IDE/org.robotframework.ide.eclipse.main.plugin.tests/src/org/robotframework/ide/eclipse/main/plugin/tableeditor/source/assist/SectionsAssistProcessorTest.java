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
import static org.mockito.Mockito.when;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.byApplyingToDocument;
import static org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.Proposals.proposalsWithImage;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.mockmodel.RobotSuiteFileCreator;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.RedCompletionBuilder.AcceptanceMode;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;
import org.robotframework.red.graphics.ImagesManager;

public class SectionsAssistProcessorTest {

    @Test
    public void sectionsProcessorIsValidOnlyForVariablesSection() {
        final SectionsAssistProcessor processor = new SectionsAssistProcessor(createAssitant());
        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.TEST_CASES_SECTION,
                SuiteSourcePartitionScanner.KEYWORDS_SECTION, SuiteSourcePartitionScanner.SETTINGS_SECTION,
                SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Test
    public void sectionsProcessorProcessorHasTitleDefined() {
        final SectionsAssistProcessor processor = new SectionsAssistProcessor(createAssitant());
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenIsNotInTheFirstCell() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = new Document("line1  cell", "line2");

        when(viewer.getDocument()).thenReturn(document);

        final SectionsAssistProcessor processor = new SectionsAssistProcessor(createAssitant());
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 7);

        assertThat(proposals).isNull();
    }

    @Test
    public void allSectionsProposalsAreProvided_whenInFirstColumnOfSuiteFile() {
        final List<String> lines = newArrayList("*** Test Cases ***", "");
        
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = new Document(lines);
        when(viewer.getDocument()).thenReturn(document);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLines(lines).build();
        final SectionsAssistProcessor processor = new SectionsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 19);
        
        assertThat(proposals).hasSize(4)
            .haveExactly(4, proposalsWithImage(ImagesManager.getImage(RedImages.getRobotCasesFileSectionImage())));
        
        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Test Cases ***", "*** Keywords ***", ""),
                new Document("*** Test Cases ***", "*** Test Cases ***", ""),
                new Document("*** Test Cases ***", "*** Variables ***", ""),
                new Document("*** Test Cases ***", "*** Settings ***", ""));
    }

    @Test
    public void allSectionsProposalsButTestCasesAreProvided_whenInFirstColumnOfResourceFile() {
        final List<String> lines = newArrayList("");
        
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = new Document(lines);
        when(viewer.getDocument()).thenReturn(document);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLines(lines).build();
        final SectionsAssistProcessor processor = new SectionsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 0);
        
        assertThat(proposals).hasSize(3)
            .haveExactly(3, proposalsWithImage(ImagesManager.getImage(RedImages.getRobotCasesFileSectionImage())));
        
        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Keywords ***", ""),
                new Document("*** Variables ***", ""),
                new Document("*** Settings ***", ""));
    }

    @Test
    public void onlyProposalsMatchingPrefixAreProvided_whenInsideFirstCell() {
        final List<String> lines = newArrayList("*** Test Cases ***", "*** Sett");

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = new Document(lines);
        when(viewer.getDocument()).thenReturn(document);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLines(lines).build();
        final SectionsAssistProcessor processor = new SectionsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 25);

        assertThat(proposals).hasSize(1).haveExactly(1,
                proposalsWithImage(ImagesManager.getImage(RedImages.getRobotCasesFileSectionImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Test Cases ***", "*** Settings ***", ""));
    }

    private static SuiteSourceAssistantContext createAssitant() {
        return createAssitant(null);
    }

    private static SuiteSourceAssistantContext createAssitant(final RobotSuiteFile model) {
        return new SuiteSourceAssistantContext(model, new AssistPreferences(AcceptanceMode.SUBSTITUTE, true, "  "));
    }
}
