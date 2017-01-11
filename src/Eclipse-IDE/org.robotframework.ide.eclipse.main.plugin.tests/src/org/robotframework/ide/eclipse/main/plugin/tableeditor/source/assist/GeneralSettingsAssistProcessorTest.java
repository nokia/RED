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
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Supplier;

public class GeneralSettingsAssistProcessorTest {

    @Test
    public void generalSettingsProcessorIsValidOnlyForSettingsSection() {
        final GeneralSettingsAssistProcessor processor = new GeneralSettingsAssistProcessor(createAssitant());
        assertThat(processor.getApplicableContentTypes()).containsOnly(SuiteSourcePartitionScanner.SETTINGS_SECTION);
    }

    @Test
    public void generalSettingsProcessorProcessorHasTitleDefined() {
        final GeneralSettingsAssistProcessor processor = new GeneralSettingsAssistProcessor(createAssitant());
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenIsNotInSettingsSection() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1  cell", "line2"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(7)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final RobotSuiteFile model = new RobotSuiteFileCreator().build();
        final GeneralSettingsAssistProcessor processor = new GeneralSettingsAssistProcessor(createAssitant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 7);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenIsNotInTheFirstCell() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1  cell", "line2"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(7)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotSuiteFileCreator().build();
        final GeneralSettingsAssistProcessor processor = new GeneralSettingsAssistProcessor(createAssitant(model));
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 7);

        assertThat(proposals).isNull();
    }

    @Test
    public void allProposalsAreProvided_whenInFirstColumnOfSuiteFile() throws Exception {
        final List<String> lines = newArrayList("*** Settings ***", "");

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(lines));
        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(17)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLines(lines).build();
        final GeneralSettingsAssistProcessor processor = new GeneralSettingsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 17);

        assertThat(proposals).hasSize(13).haveExactly(13,
                proposalWithImage(ImagesManager.getImage(RedImages.getRobotSettingImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Library  "),
                new Document("*** Settings ***", "Resource  "),
                new Document("*** Settings ***", "Variables  "),
                new Document("*** Settings ***", "Metadata  "),
                new Document("*** Settings ***", "Documentation  "),
                new Document("*** Settings ***", "Suite Setup  "),
                new Document("*** Settings ***", "Suite Teardown  "),
                new Document("*** Settings ***", "Test Setup  "),
                new Document("*** Settings ***", "Test Teardown  "),
                new Document("*** Settings ***", "Test Timeout  "),
                new Document("*** Settings ***", "Test Template  "),
                new Document("*** Settings ***", "Default Tags  "),
                new Document("*** Settings ***", "Force Tags  "));
    }

    @Test
    public void onlyProposalsMatchingPrefixAreProvided_whenInsideFirstCell() throws Exception {
        final List<String> lines = newArrayList("*** Settings ***", "Test cell");

        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document(lines));
        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(21)).thenReturn(SuiteSourcePartitionScanner.SETTINGS_SECTION);

        final RobotSuiteFile model = new RobotSuiteFileCreator().appendLines(lines).build();
        final GeneralSettingsAssistProcessor processor = new GeneralSettingsAssistProcessor(createAssitant(model));

        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 21);

        assertThat(proposals).hasSize(4).haveExactly(4,
                proposalWithImage(ImagesManager.getImage(RedImages.getRobotSettingImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("*** Settings ***", "Test Setup"),
                new Document("*** Settings ***", "Test Teardown"),
                new Document("*** Settings ***", "Test Timeout"),
                new Document("*** Settings ***", "Test Template"));
    }

    private static SuiteSourceAssistantContext createAssitant() {
        return createAssitant(null);
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
