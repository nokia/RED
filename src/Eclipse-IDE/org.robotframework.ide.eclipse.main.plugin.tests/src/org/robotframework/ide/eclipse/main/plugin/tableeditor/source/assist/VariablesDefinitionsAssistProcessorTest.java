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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.junit.Test;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.mockdocument.Document;
import org.robotframework.ide.eclipse.main.plugin.model.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.SuiteSourcePartitionScanner;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.source.assist.SuiteSourceAssistantContext.AssistPreferences;
import org.robotframework.red.graphics.ImagesManager;

import com.google.common.base.Supplier;

public class VariablesDefinitionsAssistProcessorTest {

    @Test
    public void varDefsProcessorIsValidOnlyForVariablesSection() {
        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        assertThat(processor.getApplicableContentTypes()).containsExactly(SuiteSourcePartitionScanner.VARIABLES_SECTION);
    }

    @Test
    public void varDefsProcessorHasTitleDefined() {
        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        assertThat(processor.getProposalsTitle()).isNotNull().isNotEmpty();
    }

    @Test
    public void noProposalsAreProvided_whenUserIsInNonVariableSection() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1", "line2"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(6)).thenReturn(SuiteSourcePartitionScanner.KEYWORDS_SECTION);

        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 6);

        assertThat(proposals).isNull();
    }

    @Test
    public void noProposalsAreProvided_whenIsInVariableSectionButNotInTheFirstCell() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1  cell", "line2"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(7)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 7);

        assertThat(proposals).isNull();
    }
    
    @Test
    public void proposalsAreProvided_whenAtTheEndOfFirstCellOfVariablesSection() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1", "cell2"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(11)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 11);

        assertThat(proposals).hasSize(3);
        assertThat(proposals.get(0))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));
        assertThat(proposals.get(1))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotListVariableImage())));
        assertThat(proposals.get(2))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotDictionaryVariableImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("line1", "${newScalar}  "),
                new Document("line1", "@{newList}  item  "),
                new Document("line1", "&{newDict}  key=value  "));
    }
    
    @Test
    public void proposalsAreProvided_whenInsideTheFirstCellOfVariablesSection() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1", "cell2"));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(8)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 8);

        assertThat(proposals).hasSize(3);
        assertThat(proposals.get(0))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));
        assertThat(proposals.get(1))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotListVariableImage())));
        assertThat(proposals.get(2))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotDictionaryVariableImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("line1", "${newScalar}"),
                new Document("line1", "@{newList}"),
                new Document("line1", "&{newDict}"));
    }

    @Test
    public void proposalsAreProvided_whenIsInFirstCellOfVariablesSection() throws Exception {
        final ITextViewer viewer = mock(ITextViewer.class);
        final IDocument document = spy(new Document("line1", ""));

        when(viewer.getDocument()).thenReturn(document);
        when(document.getContentType(6)).thenReturn(SuiteSourcePartitionScanner.VARIABLES_SECTION);

        final VariablesDefinitionsAssistProcessor processor = new VariablesDefinitionsAssistProcessor(createAssitant());
        final List<? extends ICompletionProposal> proposals = processor.computeProposals(viewer, 6);

        assertThat(proposals).hasSize(3);
        assertThat(proposals.get(0))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotScalarVariableImage())));
        assertThat(proposals.get(1))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotListVariableImage())));
        assertThat(proposals.get(2))
                .is(proposalWithImage(ImagesManager.getImage(RedImages.getRobotDictionaryVariableImage())));

        final List<IDocument> transformedDocuments = transform(proposals, byApplyingToDocument(document));
        assertThat(transformedDocuments).containsOnly(
                new Document("line1", "${newScalar}  "),
                new Document("line1", "@{newList}  item  "),
                new Document("line1", "&{newDict}  key=value  "));
    }

    private static SuiteSourceAssistantContext createAssitant() {
        final RobotSuiteFile file = mock(RobotSuiteFile.class);
        when(file.getFileExtension()).thenReturn("robot");
        return new SuiteSourceAssistantContext(new Supplier<RobotSuiteFile>() {

            @Override
            public RobotSuiteFile get() {
                return file;
            }
        }, new AssistPreferences(new MockRedPreferences(true, "  ")));
    }

}
